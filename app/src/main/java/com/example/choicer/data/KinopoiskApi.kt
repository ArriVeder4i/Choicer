package com.example.choicer.data

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import retrofit2.http.Path

// 1. Интерфейс запросов к Кинопоиску
interface KinopoiskApi {
    @Headers("X-API-KEY: adcbe508-1726-4266-a036-2dc569f73c66")
    @GET("api/v2.2/films/collections?type=TOP_POPULAR_ALL")
    suspend fun getPopularMovies(): KinopoiskResponse

    // Функция поиска с поддержкой фильтров
    @Headers("X-API-KEY: adcbe508-1726-4266-a036-2dc569f73c66")
    @GET("api/v2.2/films")
    suspend fun searchMovies(
        @Query("keyword") keyword: String?,
        @Query("genres") genreId: Int? = null, // Передаем один ID для реализации логики ИЛИ в ViewModel
        @Query("ratingFrom") ratingFrom: Double? = null,
        @Query("ratingTo") ratingTo: Double? = null,
        @Query("yearFrom") yearFrom: Int? = null,
        @Query("yearTo") yearTo: Int? = null,
        @Query("order") order: String = "RATING"
    ): KinopoiskResponse

    // ЗАПРОС НА ДЕТАЛИ (Описание, Жанры)
    @Headers("X-API-KEY: adcbe508-1726-4266-a036-2dc569f73c66")
    @GET("api/v2.2/films/{id}")
    suspend fun getMovieDetails(@Path("id") id: Int): KinopoiskFilmDetail

    // ЗАПРОС НА АКТЕРОВ
    @Headers("X-API-KEY: adcbe508-1726-4266-a036-2dc569f73c66")
    @GET("api/v1/staff")
    suspend fun getMovieStaff(@Query("filmId") filmId: Int): List<KinopoiskStaff>
}

// 2. Классы, описывающие JSON от Кинопоиска
data class KinopoiskResponse(val items: List<KinopoiskFilm>)

data class KinopoiskFilm(
    val kinopoiskId: Int,
    val nameRu: String?,
    val nameOriginal: String?,
    val posterUrl: String?,
    val year: Int?,
    val ratingKinopoisk: Double?
)

data class KinopoiskFilmDetail(val description: String?, val genres: List<KinopoiskGenre>?)
data class KinopoiskGenre(val genre: String)
data class KinopoiskStaff(val staffId: Int, val nameRu: String?, val posterUrl: String?, val professionKey: String)