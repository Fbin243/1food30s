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
import java.util.Date

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
        currentUserId?.let {
            fetchChatDetails(it)
            markChatAsRead(it)
        }
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
        db.collection("chats")
            .whereEqualTo("idBuyer", chatId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    for (document in querySnapshot.documents) {
                        val chat = document.toObject(Chat::class.java)
                        Picasso.get().load(chat?.avaBuyer).into(binding.imageBuyer)
                        binding.tvBuyerName.text = chat?.nameBuyer
                        // Possibly break after the first result if you only need one
                        break
                    }
                } else {
                    Log.e("ChatActivity", "No chats found with idBuyer: $chatId")
                }
            }
            .addOnFailureListener { e ->
                Log.e("ChatActivity", "Failed to retrieve chat details", e)
            }
    }


    private fun markChatAsRead(chatId: String) {
        val chatsCollection = FirebaseFirestore.getInstance().collection("chats")
        val chatQuery = chatsCollection.whereEqualTo("idBuyer", chatId).limit(1)
        chatQuery.get().addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                val document = querySnapshot.documents.first()
                document.reference.update("seen", true)
                    .addOnSuccessListener {
                        Log.d("ChatActivity", "Chat marked as read successfully.")
                    }
                    .addOnFailureListener { e ->
                        Log.e("ChatActivity", "Failed to mark chat as read.", e)
                    }
            }
        }.addOnFailureListener { e ->
            Log.e("ChatActivity", "Error fetching chat document.", e)
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
//                if (updatedChat?.seen == false) {
//                    document.reference.update("seen", true)
//                        .addOnSuccessListener {
//                            Log.d("ChatActivity", "Chat marked as read successfully.")
//                        }
//                        .addOnFailureListener { ex ->
//                            Log.e("ChatActivity", "Failed to mark chat as read.", ex)
//                        }
//                }
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
//                    document.reference.update("messages", FieldValue.arrayUnion(message))
//                        .addOnSuccessListener {
//                            Log.d("ChatActivity", "Message successfully added to Firestore")
//                        }
//                        .addOnFailureListener { e ->
//                            Log.e("ChatActivity", "Failed to send message", e)
//                        }
                    val chatUpdateMap = hashMapOf<String, Any>(
                        "messages" to FieldValue.arrayUnion(message),
                        "date" to Date()  // Cập nhật thời gian hiện tại cho chat
                    )
                    document.reference.update(chatUpdateMap)
                        .addOnSuccessListener {
                            Log.d("ChatActivity", "Message and date successfully updated in Firestore")
                        }
                        .addOnFailureListener { e ->
                            Log.e("ChatActivity", "Failed to update message and date", e)
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
