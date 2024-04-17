package com.zebrand.app1food30s.data.entity

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Exclude
import java.util.Date

data class Chat(
    val idBuyer: String = "",
    val messages: List<Message> = listOf()
)

data class Message(
    val idSender: String = "",
    val idReceiver: String = "",
    val messageString: String = "",
    val nameSender: String = "",
    var date: Date = Date()
)

