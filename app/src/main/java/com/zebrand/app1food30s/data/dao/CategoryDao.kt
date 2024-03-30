package com.zebrand.app1food30s.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.zebrand.app1food30s.data.entity.Category

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY date DESC")
    fun getAll(): List<Category>
    @Insert
    fun insert(category: Category)
    @Update
    fun update(category: Category)
    @Delete
    fun delete(category: Category)
}