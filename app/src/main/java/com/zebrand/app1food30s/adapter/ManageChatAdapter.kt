package com.zebrand.app1food30s.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.data.entity.Chat
import com.zebrand.app1food30s.data.entity.Message
import java.text.SimpleDateFormat
import java.util.Locale

class ManageChatAdapter(private val chats: List<Chat>, private val listener: ChatClickListener) : RecyclerView.Adapter<ManageChatAdapter.ChatViewHolder>() {

    companion object {
        const val CHAT_IS_READ = 1
        const val CHAT_NOT_READ = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (chats[position].seen) ManageChatAdapter.CHAT_IS_READ else ManageChatAdapter.CHAT_NOT_READ
    }

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewNameBuyer: TextView = itemView.findViewById(R.id.tvBuyerName)
        val textViewNameSender: TextView = itemView.findViewById(R.id.tvNameSender)
        val textViewLastMessage: TextView = itemView.findViewById(R.id.tvLastMessage)
        val textViewTime: TextView = itemView.findViewById(R.id.tvTimestamp)
        val avatar: ImageView = itemView.findViewById(R.id.imageBuyer)

        fun bind(chat: Chat, listener: ChatClickListener) {
            textViewNameBuyer.text = chat.nameBuyer
            // Safely handle empty messages list
            if (chat.messages.isNotEmpty()) {
                val lastMessage = chat.messages.last()
                textViewNameSender.text = if (lastMessage.idSender == "zErR5nXOOmmqrz1YR5V7") "You: " else "${chat.nameBuyer}: "
                textViewLastMessage.text = lastMessage.messageString
//                textViewTime.text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(lastMessage.date)
            } else {
                textViewNameSender.text = ""
                textViewLastMessage.text = "No messages"
//                textViewTime.text = "No timestamp"
            }

            Picasso.get()
                .load(chat.avaBuyer)
                .placeholder(R.drawable.default_avatar)  // Ensure you have a placeholder image in your resources
                .error(R.drawable.default_avatar)  // Ensure you have an error image in your resources
                .into(avatar)

            itemView.setOnClickListener {
                listener.onChatClicked(chat.idBuyer)
            }
        }
    }

//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false)
//        return ChatViewHolder(view)
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val layout = when (viewType) {
            ManageChatAdapter.CHAT_IS_READ -> R.layout.chat_item
            ManageChatAdapter.CHAT_NOT_READ -> R.layout.chat_item_new
            else -> throw IllegalArgumentException("Invalid view type")
        }
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]
        holder.bind(chat, listener)
    }

    override fun getItemCount() = chats.size
}

//interface ChatClickListener {
//    fun onChatClicked(idBuyer: String)
//}
