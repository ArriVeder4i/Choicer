package com.example.choicer.data

import androidx.room.Database
import androidx.room.RoomDatabase

// Указываем, какие таблицы есть в базе. Версия 1 — потому что это первая версия базы.
@Database(entities = [Movie::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
}