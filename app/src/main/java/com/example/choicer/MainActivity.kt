package com.example.choicer // ТВОЙ ПАКЕТ ОСТАВЬ ЗДЕСЬ

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.choicer.data.TmdbApi
import com.example.choicer.uiu.MainApp
import com.example.choicer.viewmodel.MovieViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
// ВОТ ЭТИ ИМПОРТЫ НУЖНО ДОБАВИТЬ ВВЕРХ ФАЙЛА MainActivity.kt:
import coil.Coil
import coil.ImageLoader
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Инициализация базы данных Room (Вот тут живут db и dao!)
        val db = androidx.room.Room.databaseBuilder(
            applicationContext,
            com.example.choicer.data.AppDatabase::class.java,
            "movie-database"
        ).build()
        val dao = db.movieDao()

        // 2. Инициализация TMDB API
        val tmdbRetrofit = retrofit2.Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
        val tmdbApi = tmdbRetrofit.create(com.example.choicer.data.TmdbApi::class.java)

        // 3. Инициализация Kinopoisk API
        val kinopoiskRetrofit = retrofit2.Retrofit.Builder()
            .baseUrl("https://kinopoiskapiunofficial.tech/")
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
        val kinopoiskApi = kinopoiskRetrofit.create(com.example.choicer.data.KinopoiskApi::class.java)

        // 4. Запуск интерфейса
        setContent {
            // Создаем ViewModel и передаем в нее все три инструмента
            val viewModel: com.example.choicer.viewmodel.MovieViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        // ВНИМАНИЕ: передаем tmdbApi, kinopoiskApi и dao через запятую
                        return com.example.choicer.viewmodel.MovieViewModel(tmdbApi, kinopoiskApi, dao) as T
                    }
                }
            )

            // Вызываем наш главный экран с навигацией
            // Вызываем наш главный экран
            MainApp(viewModel = viewModel)
        }
    }
}