package com.zebrand.app1food30s.data.entity

import com.google.firebase.firestore.DocumentReference
import java.util.Date

data class Review(
    var idAccount: DocumentReference? = null,
    var rating: Number = 0,
    var content: String = "",
    var date: Date = Date(),
    var avatar: String = ""
)