package com.zebrand.app1food30s.data

import java.util.Date

data class Offer(
    val id: String = "",
    val name: String = "",
    val image: String = "",
    val discountRate: Int = 0,
    val numProduct: Int = 0,
    val date: Date? = null
)
