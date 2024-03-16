package com.zebrand.app1food30s.data

import java.util.Date

data class Category(
    val id: String = "",
    val name: String = "",
    val image: String = "",
    val numProduct: Int = 0,
    val date: Date? = null
)
