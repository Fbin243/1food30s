package com.zebrand.app1food30s.data

import com.google.firebase.firestore.DocumentReference
import java.util.Date

data class Product(
    val id: String,
    val idCategory: DocumentReference?,
    val idOffer: DocumentReference?,
    val name: String,
    val image: String,
    val price: Double,
    val description: String,
    val stock: Int,
    val sold: Int = 0,
    val reviews: List<Review>?,
    val date: Date?
)