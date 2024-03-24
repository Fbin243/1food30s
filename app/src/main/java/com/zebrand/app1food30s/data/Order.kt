package com.zebrand.app1food30s.data

import com.google.firebase.firestore.DocumentReference
import java.util.Date
import com.google.firebase.firestore.ServerTimestamp

data class OrderItem(
    val productId: DocumentReference? = null,
    val category: String = "",
    val discount: Double = 0.0,
    val name: String = "",
    val image: String = "", // Firebase Storage
    val price: Double = 0.0,
    val quantity: Int = 0,
    val isReviewed: Boolean = false,
)

data class Order(
    val id: String = "",
    val accountId: DocumentReference? = null,
    val items: MutableList<OrderItem> = mutableListOf(),
    val totalAmount: Double = 0.0,
    val orderStatus: String = "",
    val cancelReason: String = "",
    @get:ServerTimestamp
    val date: Date? = null
)

//data class Product(
//    val id: String = "",
//    val idCategory: DocumentReference? = null,
//    val idOffer: DocumentReference? = null,
//    val name: String = "",
//    val image: String = "", // Firebase Storage
//    val price: Double = 0.0,
//    val description: String = "",
//    val stock: Int = 0,
//    val sold: Int = 0,
//    val reviews: List<Review>? = emptyList(),
//    val date: Date? = Date() // Date the product is created
//)


