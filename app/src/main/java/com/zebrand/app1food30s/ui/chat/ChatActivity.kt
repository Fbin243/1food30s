package com.zebrand.app1food30s.ui.chat

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.MessageAdapter
import com.zebrand.app1food30s.data.entity.Chat
import com.zebrand.app1food30s.data.entity.Message
import com.zebrand.app1food30s.databinding.ActivityChatBinding
import com.zebrand.app1food30s.utils.FirebaseUtils.fireStorage
import com.zebrand.app1food30s.utils.MySharedPreferences
import com.zebrand.app1food30s.utils.SingletonKey
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

class ChatActivity : AppCompatActivity() {
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

        mySharedPreferences = MySharedPreferences.getInstance(this)
        currentUserId = mySharedPreferences.getString(SingletonKey.KEY_USER_ID)
        setupRecyclerView()
        currentUserId?.let {
            markChatAsRead(it)
        }
//        Log.d("ChatActivity", "current id user: ${currentUserId}")
//        handleDisplayMessages("CaobLG7qUCxM10RxWZAi")
        currentUserId?.let { handleDisplayMessages(it) }
//        currentUserId?.let {
//            handleDisplayMessages(it)
//            markChatAsRead(it)  // Mark the chat as read upon opening the activity
//        }

        val backIcon = findViewById<View>(R.id.backIcon)
        backIcon.setOnClickListener {
            finish() // Kết thúc Activity hiện tại
        }

        binding.buttonSend.setOnClickListener {
            val messageText = binding.editTextMessage.text.toString()
            if (messageText.isNotEmpty()) {
//                val message = Message("CaobLG7qUCxM10RxWZAi", "zErR5nXOOmmqrz1YR5V7", messageText)  // Adjust IDs as needed
                binding.editTextMessage.text?.clear()
//                sendMessageToFirestore("CaobLG7qUCxM10RxWZAi", messageText)
                currentUserId?.let { sendMessageToFirestore(it, messageText) }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        currentUserId?.let {
            markChatAsRead(it)
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
        messageAdapter =
            currentUserId?.let { MessageAdapter(messages, it) }!! // Chuyển ID người dùng hiện tại tới adapter
        binding.recyclerViewChat.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = messageAdapter
        }
    }

    private fun markChatAsRead(chatId: String) {
        val chatsCollection = FirebaseFirestore.getInstance().collection("chats")
        val chatQuery = chatsCollection.whereEqualTo("idBuyer", chatId).limit(1)
        chatQuery.get().addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                val document = querySnapshot.documents.first()
                document.reference.update("seenByBuyer", true)
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
            } else {
                // Handle the case where no chat is found by creating a new one
                createNewChat(chatId)
            }
        }
    }

    private fun createNewChat(chatId: String) {
        val accountsCollection = FirebaseFirestore.getInstance().collection("accounts")
        if (currentUserId != null) {
            accountsCollection.document(currentUserId!!).get().addOnSuccessListener { document ->
                val avaSender = document.getString("avatar") ?: "images/avatars/avaeb015d1a-8e43-4baf-aaf6-4639eb258f5e.png"
                val nameSender = document.getString("firstName") ?: "Admin"
                lifecycleScope.launch {
                    try {
                        val avaSenderUrl = fireStorage.reference.child(avaSender).downloadUrl.await().toString()
                        // Create the new chat here inside the coroutine after all data is fetched.
                        val chatsCollection = FirebaseFirestore.getInstance().collection("chats")
                        val newChat = Chat(
                            idBuyer = chatId,
                            nameBuyer = nameSender,
                            avaBuyer = avaSenderUrl,
                            messages = listOf(),
                            seen = false,
                            seenByBuyer = true,
                            date = Date(),
                        )
                        chatsCollection.add(newChat).addOnSuccessListener {
                            Log.d("ChatActivity", "New chat created successfully for buyer ID: $chatId")
                        }.addOnFailureListener { e ->
                            Log.e("ChatActivity", "Failed to create new chat for buyer ID: $chatId", e)
                        }
                    } catch (e: Exception) {
                        Log.e("ChatActivity", "Error retrieving avatar URL", e)
                    }
                }
            }.addOnFailureListener {
                Log.e("ChatActivity", "Failed to retrieve user information", it)
            }
        }
    }


    private fun sendMessageToFirestore(chatId: String, messageText: String) {
        var nameSender = "Admin"
        val accountsCollection = FirebaseFirestore.getInstance().collection("accounts")
        accountsCollection.document("zErR5nXOOmmqrz1YR5V7").get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
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
                if (querySnapshot.documents.isNotEmpty()) {
                    val document = querySnapshot.documents.first() // Assuming 'idBuyer' is unique
                    val message = currentUserId?.let { Message(it, "zErR5nXOOmmqrz1YR5V7", messageText) }
                    val chatUpdateMap = hashMapOf<String, Any>(
                        "messages" to FieldValue.arrayUnion(message),
                        "seen" to false,
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
                Log.e("ChatActivity", "Error updating message and date in Firestore", e)
            }
        }
    }

}
