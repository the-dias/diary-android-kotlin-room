package com.example.diary.database

import androidx.room.RoomDatabase

@androidx.room.Database(entities = [Todo::class], version = 3)
abstract class Database : RoomDatabase() {
    abstract fun todoDao(): TodoDao
}