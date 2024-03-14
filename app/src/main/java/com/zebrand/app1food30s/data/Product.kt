package com.zebrand.app1food30s.data

import java.util.Date

//class Product(var img: Int, var title: String, var description: String, var price: Double)

data class Product(
    val id: String,
    val idCategory: String?,
    val idOffer: String?,
    val name: String,
    val image: String, // Assuming you're using resource IDs for sample images
    val price: Double,
    val description: String,
    val stock: Int,
    val sold: Int = 0,
    val reviews: List<Review>?,
    val date: Date? // Date the product is created
)