package com.zebrand.app1food30s.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zebrand.app1food30s.R

class CheckoutItemsAdapter : RecyclerView.Adapter<CheckoutItemsAdapter.CheckoutViewHolder>() {

    private var items = listOf<String>()

    fun setItems(newItems: List<String>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckoutViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order_details, parent, false)
        return CheckoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: CheckoutViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class CheckoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productName: TextView = itemView.findViewById(R.id.productName)
        private val productCategory: TextView = itemView.findViewById(R.id.productCategory)
        private val productImage: ImageView = itemView.findViewById(R.id.productImg)
        private val productPrice: TextView = itemView.findViewById(R.id.productPrice)
        private val quantity: TextView = itemView.findViewById(R.id.itemQuantity)

        fun bind(description: String) {
            val parts = description.split(" - ")
            if (parts.size == 5) {
                Glide.with(itemView.context).load(parts[0]).into(productImage)
                productName.text = parts[1]
                productCategory.text = parts[2]
                productPrice.text = parts[3]
                quantity.text = parts[4]
            }
        }
    }
}