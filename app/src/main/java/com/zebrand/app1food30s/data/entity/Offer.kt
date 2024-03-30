package com.zebrand.app1food30s.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "offers")
data class Offer(
    @PrimaryKey
    var id: String = "",
    var name: String = "",
    var image: String = "",
    var discountRate: Int = 0,
    var numProduct: Int = 0,
    var date: Date? = null
)
