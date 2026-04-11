package com.example.choicer.data

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApi {
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("language") lang: String = "ru-RU",
        @Query("page") page: Int = 1
    ): MovieResponse

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("language") lang: String = "ru-RU"
    ): MovieResponse

    // Запрос трейлеров напрямую из TMDB
    @GET("movie/{movie_id}/videos")
    suspend fun getMovieVideos(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") lang: String = "ru-RU"
    ): VideoResponse
}

data class MovieResponse(val results: List<Movie>)

data class VideoResponse(val results: List<TmdbVideo>)
data class TmdbVideo(
    val key: String,    // YouTube ID
    val site: String,   // "YouTube"
    val type: String    // "Trailer"
)