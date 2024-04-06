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
    var id: String = "",
    var idAccount: DocumentReference? = null,
    var items: MutableList<OrderItem> = mutableListOf(),
    var totalAmount: Double = 0.0,
    var orderStatus: String = "",
    //    User: Pending - Order accepted - On delivery - Delivered
    //Admin: Accept (Pending -> Order accepted), On delivery, Delivered
    var cancelReason: String? = "",
    var shippingAddress: String = "",
    var paymentStatus: String = "",
    var note: String = "",
    @get:ServerTimestamp
    var date: Date = Date()
)