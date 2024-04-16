package com.zebrand.app1food30s.ui.chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.ChatClickListener
import com.zebrand.app1food30s.adapter.ManageChatAdapter
import com.zebrand.app1food30s.data.entity.Chat

class ChatManager : AppCompatActivity(), ChatClickListener {
    private lateinit var chatAdapter: ManageChatAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_manager)

        recyclerView = findViewById(R.id.rvChatList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        fetchData()
    }

    private fun fetchData() {
        val db = Firebase.firestore
        db.collection("chats")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("ChatManagerActivity", "Listen failed.", e)
                    return@addSnapshotListener
                }

                val chats = mutableListOf<Chat>()
                for (doc in snapshots!!) {
                    doc.toObject(Chat::class.java)?.let {
                        chats.add(it)
                    }
                }
                chatAdapter = ManageChatAdapter(chats, this)
                recyclerView.adapter = chatAdapter
            }
    }

    override fun onChatClicked(chatId: String) {
        val intent = Intent(this, ManageChatDetail::class.java)
        intent.putExtra("chatId", chatId)
        startActivity(intent)
    }
}
