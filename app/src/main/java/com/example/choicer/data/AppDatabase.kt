package com.example.choicer.data

import androidx.room.Database
import androidx.room.RoomDatabase

// Версия изменена на 2, так как в модель Movie добавлены новые поля
@Database(entities = [Movie::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
}