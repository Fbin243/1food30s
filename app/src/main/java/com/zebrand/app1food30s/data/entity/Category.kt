package com.zebrand.app1food30s.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "categories")

data class Category(
    @PrimaryKey
    var id: String = "",
    var name: String = "",
    var image: String = "",
    var numProduct: Int = 0,
    var date: Date? = null
)
