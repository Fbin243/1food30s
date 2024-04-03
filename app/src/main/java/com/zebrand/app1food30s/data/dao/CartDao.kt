package com.zebrand.app1food30s.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zebrand.app1food30s.data.entity.CartEntity
import com.zebrand.app1food30s.data.entity.CartItemEntity

@Dao
interface CartDao {
    @Query("SELECT * FROM carts WHERE userId = :userId")
    suspend fun getCartByUserId(userId: String): CartEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCart(cart: CartEntity)
}

@Dao
interface CartItemDao {
    @Query("SELECT * FROM cart_items WHERE cartUserId = :userId")
    suspend fun getCartItemsForUser(userId: String): List<CartItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE itemId = :itemId")
    suspend fun deleteCartItem(itemId: Long)
}
