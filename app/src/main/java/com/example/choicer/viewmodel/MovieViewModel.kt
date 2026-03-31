package com.example.choicer.viewmodel

import android.graphics.Bitmap
import android.util.Base64
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.choicer.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.nio.charset.Charset

data class Genre(val id: Int, val name: String)

class MovieViewModel(
    private val api: TmdbApi,
    private val kinopoiskApi: KinopoiskApi,
    private val dao: MovieDao
) : ViewModel() {

    val trendingMovies = MutableStateFlow<List<Movie>>(emptyList())
    val searchResults = MutableStateFlow<List<Movie>>(emptyList())
    val isSearching = MutableStateFlow(false)

    val wishlist = dao.getAllMovies().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    private val _matchedMovies = MutableStateFlow<List<Movie>>(emptyList())
    val matchedMovies: StateFlow<List<Movie>> = _matchedMovies.asStateFlow()
    private val _combinedMovies = MutableStateFlow<List<Movie>>(emptyList())
    val combinedMovies: StateFlow<List<Movie>> = _combinedMovies.asStateFlow()

    // Состояния фильтров
    val selectedGenres = MutableStateFlow<List<Genre>>(emptyList())
    val ratingRange = MutableStateFlow(0f..10f)
    val yearRange = MutableStateFlow(1980f..2025f)

    val genresList = listOf(
        Genre(1, "Триллер"), Genre(2, "Драма"), Genre(3, "Криминал"),
        Genre(4, "Мелодрама"), Genre(5, "Детектив"), Genre(6, "Фантастика"),
        Genre(7, "Приключения"), Genre(11, "Боевик"), Genre(12, "Фэнтези"),
        Genre(13, "Комедия"), Genre(17, "Ужасы"), Genre(18, "Мультфильм"),
        Genre(19, "Семейный"), Genre(22, "Документальный"), Genre(24, "Аниме")
    )

    init { loadMovies() }

    private fun loadMovies() {
        viewModelScope.launch {
            try {
                val response = api.getPopularMovies("5954a5e3f1d773aeff8fc45b928db34f")
                trendingMovies.value = response.results
            } catch (e: Exception) {
                try {
                    val kpResponse = kinopoiskApi.getPopularMovies()
                    trendingMovies.value = kpResponse.items.map { kpFilm ->
                        Movie(kpFilm.kinopoiskId, kpFilm.nameRu ?: kpFilm.nameOriginal ?: "Без названия", kpFilm.posterUrl, "", kpFilm.year?.toString() ?: "", kpFilm.ratingKinopoisk ?: 0.0)
                    }
                } catch (e2: Exception) {}
            }
        }
    }

    // ЛОГИКА "ИЛИ" ДЛЯ ЖАНРОВ
    fun searchMovies(query: String) {
        searchResults.value = emptyList()
        isSearching.value = true
        viewModelScope.launch {
            try {
                val resultsSet = mutableSetOf<Movie>()
                val genres = selectedGenres.value
                val keyword = query.ifBlank { null }

                if (genres.isEmpty()) {
                    // Обычный поиск, если жанры не выбраны
                    val resp = kinopoiskApi.searchMovies(
                        keyword = keyword,
                        ratingFrom = ratingRange.value.start.toDouble(),
                        ratingTo = ratingRange.value.endInclusive.toDouble(),
                        yearFrom = yearRange.value.start.toInt(),
                        yearTo = yearRange.value.endInclusive.toInt()
                    )
                    resultsSet.addAll(resp.items.map { kpFilm ->
                        Movie(kpFilm.kinopoiskId, kpFilm.nameRu ?: kpFilm.nameOriginal ?: "Без названия", kpFilm.posterUrl, "", kpFilm.year?.toString() ?: "", kpFilm.ratingKinopoisk ?: 0.0)
                    })
                } else {
                    // Делаем по запросу на каждый жанр (Логика ИЛИ)
                    genres.forEach { genre ->
                        val resp = kinopoiskApi.searchMovies(
                            keyword = keyword,
                            genreId = genre.id,
                            ratingFrom = ratingRange.value.start.toDouble(),
                            ratingTo = ratingRange.value.endInclusive.toDouble(),
                            yearFrom = yearRange.value.start.toInt(),
                            yearTo = yearRange.value.endInclusive.toInt()
                        )
                        resultsSet.addAll(resp.items.map { kpFilm ->
                            Movie(kpFilm.kinopoiskId, kpFilm.nameRu ?: kpFilm.nameOriginal ?: "Без названия", kpFilm.posterUrl, "", kpFilm.year?.toString() ?: "", kpFilm.ratingKinopoisk ?: 0.0)
                        })
                    }
                }
                // Сортируем по рейтингу и обновляем список
                searchResults.value = resultsSet.toList().sortedByDescending { it.vote_average }
            } catch (e: Exception) {
                if (query.isNotBlank()) {
                    try {
                        val response = api.searchMovies("5954a5e3f1d773aeff8fc45b928db34f", query)
                        searchResults.value = response.results
                    } catch (e2: Exception) {}
                }
            } finally {
                isSearching.value = false
            }
        }
    }

    fun toggleGenre(genre: Genre) {
        val current = selectedGenres.value.toMutableList()
        if (current.any { it.id == genre.id }) current.removeAll { it.id == genre.id } else current.add(genre)
        selectedGenres.value = current
    }

    // Методы для БД
    fun addToWishlist(movie: Movie) { viewModelScope.launch(Dispatchers.IO) { dao.insertMovie(movie) } }
    fun removeFromWishlist(movie: Movie) { viewModelScope.launch(Dispatchers.IO) { dao.deleteMovie(movie) } }
    fun toggleWatchedStatus(movie: Movie) { viewModelScope.launch(Dispatchers.IO) { dao.updateMovie(movie.copy(isWatched = !movie.isWatched)) } }

    var selectedMovieForDetails = mutableStateOf<Movie?>(null)
    var detailedDescription = mutableStateOf("")
    var movieGenres = mutableStateOf("")
    var movieActors = mutableStateOf<List<KinopoiskStaff>>(emptyList())

    fun loadExtraDetails(movieId: Int) {
        viewModelScope.launch {
            try {
                val details = kinopoiskApi.getMovieDetails(movieId)
                detailedDescription.value = details.description ?: ""
                movieGenres.value = details.genres?.joinToString(", ") { it.genre } ?: ""
                movieActors.value = kinopoiskApi.getMovieStaff(movieId).filter { it.professionKey == "ACTOR" }.take(15)
            } catch (e: Exception) {}
        }
    }

    // Генерация QR (используем твой оригинальный метод из файлов выше)
    fun generateWishlistQR(): Bitmap? {
        val moviesData = wishlist.value.take(20).joinToString(";") { movie ->
            val safeTitle = movie.title?.replace(";", "")?.replace("|", "") ?: "Без названия"
            val shortPoster = when {
                movie.poster_path?.contains("posters/kp/") == true -> "K${movie.id}"
                movie.poster_path?.startsWith("/") == true -> "T${movie.poster_path}"
                else -> "null"
            }
            "${movie.id}|$safeTitle|$shortPoster|${movie.vote_average}"
        }
        if (moviesData.isEmpty()) return null
        val encodedData = android.util.Base64.encodeToString(moviesData.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP or android.util.Base64.URL_SAFE)
        val writer = com.google.zxing.qrcode.QRCodeWriter()
        val bitMatrix = writer.encode(encodedData, com.google.zxing.BarcodeFormat.QR_CODE, 512, 512)
        val bitmap = android.graphics.Bitmap.createBitmap(512, 512, android.graphics.Bitmap.Config.RGB_565)
        for (x in 0 until 512) { for (y in 0 until 512) { bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE) } }
        return bitmap
    }

    fun matchWishlists(data: String) { /* Твоя логика мэтчинга из файлов выше */ }
}