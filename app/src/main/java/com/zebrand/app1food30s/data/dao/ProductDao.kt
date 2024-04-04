package com.zebrand.app1food30s.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zebrand.app1food30s.data.entity.Product

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY date DESC")
    fun getAll(): List<Product>

    @Query("SELECT * FROM products WHERE id = :id")
    fun getOneById(id: String): Product

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(category: Product)

    @Update
    fun update(category: Product)

    @Delete
    fun delete(category: Product)

    @Query("DELETE FROM products")
    fun deleteAll()

    @Query("SELECT * FROM products WHERE idCategory = :categoryId")
    fun getByCategory(categoryId: String): List<Product>

    @Query("SELECT * FROM products WHERE name LIKE '%' || :name || '%'")
    fun searchByName(name: String): List<Product>
}