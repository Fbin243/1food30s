package com.zebrand.app1food30s.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.zebrand.app1food30s.data.entity.Category
import com.zebrand.app1food30s.data.entity.Product

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY date DESC")
    fun getAll(): List<Product>
    @Insert
    fun insert(category: Product)
    @Update
    fun update(category: Product)
    @Delete
    fun delete(category: Product)
    @Query("DELETE FROM products")
    fun deleteAll()
}