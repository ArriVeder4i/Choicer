package com.example.choicer.data

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class ClipRepository(private val client: OkHttpClient) {

    private val clipsCache = mutableMapOf<Int, String>() // movieId -> driveId
    private val gson = Gson()

    // ТВОЯ ИСПРАВЛЕННАЯ RAW ССЫЛКА
    private val jsonUrl = "https://raw.githubusercontent.com/ArriVeder4i/Choicer/master/clips.json"

    suspend fun refreshClips() = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(jsonUrl)
                .header("Cache-Control", "no-cache") // Чтобы изменения в GitHub подхватывались сразу
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("ClipRepo", "Ошибка загрузки JSON: ${response.code}")
                    return@withContext
                }

                val jsonString = response.body?.string() ?: return@withContext
                Log.d("ClipRepo", "JSON получен: $jsonString")

                val remoteData = gson.fromJson(jsonString, RemoteClipResponse::class.java)

                clipsCache.clear()
                remoteData.movies.forEach { container ->
                    container.clips.firstOrNull()?.let { clip ->
                        clipsCache[container.movieId] = clip.driveId
                    }
                }
                Log.d("ClipRepo", "База обновлена: загружено ${clipsCache.size} клипов")
            }
        } catch (e: Exception) {
            Log.e("ClipRepo", "Критическая ошибка при синхронизации JSON", e)
        }
    }

    fun hasClip(movieId: Int): Boolean = clipsCache.containsKey(movieId)

    fun getClipUrl(movieId: Int): String? {
        val driveId = clipsCache[movieId]
        return driveId?.let { "https://drive.google.com/uc?export=download&id=$it" }
    }
}
