package com.example.choicer.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Locale

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
}