package com.example.choicer.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Locale

enum class PosterSize(val pathSegment: String) {
    W500("w500"),
    ORIGINAL("original")
}

@Entity(tableName = "wishlist")
data class Movie(
    @PrimaryKey val id: Int,
    val title: String,
    val poster_path: String?,
    val overview: String,
    val release_date: String?,
    val vote_average: Double?,
    var isWatched: Boolean = false
) {
    // Вычисляемый рейтинг
    val formattedRating: String
        get() = if (vote_average == null || vote_average <= 0.0) "-"
        else String.format(Locale.US, "%.1f", vote_average)

    val releaseYear: String
        get() = release_date?.take(4).orEmpty()

    fun posterUrl(size: PosterSize = PosterSize.W500): String? {
        val posterPath = poster_path?.takeIf { it.isNotBlank() } ?: return null
        return if (posterPath.startsWith("http")) {
            posterPath
        } else {
            "https://image.tmdb.org/t/p/${size.pathSegment}$posterPath"
        }
    }
}
