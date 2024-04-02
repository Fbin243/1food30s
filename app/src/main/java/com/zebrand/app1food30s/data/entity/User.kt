package com.zebrand.app1food30s.data.entity

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class User(
    var id: String? = null,
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val avatar: String = "",
    val phone: String = "",
    val address: String = "",
    val isAdmin: Boolean = false,
    @get:ServerTimestamp
    val date: Date? = null,
    val wishlistRef: DocumentReference? = null,
    val cartRef: DocumentReference? = null,
)
