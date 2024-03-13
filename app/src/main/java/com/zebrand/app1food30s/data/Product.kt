package com.zebrand.app1food30s.data

import java.util.Date

//class Product(var img: Int, var title: String, var description: String, var price: Double)

data class Product(
    val id: String = "",
    val idCategory: String = "",
    val idOffer: String = "",
    val name: String = "",
    val image: String = "", // Firebase Storage
    val price: Double = 0.0,
    val description: String = "",
    val stock: Int = 0,
    val sold: Int = 0,
    val reviews: List<Review> = emptyList(),
    val date: Date = Date() // Date the product is created
)