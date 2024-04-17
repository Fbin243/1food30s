package com.zebrand.app1food30s.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.data.entity.Message
import com.zebrand.app1food30s.utils.FirebaseUtils.fireStorage
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(private val messages: List<Message>, private val currentUserId: String) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    companion object {
        const val MESSAGE_TYPE_SENT = 1
        const val MESSAGE_TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].idSender == currentUserId) MESSAGE_TYPE_SENT else MESSAGE_TYPE_RECEIVED
    }
    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewSender: TextView = view.findViewById(R.id.textViewSender)
        val textViewMessage: TextView = view.findViewById(R.id.textViewMessage)
        val textViewTimestamp: TextView = view.findViewById(R.id.textViewTimestamp)
    }

//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
//        return MessageViewHolder(view)
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layout = when (viewType) {
            MESSAGE_TYPE_SENT -> R.layout.item_message_sent
            MESSAGE_TYPE_RECEIVED -> R.layout.item_message_received
            else -> throw IllegalArgumentException("Invalid view type")
        }
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
//        val imageUrl = fireStorage.reference.child(message.avaSender).downloadUrl.await().toString()
//        Picasso.get().load(message.avaSender).into(holder.avatar)
        holder.textViewSender.text = message.nameSender
        holder.textViewMessage.text = message.messageString
//        Picasso.get().load(message.avaSender).into(holder.avatar)
        Log.d("adminLogin", "messageStr: ${message.messageString}")
        holder.textViewTimestamp.text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(message.date)
    }

    override fun getItemCount(): Int = messages.size
}
