package com.example.choicer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.example.choicer.data.*
import com.example.choicer.uiu.MainApp
import com.example.choicer.viewmodel.MovieViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = androidx.room.Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "movie-database"
        ).fallbackToDestructiveMigration().build()
        val dao = db.movieDao()

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        val clipRepository = ClipRepository(okHttpClient)

        val tmdbRetrofit = Retrofit.Builder()
            .baseUrl("https://tmdb.cub.red/3/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val tmdbApi = tmdbRetrofit.create(TmdbApi::class.java)

        val kinopoiskRetrofit = Retrofit.Builder()
            .baseUrl("https://kinopoiskapiunofficial.tech/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val kinopoiskApi = kinopoiskRetrofit.create(KinopoiskApi::class.java)

        setContent {
            val viewModel: MovieViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return MovieViewModel(tmdbApi, kinopoiskApi, dao, clipRepository) as T
                    }
                }
            )
            MainApp(viewModel = viewModel)
        }
    }
}