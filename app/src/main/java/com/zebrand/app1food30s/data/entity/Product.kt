package com.zebrand.app1food30s.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Exclude
import java.util.Date

@Entity(tableName = "products")
data class Product(
    @PrimaryKey
    var id: String = "",
    var idCategory: DocumentReference? = null,
    var idOffer: DocumentReference? = null,
    var name: String = "",
    var image: String = "", // Firebase Storage
    var price: Double = 0.0,
    var description: String = "",
    var stock: Int = 0,
    var sold: Int = 0,
    var date: Date? = Date(), // Date the product is created
    @Exclude
    var isGrid: Boolean = false
)
