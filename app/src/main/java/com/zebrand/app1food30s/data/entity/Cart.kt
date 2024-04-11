package com.zebrand.app1food30s.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentReference

@Entity(tableName = "carts")
data class CartEntity(
    @PrimaryKey(autoGenerate = false)
//    var id: String,
    var userId: String
)

@Entity(
    tableName = "cart_items",
    foreignKeys = [
        ForeignKey(
            entity = CartEntity::class,
            parentColumns = ["userId"], // This should match the primary key column name of CartEntity
            childColumns = ["cartUserId"], // This should match the foreign key column name in CartItemEntity
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["cartUserId"])] // Index for faster lookup
)
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true)
    val itemId: Long = 0L,
    val cartUserId: String, // Name it to clearly indicate it's a foreign key to CartEntity's userId
    val productId: String,
    val productCategory: String,
    val productName: String,
    val productPrice: Double,
    val productImage: String,
    val productStock: Int,
    var quantity: Int
)

// TODO
data class Cart(
    var id: String = "",
    var userId: DocumentReference? = null,
    var items: MutableList<CartItem> = mutableListOf()
)

data class CartItem(
    val productId: DocumentReference? = null, // Adjusted as per your previous structure; consider using String if dealing with Firestore document IDs
    val productCategory: String = "",
    val productName: String = "",
    val productPrice: Double = 0.0, // Price after discount
    val oldPrice: Double = 0.0, // Original price before discount
    val productImage: String = "",
    val productStock: Int = 0,
    var quantity: Int = 0,
)

//data class CartItem(
//    val productId: DocumentReference? = null,
//    var quantity: Int = 0,
//)
