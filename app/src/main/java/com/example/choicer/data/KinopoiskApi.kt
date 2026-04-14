package com.example.choicer.data

import com.example.choicer.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.http.Path

interface KinopoiskApi {
    // Обновленный эндпоинт коллекций (теперь можно передавать тип)
    @GET("api/v2.2/films/collections")
    suspend fun getCollections(
        @Query("type") type: String = "TOP_POPULAR_ALL",
        @Query("page") page: Int = 1,
        @Header("X-API-KEY") apiKey: String = BuildConfig.KINOPOISK_API_KEY
    ): KinopoiskResponse

    @GET("api/v2.2/films")
    suspend fun searchMovies(
        @Query("keyword") keyword: String? = null,
        @Query("genres") genreId: Int? = null,
        @Query("ratingFrom") ratingFrom: Double? = null,
        @Query("ratingTo") ratingTo: Double? = null,
        @Query("yearFrom") yearFrom: Int? = null,
        @Query("yearTo") yearTo: Int? = null,
        @Query("countries") country: Int? = null,
        @Query("type") type: String? = null,
        @Query("order") order: String = "NUM_VOTE", // Популярные сейчас (по кол-ву голосов)
        @Header("X-API-KEY") apiKey: String = BuildConfig.KINOPOISK_API_KEY
    ): KinopoiskResponse

    @GET("api/v2.2/films/{id}")
    suspend fun getMovieDetails(
        @Path("id") id: Int,
        @Header("X-API-KEY") apiKey: String = BuildConfig.KINOPOISK_API_KEY
    ): KinopoiskFilmDetail

    @GET("api/v1/staff")
    suspend fun getMovieStaff(
        @Query("filmId") filmId: Int,
        @Header("X-API-KEY") apiKey: String = BuildConfig.KINOPOISK_API_KEY
    ): List<KinopoiskStaff>
}

data class KinopoiskResponse(val items: List<KinopoiskFilm>)

data class KinopoiskFilm(
    val kinopoiskId: Int,
    val nameRu: String?,
    val nameOriginal: String?,
    val posterUrl: String?,
    val year: Int?,
    val ratingKinopoisk: Double?,
    val countries: List<KinopoiskCountry>? // Добавили страны для фильтрации
)

data class KinopoiskCountry(val country: String)

// Остальные модели (KinopoiskFilmDetail, KinopoiskStaff и т.д.) без изменений

data class KinopoiskFilmDetail(
    val description: String?,
    val genres: List<KinopoiskGenre>?,
    val imdbId: String? = null
)

data class KinopoiskGenre(val genre: String)

data class KinopoiskStaff(
    val staffId: Int,
    val nameRu: String?,
    val posterUrl: String?,
    val professionKey: String
)
