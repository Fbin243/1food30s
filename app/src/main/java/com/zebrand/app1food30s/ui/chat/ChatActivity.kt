package com.zebrand.app1food30s.ui.chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.zebrand.app1food30s.adapter.ManageProductAdapter
import com.zebrand.app1food30s.adapter.MessageAdapter
import com.zebrand.app1food30s.data.entity.Category
import com.zebrand.app1food30s.data.entity.Chat
import com.zebrand.app1food30s.data.entity.Message
import com.zebrand.app1food30s.databinding.ActivityChatBinding
import com.zebrand.app1food30s.ui.edit_product.EditProduct
import com.zebrand.app1food30s.utils.FirebaseUtils.fireStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var messageAdapter: MessageAdapter
    private val messages = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        setupRecyclerView()
        handleDisplayMessages()
        binding.buttonSend.setOnClickListener {
            sendMessage()
        }
    }

    private fun handleDisplayMessages() {
        lifecycleScope.launch {
//            showShimmerEffectForProducts()
//            val db = AppDatabase.getInstance(applicationContext)
            Log.e("getListMessages", "Test 1 getting messages")
            val adapter = MessageAdapter(getListMessages("CaobLG7qUCxM10RxWZAi"))
            binding.recyclerViewChat.layoutManager = LinearLayoutManager(this@ChatActivity)
            binding.recyclerViewChat.adapter = adapter
//            hideShimmerEffectForProducts()
        }
    }

//    private fun setupRecyclerView() {
//        messageAdapter = MessageAdapter(messages)
//        binding.recyclerViewChat.apply {
//            layoutManager = LinearLayoutManager(this@ChatActivity)
//            adapter = messageAdapter
//        }
//    }

    private fun sendMessage() {
        val messageText = binding.editTextMessage.text.toString()
        if (messageText.isNotEmpty()) {
            val message = Message("senderId", "receiverId", messageText, Date())
            messages.add(message)
            messageAdapter.notifyItemInserted(messages.size - 1)
            binding.recyclerViewChat.scrollToPosition(messages.size - 1)
            binding.editTextMessage.text.clear()
        }
    }

    private suspend fun getListMessages(chatId: String): List<Message> {
        return withContext(Dispatchers.IO) {
            val fireStore = FirebaseFirestore.getInstance()
            try {
                // Query the chats collection for documents where idBuyer matches chatId
                val querySnapshot = fireStore.collection("chats")
                    .whereEqualTo("idBuyer", chatId)
                    .get()
                    .await()

                // Process the first document that matches the query
                if (querySnapshot.documents.isNotEmpty()) {
                    val document = querySnapshot.documents.first()
                    val chat = document.toObject(Chat::class.java)
                    chat?.messages ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e("getMessagesForBuyer", "Error querying messages for buyer", e)
                emptyList()
            }
        }
    }

}
