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
import com.zebrand.app1food30s.utils.MySharedPreferences
import com.zebrand.app1food30s.utils.SingletonKey
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ManageChatDetail : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var messageAdapter: MessageAdapter
    private val messages = mutableListOf<Message>()
    private var currentUserId: String? = null
    private lateinit var mySharedPreferences: MySharedPreferences
//    idUser = mySharedPreferences.getString(SingletonKey.KEY_USER_ID)
//    private val currentUserId = "CaobLG7qUCxM10RxWZAi"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val chatId = intent.getStringExtra("chatId")
        currentUserId = chatId
//        mySharedPreferences = MySharedPreferences.getInstance(this)
//        currentUserId = mySharedPreferences.getString(SingletonKey.KEY_USER_ID)
        setupRecyclerView()
//        Log.d("ChatActivity", "current id user: ${currentUserId}")
//        handleDisplayMessages("CaobLG7qUCxM10RxWZAi")
        currentUserId?.let { handleDisplayMessages(it) }
        binding.buttonSend.setOnClickListener {
            val messageText = binding.editTextMessage.text.toString()
            if (messageText.isNotEmpty()) {
//                val message = Message("CaobLG7qUCxM10RxWZAi", "zErR5nXOOmmqrz1YR5V7", messageText)  // Adjust IDs as needed
                binding.editTextMessage.text.clear()
//                sendMessageToFirestore("CaobLG7qUCxM10RxWZAi", messageText)
                currentUserId?.let { sendMessageToFirestore(it, messageText) }
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
        messageAdapter = MessageAdapter(messages, "zErR5nXOOmmqrz1YR5V7") // Chuyển ID người dùng hiện tại tới adapter
        binding.recyclerViewChat.apply {
            layoutManager = LinearLayoutManager(this@ManageChatDetail)
            adapter = messageAdapter
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
//                val image = document.getString("image") ?: "images/product/product3.png"
//                val imageUrl = fireStorage.reference.child(avaSender).downloadUrl.await().toString()

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
