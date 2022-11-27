package com.example.diary.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TodoDao {
    @Query("SELECT * FROM todo")
    fun getAll(): List<Todo>

    @Query("SELECT * FROM todo WHERE id IN (:todoIds)")
    fun loadAllByIds(todoIds: IntArray): List<Todo>

    @Query("SELECT * FROM todo WHERE name LIKE :name LIMIT 1")
    fun findByName(name: String): Todo


    @Insert
    fun insertAll(vararg todoLists: Todo)

    @Insert
    fun insert(todo: Todo)

    @Delete
    fun delete(user: Todo)

}