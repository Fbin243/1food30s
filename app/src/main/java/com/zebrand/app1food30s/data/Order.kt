package com.zebrand.app1food30s.data

import com.google.firebase.firestore.DocumentReference
import java.util.Date

data class Order(
    val id: String = "",
    val idAccount: DocumentReference? = null,
    val items: MutableList<OrderItem> = mutableListOf(),
    val totalAmount: Double = 0.0,
    val orderStatus: String = "",
    val cancelReason: String? = null, // Nullable, since it won't be set for orders that aren't cancelled
    val shippingAddress: String = "",
    val paymentStatus: String = "",
    val note: String = "",
    val date: Date = Date(),
)

data class OrderItem(
    val productId: DocumentReference,
    val category: String = "",
    val discount: Double = 0.0,
    val name: String = "",
    var image: String = "", // Firebase Storage
    val price: Double = 0.0,
    val quantity: Int,
    val isReviewed: Boolean = false // Defaulting to false, assuming products are not reviewed when the order is created
)