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
    val admin: Boolean,
    @get:ServerTimestamp
    val date: Date? = null,
    var wishlistRef: DocumentReference? = null,
    var cartRef: DocumentReference? = null,
){
    constructor() : this(
        id = null,
        admin = false
    )
}
