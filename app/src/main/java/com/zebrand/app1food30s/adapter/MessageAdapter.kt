package com.zebrand.app1food30s.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.data.entity.Message
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(private val messages: List<Message>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewSender: TextView = view.findViewById(R.id.textViewSender)
        val textViewMessage: TextView = view.findViewById(R.id.textViewMessage)
        val textViewTimestamp: TextView = view.findViewById(R.id.textViewTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.textViewSender.text = message.idSender // Replace this with sender's name if available
        holder.textViewMessage.text = message.messageString
        holder.textViewTimestamp.text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(message.date)
    }

    override fun getItemCount(): Int = messages.size
}
