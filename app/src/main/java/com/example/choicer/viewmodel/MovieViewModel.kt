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

    init { loadMovies() }

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
                } catch (e2: Exception) { }
            }
        }
    }

    fun addToWishlist(movie: Movie) { viewModelScope.launch(Dispatchers.IO) { dao.insertMovie(movie) } }
    fun removeFromWishlist(movie: Movie) { viewModelScope.launch(Dispatchers.IO) { dao.deleteMovie(movie) } }
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
                } catch (e2: Exception) { }
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
                movieGenres.value = details.genres?.joinToString(", ") { it.genre }?.replaceFirstChar { it.uppercase() } ?: ""
                val staff = kinopoiskApi.getMovieStaff(movieId)
                movieActors.value = staff.filter { it.professionKey == "ACTOR" && !it.posterUrl.isNullOrEmpty() }.take(15)
            } catch (e: Exception) { }
        }
    }

    fun generateWishlistQR(): Bitmap? {
        val moviesData = wishlist.value.take(40).joinToString(";") { movie ->
            val safeTitle = movie.title?.replace(";", "")?.replace("|", "") ?: "Без названия"
            val safePoster = movie.poster_path ?: "null"
            "${movie.id}|$safeTitle|$safePoster|${movie.vote_average}"
        }

        if (moviesData.isEmpty()) return null

        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(moviesData, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return bitmap
    }

    fun matchWishlists(scannedData: String) {
        val myMovies = wishlist.value.toMutableList()
        val myIds = myMovies.map { it.id }.toSet()

        val friendMovies = scannedData.split(";").mapNotNull { part ->
            val fields = part.split("|")
            if (fields.size >= 4) {
                val id = fields[0].toIntOrNull() ?: return@mapNotNull null
                val title = fields[1]
                val poster = if (fields[2] == "null") null else fields[2]
                val rating = fields[3].toDoubleOrNull() ?: 0.0

                Movie(id = id, title = title, poster_path = poster, overview = "Фильм из вишлиста друга", release_date = "Н/Д", vote_average = rating)
            } else null
        }

        // 1. Вычисляем только совпадения (пересечение)
        val matched = friendMovies.filter { it.id in myIds }
        _matchedMovies.value = matched

        // 2. Вычисляем объединенный список (уникальные фильмы из обоих списков)
        val combined = myMovies.toMutableList()
        for (friendMovie in friendMovies) {
            if (friendMovie.id !in myIds) {
                combined.add(friendMovie)
            }
        }
        _combinedMovies.value = combined

        _isConnected.value = true
    }
}