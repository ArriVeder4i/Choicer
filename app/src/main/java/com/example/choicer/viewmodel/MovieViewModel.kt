package com.example.choicer.viewmodel

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.choicer.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class Genre(val id: Int, val name: String)

data class ScannedDevice(
    val name: String,
    val address: String,
    val rawDevice: BluetoothDevice
)

class MovieViewModel(
    private val movieCatalogRepository: MovieCatalogRepository,
    private val dao: MovieDao,
    private val clipRepository: ClipRepository
) : ViewModel() {

    val trendingMovies = MutableStateFlow<List<Movie>>(emptyList())
    val searchResults = MutableStateFlow<List<Movie>>(emptyList())

    val popularRuMovies = MutableStateFlow<List<Movie>>(emptyList())
    val popularRuSeries = MutableStateFlow<List<Movie>>(emptyList())
    val popularForeignMovies = MutableStateFlow<List<Movie>>(emptyList())
    val popularForeignSeries = MutableStateFlow<List<Movie>>(emptyList())

    private var currentPage = 1
    private var isPaginationLoading = false

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

    private val _discoveredDevices = MutableStateFlow<List<ScannedDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<ScannedDevice>> = _discoveredDevices.asStateFlow()

    val compatibilityPercent = MutableStateFlow(0)
    val compatibilityRating = MutableStateFlow(0.0)

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

    var isVideoMode = mutableStateOf(false)
    var activeVideoId = mutableStateOf<Int?>(null)
    var currentVideoUrl = mutableStateOf<String?>(null)
    var isVideoLoading = mutableStateOf(false)

    var selectedMovieForDetails = mutableStateOf<Movie?>(null)
    var detailedDescription = mutableStateOf("")
    var movieGenres = mutableStateOf("")
    var movieActors = mutableStateOf<List<KinopoiskStaff>>(emptyList())

    init {
        loadMovies(1)
        loadPopularCollections()
        viewModelScope.launch { clipRepository.refreshClips() }
    }

    fun loadPopularCollections() {
        viewModelScope.launch {
            try {
                val collections = movieCatalogRepository.loadPopularCollections()
                popularRuMovies.value = collections.russianMovies
                popularForeignMovies.value = collections.foreignMovies
                popularRuSeries.value = collections.russianSeries
                popularForeignSeries.value = collections.foreignSeries
            } catch (e: Exception) {
                Log.e("Popular", "Ошибка", e)
            }
        }
    }

    fun loadMovies(page: Int) {
        if (isPaginationLoading) return
        isPaginationLoading = true

        viewModelScope.launch {
            try {
                val movies = movieCatalogRepository.loadTrendingPage(page)
                updateTrendingList(movies, page)
                currentPage = page
            } catch (e: Exception) {
                Log.e("Pagination", "Не удалось загрузить страницу $page", e)
            } finally {
                isPaginationLoading = false
            }
        }
    }

    private fun updateTrendingList(newMovies: List<Movie>, page: Int) {
        if (newMovies.isEmpty()) return

        if (page == 1) {
            trendingMovies.value = newMovies
        } else {
            val current = trendingMovies.value.toMutableList()
            val filtered = newMovies.filter { n -> current.none { it.id == n.id } }
            trendingMovies.value = current + filtered
        }
    }

    fun getNextPage() {
        loadMovies(currentPage + 1)
    }

    fun toggleGenre(genre: Genre) {
        val current = selectedGenres.value.toMutableList()
        if (current.any { it.id == genre.id }) current.removeAll { it.id == genre.id }
        else current.add(genre)
        selectedGenres.value = current
    }

    fun searchMovies(query: String) {
        searchResults.value = emptyList()

        viewModelScope.launch {
            try {
                val genres = selectedGenres.value
                searchResults.value = movieCatalogRepository.searchMovies(
                    MovieSearchFilters(
                        query = query.takeIf { it.isNotBlank() },
                        genreId = if (genres.isEmpty()) null else genres.first().id,
                        ratingFrom = ratingRange.value.start.toDouble(),
                        ratingTo = ratingRange.value.endInclusive.toDouble(),
                        yearFrom = yearRange.value.start.toInt(),
                        yearTo = yearRange.value.endInclusive.toInt()
                    )
                )
            } catch (e: Exception) {
                Log.e("Search", "Error", e)
            }
        }
    }

    fun hasClip(movieId: Int): Boolean = clipRepository.hasClip(movieId)

    fun toggleVideoMode(movie: Movie) {
        if (!isVideoMode.value) {
            isVideoMode.value = true
            activeVideoId.value = movie.id
            loadVideo(movie)
        } else {
            isVideoMode.value = false
            activeVideoId.value = null
            currentVideoUrl.value = null
        }
    }

    fun loadVideo(movie: Movie) {
        isVideoLoading.value = true
        currentVideoUrl.value = null
        viewModelScope.launch {
            currentVideoUrl.value = clipRepository.getClipUrl(movie.id)
            isVideoLoading.value = false
        }
    }

    fun loadExtraDetails(movieId: Int) {
        viewModelScope.launch {
            try {
                val details = movieCatalogRepository.loadMovieDetails(movieId)
                detailedDescription.value = details.description
                movieGenres.value = details.genres
                movieActors.value = details.actors
            } catch (e: Exception) {
                Log.e("Details", "Error", e)
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
            dao.updateMovie(movie.copy(isWatched = !movie.isWatched))
        }
    }

    fun generateWishlistQR(): Bitmap? {
        val encodedData = WishlistShareCodec.encode(wishlist.value)
        if (encodedData.isBlank()) return null

        return try {
            val writer = com.google.zxing.qrcode.QRCodeWriter()
            val matrix = writer.encode(encodedData, com.google.zxing.BarcodeFormat.QR_CODE, 512, 512)
            val bmp = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)
            for (x in 0 until 512)
                for (y in 0 until 512)
                    bmp.setPixel(x, y, if (matrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            bmp
        } catch (_: Exception) {
            null
        }
    }

    fun generateWishlistQRData(): String = WishlistShareCodec.encode(wishlist.value)

    @SuppressLint("MissingPermission")
    fun addDiscoveredDevice(device: BluetoothDevice, advertisedName: String? = null) {
        val deviceAddress = device.address ?: ""
        val deviceName = advertisedName
            ?.takeIf { it.isNotBlank() }
            ?: try {
                device.name?.takeIf { it.isNotBlank() }
            } catch (e: Exception) {
                null
            }
            ?: deviceAddress.ifBlank { "Неизвестное устройство" }

        _discoveredDevices.update { current ->
            val existingIndex = current.indexOfFirst {
                it.address.equals(deviceAddress, ignoreCase = true) ||
                    it.name.equals(deviceName, ignoreCase = true)
            }

            if (existingIndex == -1) {
                current + ScannedDevice(deviceName, deviceAddress, device)
            } else {
                current.toMutableList().apply {
                    val existing = this[existingIndex]
                    this[existingIndex] = existing.copy(
                        name = deviceName,
                        address = deviceAddress.ifBlank { existing.address },
                        rawDevice = device
                    )
                }
            }
        }
    }

    fun connectToFoundDevice(device: ScannedDevice) {
        BleManager.connectToDevice(device.rawDevice)
    }

    fun clearDiscoveredDevices() {
        _discoveredDevices.value = emptyList()
    }

    fun matchWishlists(scannedData: String) {
        val myMovies = wishlist.value

        fun normalize(t: String?): String =
            t?.lowercase()
                ?.replace("ё", "е")
                ?.replace(Regex("(человек|фильм|сериал|movie|film|the)"), "")
                ?.replace(Regex("[^a-zа-я0-9]"), "") ?: ""

        try {
            val friendList = WishlistShareCodec.decode(scannedData)

            val matched = friendList.filter { f ->
                val ft = normalize(f.title)
                myMovies.any {
                    val mt = normalize(it.title)
                    f.id == it.id || (ft.length > 4 && (ft.contains(mt) || mt.contains(ft)))
                }
            }

            _matchedMovies.value = matched

            _combinedMovies.value = (myMovies + friendList)
                .distinctBy { it.id }

            val total = myMovies.size + friendList.size
            compatibilityPercent.value =
                if (total == 0) 0 else (2 * matched.size * 100) / total

            compatibilityRating.value =
                matched.mapNotNull { it.vote_average }
                    .takeIf { it.isNotEmpty() }
                    ?.average() ?: 0.0

            _isConnected.value = true

        } catch (e: Exception) {
            Log.e("Match", "Error", e)
        }
    }

    fun getRandomMovieFromMatch(): Movie? = _matchedMovies.value.randomOrNull()
    fun getRandomMovieFromCombined(): Movie? = _combinedMovies.value.randomOrNull()
    fun disconnect() {
        _isConnected.value = false
        _matchedMovies.value = emptyList()
        _combinedMovies.value = emptyList()
        _discoveredDevices.value = emptyList()
        compatibilityPercent.value = 0
        compatibilityRating.value = 0.0
    }
}
