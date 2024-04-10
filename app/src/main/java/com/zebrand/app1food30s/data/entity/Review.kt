package com.zebrand.app1food30s.data.entity

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Exclude
import java.util.Date

data class Review(
    var idProduct: DocumentReference? = null,
    var idAccount: DocumentReference? = null,
    var rating: Int = 0,
    var content: String = "",
    var date: Date = Date(),
    @Exclude
    var avatar: String? = "",
    @Exclude
    var name: String? = ""
)