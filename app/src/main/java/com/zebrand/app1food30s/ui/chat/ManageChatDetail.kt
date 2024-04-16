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
import com.zebrand.app1food30s.databinding.ActivityManageChatDetailBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ManageChatDetail : AppCompatActivity() {
    private lateinit var binding: ActivityManageChatDetailBinding
    private lateinit var messageAdapter: MessageAdapter
    private val messages = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageChatDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        handleDisplayMessages("CaobLG7qUCxM10RxWZAi")
        binding.buttonSend.setOnClickListener {
            val messageText = binding.editTextMessage.text.toString()
            if (messageText.isNotEmpty()) {
                val message = Message("zErR5nXOOmmqrz1YR5V7", "CaobLG7qUCxM10RxWZAi", messageText)  // Adjust IDs as needed
                binding.editTextMessage.text.clear()
                sendMessageToFirestore("CaobLG7qUCxM10RxWZAi", message)
            }
        }
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(messages)
        binding.recyclerViewChat.apply {
            layoutManager = LinearLayoutManager(this@ManageChatDetail)
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

    private fun sendMessageToFirestore(chatId: String, message: Message) {
        val chatsCollection = FirebaseFirestore.getInstance().collection("chats")
        lifecycleScope.launch {
            try {
                val querySnapshot = chatsCollection.whereEqualTo("idBuyer", chatId).get().await()

                if (querySnapshot.documents.isNotEmpty()) {
                    val document = querySnapshot.documents.first() // Assuming 'idBuyer' is unique and expecting only one document
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
