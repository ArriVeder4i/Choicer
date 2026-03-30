package com.example.choicer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wishlist")
data class Movie(
    @PrimaryKey val id: Int,
    val title: String,
    val poster_path: String?,
    val overview: String,
    val release_date: String?, // Дата выхода (гггг-мм-дд)
    val vote_average: Double?, // Рейтинг (например, 8.5)
    var isWatched: Boolean = false // На будущее для вишлиста
)