package com.zebrand.app1food30s.data.entity

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
    val idAccount: DocumentReference? = null,
    val items: MutableList<OrderItem> = mutableListOf(),
    val totalAmount: Double = 0.0,
    val orderStatus: String = "",
    val cancelReason: String? = "",
    val shippingAddress: String = "",
    val paymentStatus: String = "",
    val note: String = "",
    @get:ServerTimestamp
    val date: Date = Date()
)