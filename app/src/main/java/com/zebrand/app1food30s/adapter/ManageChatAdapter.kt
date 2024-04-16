package com.zebrand.app1food30s.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.data.entity.Chat

class ManageChatAdapter(private val chats: List<Chat>, private val listener: ChatClickListener) : RecyclerView.Adapter<ManageChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(chat: Chat, listener: ChatClickListener) {
            itemView.findViewById<TextView>(R.id.tvBuyerName).text = "Buyer ID: ${chat.idBuyer}"
            itemView.setOnClickListener {
                listener.onChatClicked(chat.idBuyer)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(chats[position], listener)
    }

    override fun getItemCount() = chats.size
}
