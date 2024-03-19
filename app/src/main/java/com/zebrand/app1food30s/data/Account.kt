package com.zebrand.app1food30s.data

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Account(
    @Exclude
    var id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val avatar: String,
    val phone: String,
    val address: String,
    val isAdmin: Boolean,
    val cartRef: DocumentReference,
    @Exclude
    val cart: Cart,
    @ServerTimestamp
    val date: Date
)
