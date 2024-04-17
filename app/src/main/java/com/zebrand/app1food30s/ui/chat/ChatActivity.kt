package com.zebrand.app1food30s.ui.chat

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.zebrand.app1food30s.adapter.MessageAdapter
import com.zebrand.app1food30s.data.entity.Chat
import com.zebrand.app1food30s.data.entity.Message
import com.zebrand.app1food30s.databinding.ActivityChatBinding
import com.zebrand.app1food30s.utils.FirebaseUtils.fireStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var messageAdapter: MessageAdapter
    private val messages = mutableListOf<Message>()
    private val currentUserId = "CaobLG7qUCxM10RxWZAi"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        handleDisplayMessages("CaobLG7qUCxM10RxWZAi")
        binding.buttonSend.setOnClickListener {
            val messageText = binding.editTextMessage.text.toString()
            if (messageText.isNotEmpty()) {
//                val message = Message("CaobLG7qUCxM10RxWZAi", "zErR5nXOOmmqrz1YR5V7", messageText)  // Adjust IDs as needed
                binding.editTextMessage.text.clear()
                sendMessageToFirestore("CaobLG7qUCxM10RxWZAi", messageText)
            }
        }
    }

//    private fun setupRecyclerView() {
//        messageAdapter = MessageAdapter(messages)
//        binding.recyclerViewChat.apply {
//            layoutManager = LinearLayoutManager(this@ChatActivity)
//            adapter = messageAdapter
//        }
//    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(messages, currentUserId) // Chuyển ID người dùng hiện tại tới adapter
        binding.recyclerViewChat.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = messageAdapter
        }
    }

    private fun handleDisplayMessages(chatId: String) {
        val chatsCollection = FirebaseFirestore.getInstance().collection("chats")
        lifecycleScope.launch {
            try {
                val querySnapshot = chatsCollection.whereEqualTo("idBuyer", chatId).get().await()

                if (querySnapshot.documents.isNotEmpty()) {
                    val document = querySnapshot.documents.first() // Assuming 'idBuyer' is unique and expecting only one document

                    document.reference.addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Log.e("ChatActivity", "Listen failed.", e)
                            return@addSnapshotListener
                        }

                        if (snapshot != null && snapshot.exists()) {
                            Log.d("ChatActivity", "Current data: ${snapshot.data}")
                            val updatedChat = snapshot.toObject(Chat::class.java)
                            updatedChat?.messages?.let {
                                messages.clear()
                                messages.addAll(it)
                                messageAdapter.notifyDataSetChanged()
                                binding.recyclerViewChat.scrollToPosition(messages.size - 1)
                            }
                        } else {
                            Log.d("ChatActivity", "Current data: null")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatActivity", "Error setting up real-time updates", e)
            }
        }
    }

    private fun sendMessageToFirestore(chatId: String, messageText: String) {
//        var avaSender = "images/avatars/avaeb015d1a-8e43-4baf-aaf6-4639eb258f5e.png"
        var nameSender = "Admin"
        val accountsCollection = FirebaseFirestore.getInstance().collection("accounts")
        accountsCollection.document("zErR5nXOOmmqrz1YR5V7").get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
//                avaSender = document.getString("avatar") ?: "images/avatars/avaeb015d1a-8e43-4baf-aaf6-4639eb258f5e.png"
                nameSender = document.getString("firstName") ?: "Admin"
            } else {
                nameSender = "Admin"
            }
        }.addOnFailureListener {
            nameSender = "Admin"
        }
        val chatsCollection = FirebaseFirestore.getInstance().collection("chats")
        lifecycleScope.launch {
            try {
                val querySnapshot = chatsCollection.whereEqualTo("idBuyer", chatId).get().await()
//                val image = document.getString("image") ?: "images/product/product3.png"
//                val imageUrl = fireStorage.reference.child(avaSender).downloadUrl.await().toString()

                if (querySnapshot.documents.isNotEmpty()) {
                    val document = querySnapshot.documents.first() // Assuming 'idBuyer' is unique and expecting only one document
                    val message = Message("CaobLG7qUCxM10RxWZAi", "zErR5nXOOmmqrz1YR5V7", messageText, nameSender)
                    document.reference.update("messages", FieldValue.arrayUnion(message))
                        .addOnSuccessListener {
                            Log.d("ChatActivity", "Message successfully added to Firestore")
                        }
                        .addOnFailureListener { e ->
                            Log.e("ChatActivity", "Failed to send message", e)
                        }
                } else {
                    Log.e("ChatActivity", "No chat document found for idBuyer: $chatId")
                }
            } catch (e: Exception) {
                Log.e("ChatActivity", "Error sending message to Firestore", e)
            }
        }
    }
}
