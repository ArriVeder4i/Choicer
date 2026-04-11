package com.example.choicer.viewmodel

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.choicer.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class Genre(val id: Int, val name: String)

class MovieViewModel(
    private val api: TmdbApi,
    private val kinopoiskApi: KinopoiskApi,
    private val dao: MovieDao,
    private val clipRepository: ClipRepository
) : ViewModel() {

    val trendingMovies = MutableStateFlow<List<Movie>>(emptyList())
    val searchResults = MutableStateFlow<List<Movie>>(emptyList())
    val isSearching = MutableStateFlow(false)

    private var currentPage = 1
    private var isPaginationLoading = false
    private var lastLoadedPage = 0

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

    private val _uiMessage = MutableSharedFlow<String>()
    val uiMessage = _uiMessage.asSharedFlow()

    init {
        loadMovies(1)
        viewModelScope.launch { clipRepository.refreshClips() }
    }

    fun loadMovies(page: Int) {
        if (isPaginationLoading || page == lastLoadedPage) return
        isPaginationLoading = true
        viewModelScope.launch {
            try {
                val response = api.getPopularMovies("5954a5e3f1d773aeff8fc45b928db34f", page = page)
                updateTrendingList(response.results, page)
                lastLoadedPage = page
                currentPage = page
            } catch (e: Exception) {
                try {
                    val kpResponse = kinopoiskApi.getPopularMovies(page = page)
                    val mapped = kpResponse.items.map { Movie(it.kinopoiskId, it.nameRu ?: it.nameOriginal ?: "Без названия", it.posterUrl, "", it.year?.toString() ?: "Н/Д", it.ratingKinopoisk ?: 0.0) }
                    updateTrendingList(mapped, page)
                    lastLoadedPage = page
                    currentPage = page
                } catch (e2: Exception) { Log.e("ViewModel", "Error", e2) }
            } finally { isPaginationLoading = false }
        }
    }

    private fun updateTrendingList(newMovies: List<Movie>, page: Int) {
        if (page == 1) trendingMovies.value = newMovies
        else {
            val currentList = trendingMovies.value.toMutableList()
            val filteredNew = newMovies.filter { new -> currentList.none { it.id == new.id } }
            trendingMovies.value = currentList + filteredNew
        }
    }

    fun getNextPage() { loadMovies(currentPage + 1) }

    fun searchMovies(query: String) {
        searchResults.value = emptyList()
        isSearching.value = true
        viewModelScope.launch {
            try {
                val resultsSet = mutableSetOf<Movie>()
                val genres = selectedGenres.value
                val keyword = query.ifBlank { null }
                if (genres.isEmpty()) {
                    val resp = kinopoiskApi.searchMovies(keyword, null, ratingRange.value.start.toDouble(), ratingRange.value.endInclusive.toDouble(), yearRange.value.start.toInt(), yearRange.value.endInclusive.toInt())
                    resultsSet.addAll(resp.items.map { Movie(it.kinopoiskId, it.nameRu ?: it.nameOriginal ?: "Без названия", it.posterUrl, "", it.year?.toString() ?: "", it.ratingKinopoisk ?: 0.0) })
                } else {
                    genres.forEach { genre ->
                        val resp = kinopoiskApi.searchMovies(keyword, genre.id, ratingRange.value.start.toDouble(), ratingRange.value.endInclusive.toDouble(), yearRange.value.start.toInt(), yearRange.value.endInclusive.toInt())
                        resultsSet.addAll(resp.items.map { Movie(it.kinopoiskId, it.nameRu ?: it.nameOriginal ?: "Без названия", it.posterUrl, "", it.year?.toString() ?: "", it.ratingKinopoisk ?: 0.0) })
                    }
                }
                searchResults.value = resultsSet.toList().sortedByDescending { it.vote_average }
            } catch (e: Exception) {
                if (query.isNotBlank()) {
                    try {
                        val response = api.searchMovies("5954a5e3f1d773aeff8fc45b928db34f", query)
                        searchResults.value = response.results
                    } catch (e2: Exception) {}
                }
            } finally { isSearching.value = false }
        }
    }

    fun toggleGenre(genre: Genre) {
        val current = selectedGenres.value.toMutableList()
        if (current.any { it.id == genre.id }) current.removeAll { it.id == genre.id } else current.add(genre)
        selectedGenres.value = current
    }

    fun addToWishlist(movie: Movie) { viewModelScope.launch(Dispatchers.IO) { try { dao.insertMovie(movie) } catch (e: Exception) {} } }
    fun removeFromWishlist(movie: Movie) { viewModelScope.launch(Dispatchers.IO) { try { dao.deleteMovie(movie) } catch (e: Exception) {} } }
    fun toggleWatchedStatus(movie: Movie) { viewModelScope.launch(Dispatchers.IO) { try { dao.updateMovie(movie.copy(isWatched = !movie.isWatched)) } catch (e: Exception) {} } }

    var selectedMovieForDetails = mutableStateOf<Movie?>(null)
    var detailedDescription = mutableStateOf("")
    var movieGenres = mutableStateOf("")
    var movieActors = mutableStateOf<List<KinopoiskStaff>>(emptyList())

    var currentVideoUrl = mutableStateOf<String?>(null)
    var isVideoLoading = mutableStateOf(false)

    fun hasClip(movieId: Int): Boolean = clipRepository.hasClip(movieId)

    fun loadVideo(movie: Movie) {
        if (isVideoLoading.value) return
        isVideoLoading.value = true
        currentVideoUrl.value = null

        viewModelScope.launch {
            val url = clipRepository.getClipUrl(movie.id)
            if (url != null) {
                currentVideoUrl.value = url
            } else {
                _uiMessage.emit("Клип для ID ${movie.id} еще не добавлен")
            }
            isVideoLoading.value = false
        }
    }

    fun loadExtraDetails(movieId: Int) {
        // ТЕПЕРЬ ЛОГ ЗДЕСЬ — ОН СРАБОТАЕТ ПРИ ЛЮБОМ КЛИКЕ
        Log.d("CLIP_DEBUG", "ВЫБРАН ФИЛЬМ. ID для JSON: $movieId")

        viewModelScope.launch {
            try {
                val details = kinopoiskApi.getMovieDetails(movieId)
                detailedDescription.value = details.description ?: ""
                movieGenres.value = details.genres?.joinToString(", ") { it.genre } ?: ""
                movieActors.value = kinopoiskApi.getMovieStaff(movieId).filter { it.professionKey == "ACTOR" }.take(15)
            } catch (e: Exception) {
                Log.e("ViewModel", "Error loading details for $movieId", e)
            }
        }
    }

    fun generateWishlistQR(): Bitmap? {
        val moviesData = wishlist.value.take(20).joinToString(";") { movie ->
            val safeTitle = movie.title.replace(";", "").replace("|", "")
            val shortPoster = if (movie.poster_path?.contains("posters/kp/") == true) "K${movie.id}" else if (movie.poster_path?.startsWith("/") == true) "T${movie.poster_path}" else "null"
            "${movie.id}|$safeTitle|$shortPoster|${movie.vote_average}"
        }
        if (moviesData.isEmpty()) return null
        val encodedData = Base64.encodeToString(moviesData.toByteArray(Charsets.UTF_8), Base64.NO_WRAP or Base64.URL_SAFE)
        val writer = com.google.zxing.qrcode.QRCodeWriter()
        val bitMatrix = writer.encode(encodedData, com.google.zxing.BarcodeFormat.QR_CODE, 512, 512)
        val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)
        for (x in 0 until 512) for (y in 0 until 512) bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        return bitmap
    }

    fun matchWishlists(scannedData: String) {
        val myMovies = wishlist.value.toMutableList()
        fun normalize(t: String?): String = t?.lowercase()?.replace("ё", "е")?.replace(Regex("(человек|фильм|сериал|movie|film|the)"), "")?.replace(Regex("[^a-zа-я0-9]"), "") ?: ""
        try {
            val decodedBytes = Base64.decode(scannedData, Base64.NO_WRAP or Base64.URL_SAFE)
            val rawData = String(decodedBytes, Charsets.UTF_8)
            val friendMovies = rawData.split(";").mapNotNull { part ->
                val f = part.split("|")
                if (f.size >= 4) Movie(f[0].toIntOrNull() ?: 0, f[1], if (f[2].startsWith("K")) "https://kinopoiskapiunofficial.tech/images/posters/kp/${f[2].drop(1)}.jpg" else f[2].drop(1), "", "", f[3].toDoubleOrNull() ?: 0.0) else null
            }
            _matchedMovies.value = friendMovies.filter { fMovie -> val fTitle = normalize(fMovie.title); myMovies.any { myMovie -> val myTitle = normalize(myMovie.title); fMovie.id == myMovie.id || (fTitle.length > 4 && (fTitle.contains(myTitle) || myTitle.contains(fTitle))) } }
            val combined = myMovies.toMutableList(); friendMovies.forEach { fMovie -> val fTitle = normalize(fMovie.title); val exists = combined.any { it.id == fMovie.id || normalize(it.title).let { t -> fTitle.contains(t) || t.contains(fTitle) } }; if (!exists) combined.add(fMovie) }
            _combinedMovies.value = combined; _isConnected.value = true
        } catch (e: Exception) {}
    }
}