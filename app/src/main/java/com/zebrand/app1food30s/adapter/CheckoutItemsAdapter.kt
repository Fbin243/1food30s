package com.zebrand.app1food30s.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.data.model.CheckoutItem

class CheckoutItemsAdapter(private val items: List<CheckoutItem>) : RecyclerView.Adapter<CheckoutItemsAdapter.CheckoutViewHolder>() {

    class CheckoutViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productName: TextView = view.findViewById(R.id.productName)
        val productPrice: TextView = view.findViewById(R.id.productPrice)
        val productQuantity: TextView = view.findViewById(R.id.productQuantity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckoutViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order_details, parent, false)
        return CheckoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: CheckoutViewHolder, position: Int) {
        val item = items[position]
        holder.productName.text = item.productName
        holder.productPrice.text = "Price: ${item.productPrice}" // Format as needed
        holder.productQuantity.text = "Quantity: ${item.quantity}"
        // Update other views as necessary
    }

    override fun getItemCount(): Int = items.size
}
