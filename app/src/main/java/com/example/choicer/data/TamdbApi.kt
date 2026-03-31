package com.example.choicer.data

import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbApi {
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("language") lang: String = "ru-RU",
        @Query("page") page: Int = 1 // Добавлен параметр страницы
    ): MovieResponse

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("language") lang: String = "ru-RU"
    ): MovieResponse
}

data class MovieResponse(val results: List<Movie>)