package com.zebrand.app1food30s.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zebrand.app1food30s.data.entity.Offer
import com.zebrand.app1food30s.data.entity.Product

@Dao
interface OfferDao {
    @Query("SELECT * FROM offers ORDER BY date DESC")
    fun getAll(): List<Offer>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(category: Offer)
    @Update
    fun update(category: Offer)
    @Delete
    fun delete(category: Offer)
    @Query("DELETE FROM offers")
    fun deleteAll()
}