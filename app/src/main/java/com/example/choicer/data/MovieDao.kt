package com.example.choicer.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Query("SELECT * FROM wishlist")
    fun getAllMovies(): Flow<List<Movie>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMovie(movie: Movie): Long // Без suspend, но с Long

    @Update
    fun updateMovie(movie: Movie): Int  // Без suspend, но с Int

    @Delete
    fun deleteMovie(movie: Movie): Int  // Без suspend, но с Int
}