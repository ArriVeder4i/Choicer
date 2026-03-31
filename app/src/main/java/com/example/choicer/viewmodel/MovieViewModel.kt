package com.example.choicer.viewmodel

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.choicer.data.KinopoiskApi
import com.example.choicer.data.Movie
import com.example.choicer.data.MovieDao
import com.example.choicer.data.TmdbApi
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.util.Base64
import java.nio.charset.Charset

class MovieViewModel(
    private val api: TmdbApi,
    private val kinopoiskApi: KinopoiskApi,
    private val dao: MovieDao
) : ViewModel() {

    val trendingMovies = MutableStateFlow<List<Movie>>(emptyList())
    val searchResults = MutableStateFlow<List<Movie>>(emptyList())

    val wishlist = dao.getAllMovies().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    // 1. ТОЛЬКО СОВПАДЕНИЯ
    private val _matchedMovies = MutableStateFlow<List<Movie>>(emptyList())
    val matchedMovies: StateFlow<List<Movie>> = _matchedMovies.asStateFlow()

    // 2. ОБЪЕДИНЕННЫЙ СПИСОК (Мои + Друга)
    private val _combinedMovies = MutableStateFlow<List<Movie>>(emptyList())
    val combinedMovies: StateFlow<List<Movie>> = _combinedMovies.asStateFlow()

    init {
        loadMovies()
    }

    private fun loadMovies() {
        viewModelScope.launch {
            try {
                val response = api.getPopularMovies("5954a5e3f1d773aeff8fc45b928db34f")
                trendingMovies.value = response.results
            } catch (e: Exception) {
                try {
                    val kpResponse = kinopoiskApi.getPopularMovies()
                    val mappedMovies = kpResponse.items.map { kpFilm ->
                        Movie(
                            id = kpFilm.kinopoiskId,
                            title = kpFilm.nameRu ?: kpFilm.nameOriginal ?: "Без названия",
                            poster_path = kpFilm.posterUrl,
                            overview = "Описание недоступно",
                            release_date = kpFilm.year?.toString() ?: "Н/Д",
                            vote_average = kpFilm.ratingKinopoisk ?: 0.0
                        )
                    }
                    trendingMovies.value = mappedMovies
                } catch (e2: Exception) {
                }
            }
        }
    }

    fun addToWishlist(movie: Movie) {
        viewModelScope.launch(Dispatchers.IO) { dao.insertMovie(movie) }
    }

    fun removeFromWishlist(movie: Movie) {
        viewModelScope.launch(Dispatchers.IO) { dao.deleteMovie(movie) }
    }

    fun toggleWatchedStatus(movie: Movie) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedMovie = movie.copy(isWatched = !movie.isWatched)
            dao.updateMovie(updatedMovie)
        }
    }

    fun searchMovies(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            try {
                val response = api.searchMovies("5954a5e3f1d773aeff8fc45b928db34f", query)
                searchResults.value = response.results
            } catch (e: Exception) {
                try {
                    val kpResponse = kinopoiskApi.searchMovies(query)
                    val mappedMovies = kpResponse.items.map { kpFilm ->
                        Movie(
                            id = kpFilm.kinopoiskId,
                            title = kpFilm.nameRu ?: kpFilm.nameOriginal ?: "Без названия",
                            poster_path = kpFilm.posterUrl,
                            overview = "Описание",
                            release_date = kpFilm.year?.toString() ?: "Н/Д",
                            vote_average = kpFilm.ratingKinopoisk ?: 0.0
                        )
                    }
                    searchResults.value = mappedMovies
                } catch (e2: Exception) {
                }
            }
        }
    }

    var selectedMovieForDetails = mutableStateOf<Movie?>(null)
    var detailedDescription = mutableStateOf("Загрузка описания...")
    var movieGenres = mutableStateOf("")
    var movieActors = mutableStateOf<List<com.example.choicer.data.KinopoiskStaff>>(emptyList())

    fun loadExtraDetails(movieId: Int) {
        detailedDescription.value = "Загрузка описания..."
        movieGenres.value = ""
        movieActors.value = emptyList()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val details = kinopoiskApi.getMovieDetails(movieId)
                detailedDescription.value = details.description ?: "Описание отсутствует."
                movieGenres.value = details.genres?.joinToString(", ") { it.genre }
                    ?.replaceFirstChar { it.uppercase() } ?: ""
                val staff = kinopoiskApi.getMovieStaff(movieId)
                movieActors.value =
                    staff.filter { it.professionKey == "ACTOR" && !it.posterUrl.isNullOrEmpty() }
                        .take(15)
            } catch (e: Exception) {
            }
        }
    }

    // --- ЛОГИКА ДРУЗЕЙ (С УМНЫМ МЭТЧИНГОМ И ИСПРАВЛЕННЫМ BASE64) ---

    fun generateWishlistQR(): Bitmap? {
        // Берем до 20 фильмов, чтобы QR был максимально простым для сканера
        val moviesData = wishlist.value.take(20).joinToString(";") { movie ->
            val safeTitle = movie.title?.replace(";", "")?.replace("|", "") ?: "Без названия"

            // Сжимаем путь: оставляем только ID для Кинопоиска или чистый путь для TMDB
            val shortPoster = when {
                movie.poster_path?.contains("posters/kp/") == true -> "K${movie.id}"
                movie.poster_path?.startsWith("/") == true -> "T${movie.poster_path}"
                else -> "null"
            }
            "${movie.id}|$safeTitle|$shortPoster|${movie.vote_average}"
        }

        if (moviesData.isEmpty()) return null

        // Используем NO_WRAP и URL_SAFE, чтобы данные не искажались
        val encodedData = android.util.Base64.encodeToString(
            moviesData.toByteArray(Charsets.UTF_8),
            android.util.Base64.NO_WRAP or android.util.Base64.URL_SAFE
        )

        val writer = com.google.zxing.qrcode.QRCodeWriter()
        val bitMatrix = writer.encode(encodedData, com.google.zxing.BarcodeFormat.QR_CODE, 512, 512)
        val bitmap = android.graphics.Bitmap.createBitmap(512, 512, android.graphics.Bitmap.Config.RGB_565)
        for (x in 0 until 512) {
            for (y in 0 until 512) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return bitmap
    }

    fun matchWishlists(scannedData: String) {
        val myMovies = wishlist.value.toMutableList()

        // Улучшенная нормализация: убираем лишние слова типа "человек", "фильм", "сериал"
        fun normalize(t: String?): String {
            return t?.lowercase()
                ?.replace("ё", "е")
                ?.replace(Regex("(человек|фильм|сериал|movie|film|the)"), "")
                ?.replace(Regex("[^a-zа-я0-9]"), "") ?: ""
        }

        try {
            val decodedBytes = android.util.Base64.decode(scannedData, android.util.Base64.NO_WRAP or android.util.Base64.URL_SAFE)
            val rawData = String(decodedBytes, Charsets.UTF_8)

            val friendMovies = rawData.split(";").mapNotNull { part ->
                val f = part.split("|")
                if (f.size >= 4) {
                    val id = f[0].toIntOrNull() ?: 0
                    val title = f[1]

                    // Восстанавливаем постер из короткого кода
                    val posterPath = when {
                        f[2].startsWith("K") -> "https://kinopoiskapiunofficial.tech/images/posters/kp/${f[2].drop(1)}.jpg"
                        f[2].startsWith("T") -> f[2].drop(1)
                        else -> null
                    }

                    Movie(id = id, title = title, poster_path = posterPath, overview = "", release_date = "", vote_average = f[3].toDoubleOrNull() ?: 0.0)
                } else null
            }

            // УМНЫЙ МЭТЧ: Если одно название содержит другое (минимум 5 символов)
            _matchedMovies.value = friendMovies.filter { fMovie ->
                val fTitle = normalize(fMovie.title)
                myMovies.any { myMovie ->
                    val myTitle = normalize(myMovie.title)
                    fMovie.id == myMovie.id ||
                            (fTitle.length > 4 && (fTitle.contains(myTitle) || myTitle.contains(fTitle)))
                }
            }

            // ОБЪЕДИНЕНИЕ без дублей
            val combined = myMovies.toMutableList()
            friendMovies.forEach { fMovie ->
                val fTitle = normalize(fMovie.title)
                val exists = combined.any {
                    it.id == fMovie.id || (normalize(it.title).let { t -> fTitle.contains(t) || t.contains(fTitle) })
                }
                if (!exists) combined.add(fMovie)
            }

            _combinedMovies.value = combined
            _isConnected.value = true
        } catch (e: Exception) { }
    }
}