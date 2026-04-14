package com.example.choicer.data

data class MovieSearchFilters(
    val query: String? = null,
    val genreId: Int? = null,
    val ratingFrom: Double? = null,
    val ratingTo: Double? = null,
    val yearFrom: Int? = null,
    val yearTo: Int? = null
)

data class MovieCollections(
    val russianMovies: List<Movie>,
    val russianSeries: List<Movie>,
    val foreignMovies: List<Movie>,
    val foreignSeries: List<Movie>
)

data class MovieDetailsPayload(
    val description: String,
    val genres: String,
    val actors: List<KinopoiskStaff>
)

class MovieCatalogRepository(
    private val kinopoiskApi: KinopoiskApi
) {
    suspend fun loadTrendingPage(page: Int): List<Movie> =
        kinopoiskApi.getCollections(page = page).items.toMovies()

    suspend fun loadPopularCollections(targetCount: Int = 20): MovieCollections =
        MovieCollections(
            russianMovies = fetchFilteredMovies("TOP_POPULAR_MOVIES", true, targetCount),
            russianSeries = fetchFilteredMovies("POPULAR_SERIES", true, targetCount),
            foreignMovies = fetchFilteredMovies("TOP_POPULAR_MOVIES", false, targetCount),
            foreignSeries = fetchFilteredMovies("POPULAR_SERIES", false, targetCount)
        )

    suspend fun searchMovies(filters: MovieSearchFilters): List<Movie> =
        kinopoiskApi.searchMovies(
            keyword = filters.query?.takeIf { it.isNotBlank() },
            genreId = filters.genreId,
            ratingFrom = filters.ratingFrom,
            ratingTo = filters.ratingTo,
            yearFrom = filters.yearFrom,
            yearTo = filters.yearTo
        ).items
            .toMovies()
            .sortedByDescending { it.vote_average ?: 0.0 }

    suspend fun loadMovieDetails(movieId: Int): MovieDetailsPayload {
        val details = kinopoiskApi.getMovieDetails(movieId)
        val actors = kinopoiskApi.getMovieStaff(movieId)
            .filter { it.professionKey == "ACTOR" }
            .take(15)

        return MovieDetailsPayload(
            description = details.description.orEmpty(),
            genres = details.genres?.joinToString(", ") { it.genre }.orEmpty(),
            actors = actors
        )
    }

    private suspend fun fetchFilteredMovies(
        collectionType: String,
        isRussian: Boolean,
        targetCount: Int
    ): List<Movie> {
        val collected = mutableListOf<KinopoiskFilm>()
        var page = 1

        while (collected.size < targetCount && page <= 5) {
            val response = kinopoiskApi.getCollections(type = collectionType, page = page)
            val filtered = response.items.filter { film ->
                val hasRu = film.countries?.any { country ->
                    country.country.contains("Россия", ignoreCase = true)
                } == true
                if (isRussian) hasRu else !hasRu
            }

            collected += filtered
            page++
        }

        return collected
            .take(targetCount)
            .toMovies()
    }

    private fun List<KinopoiskFilm>.toMovies(): List<Movie> = map { film ->
        Movie(
            id = film.kinopoiskId,
            title = film.nameRu ?: film.nameOriginal ?: "Без названия",
            poster_path = film.posterUrl.orEmpty(),
            overview = "",
            release_date = film.year?.toString().orEmpty(),
            vote_average = film.ratingKinopoisk ?: 0.0
        )
    }
}
