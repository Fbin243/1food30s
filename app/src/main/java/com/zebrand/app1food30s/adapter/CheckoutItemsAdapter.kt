package com.zebrand.app1food30s.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zebrand.app1food30s.R

class CheckoutItemsAdapter(private val items: List<CartItemAdapter.CartItem>) : RecyclerView.Adapter<CheckoutItemsAdapter.CheckoutViewHolder>() {

    class CheckoutViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productName: TextView = view.findViewById(R.id.productName) // Adjust ID as necessary
        val productPrice: TextView = view.findViewById(R.id.productPrice) // Adjust ID as necessary
        val productQuantity: TextView = view.findViewById(R.id.productQuantity) // You might need to add this to your XML layout if it doesn't exist
        // Add more views as necessary
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckoutViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order_details, parent, false)
        return CheckoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: CheckoutViewHolder, position: Int) {
        val item = items[position]
        holder.productName.text = item.product.name
        holder.productPrice.text = "Price: ${item.product.price}" // Format as needed
        holder.productQuantity.text = "Quantity: ${item.quantity}" // Ensure you have this TextView in your item layout
        // Update other views as necessary
    }

    override fun getItemCount(): Int = items.size
}
