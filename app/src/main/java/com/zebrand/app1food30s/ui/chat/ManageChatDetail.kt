package com.zebrand.app1food30s.ui.chat

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.zebrand.app1food30s.adapter.MessageAdapter
import com.zebrand.app1food30s.data.entity.Chat
import com.zebrand.app1food30s.data.entity.Message
import com.zebrand.app1food30s.databinding.ActivityManageChatDetailBinding
import com.zebrand.app1food30s.utils.FirebaseUtils.fireStorage
import com.zebrand.app1food30s.utils.MySharedPreferences
import com.zebrand.app1food30s.utils.SingletonKey
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ManageChatDetail : AppCompatActivity() {
    private lateinit var binding: ActivityManageChatDetailBinding
    private lateinit var messageAdapter: MessageAdapter
    private val messages = mutableListOf<Message>()
    private var currentUserId: String? = null
    private lateinit var mySharedPreferences: MySharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageChatDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val chatId = intent.getStringExtra("chatId")
        currentUserId = chatId
        setupRecyclerView()
        currentUserId?.let { fetchChatDetails(it) }
//        val chatsCollection = FirebaseFirestore.getInstance().collection("chats")
//        chatsCollection.whereEqualTo("idBuyer", chatId).get().addOnSuccessListener { document ->
//            val avaSender = document.getString("avaBuyer") ?: "images/avatars/avaeb015d1a-8e43-4baf-aaf6-4639eb258f5e.png"
//            Picasso.get().load(avaSender).into(binding.imageBuyer)
//            binding.tvBuyerName.text = document.getString("nameBuyer")
//        }.addOnFailureListener {
//            Log.e("ChatActivity", "Failed to retrieve user information", it)
//        }

        currentUserId?.let { handleDisplayMessages(it) }
        binding.buttonSend.setOnClickListener {
            val messageText = binding.editTextMessage.text.toString()
            if (messageText.isNotEmpty()) {
                binding.editTextMessage.text.clear()
                currentUserId?.let { sendMessageToFirestore(it, messageText) }
            }
        }
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(messages, "zErR5nXOOmmqrz1YR5V7") // Chuyển ID người dùng hiện tại tới adapter
        binding.recyclerViewChat.apply {
            layoutManager = LinearLayoutManager(this@ManageChatDetail)
            adapter = messageAdapter
        }
    }

    private fun fetchChatDetails(chatId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("chats").document(chatId).get().addOnSuccessListener { document ->
            if (document.exists()) {
//                val chat = document.toObject(Chat::class.java)
                val avaUrl = document.getString("avaBuyer") ?: ""
                val nameBuyer = document.getString("nameBuyer") ?: ""
                Picasso.get().load(avaUrl).into(binding.imageBuyer)
                binding.tvBuyerName.text = nameBuyer
            } else {
                Log.e("ChatActivity", "Chat not found")
            }
        }.addOnFailureListener {
            Log.e("ChatActivity", "Failed to retrieve chat details", it)
        }
    }

    private fun handleDisplayMessages(chatId: String) {
        val chatsCollection = FirebaseFirestore.getInstance().collection("chats")
        val chatQuery = chatsCollection.whereEqualTo("idBuyer", chatId)
        chatQuery.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("ChatActivity", "Listen failed.", e)
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                val document = snapshot.documents.first()
                val updatedChat = document.toObject(Chat::class.java)
                updatedChat?.messages?.let {
                    messages.clear()
                    messages.addAll(it)
                    messageAdapter.notifyDataSetChanged()
                    binding.recyclerViewChat.scrollToPosition(messages.size - 1)
                }
            }
        }
    }

    private fun sendMessageToFirestore(chatId: String, messageText: String) {
        val chatsCollection = FirebaseFirestore.getInstance().collection("chats")
        lifecycleScope.launch {
            try {
                val querySnapshot = chatsCollection.whereEqualTo("idBuyer", chatId).get().await()
                if (querySnapshot.documents.isNotEmpty()) {
                    val document = querySnapshot.documents.first() // Assuming 'idBuyer' is unique and expecting only one document
                    val message = currentUserId?.let { Message("zErR5nXOOmmqrz1YR5V7", it, messageText) }
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
