package com.zebrand.app1food30s.data

import com.google.firebase.firestore.DocumentReference
import com.zebrand.app1food30s.data.Review
import java.util.Date

data class Product(
    val id: String = "",
    val idCategory: DocumentReference? = null,
    val idOffer: DocumentReference? = null,
    val name: String = "",
    val image: String = "", // Firebase Storage
    val price: Double = 0.0,
    val description: String = "",
    val stock: Int = 0,
    val sold: Int = 0,
    val reviews: List<Review> = emptyList(),
    val date: Date = Date() // Date the product is created
)