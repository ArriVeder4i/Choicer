package com.example.choicer.data

import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbApi {
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("language") lang: String = "ru-RU"
    ): MovieResponse

    // ДОБАВИЛИ ФУНКЦИЮ ПОИСКА СЮДА
    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String, // Слово для поиска
        @Query("language") lang: String = "ru-RU"
    ): MovieResponse
}

data class MovieResponse(val results: List<Movie>)