package com.zebrand.app1food30s.data

import java.util.Date

data class Offer(
    val id: String,
    val name: String,
    val image: String,
    val numProduct: Number,
    val discountRate: Int,
    val date: Date?
)