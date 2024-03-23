package com.zebrand.app1food30s.data

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class User(
    @get:Exclude
    var id: String? = null,
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val avatar: String = "",
    val phone: String = "",
    val address: String = "",
    val isAdmin: Boolean = false,
    @get:Exclude
    val cart: Cart? = null,
    @get:ServerTimestamp
    val date: Date? = null
)
