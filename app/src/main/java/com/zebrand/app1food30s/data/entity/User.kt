package com.zebrand.app1food30s.data.entity

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class User(
    var id: String? = null,
    var firstName: String = "",
    var lastName: String = "",
    var email: String = "",
    var avatar: String = "",
    var phone: String = "",
    var address: String = "",
    var admin: Boolean,
    @get:ServerTimestamp
    var date: Date? = null,
    var wishlistRef: DocumentReference? = null,
    var cartRef: DocumentReference? = null,
){
    constructor() : this(
        id = null,
        admin = false
    )
}
