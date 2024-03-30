package com.zebrand.app1food30s.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.data.entity.DetailedCartItem

class CheckoutItemsAdapter : RecyclerView.Adapter<CheckoutItemsAdapter.CheckoutViewHolder>() {

    private var items = listOf<DetailedCartItem>()

    fun setItems(newItems: List<DetailedCartItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckoutViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order_details, parent, false)
        return CheckoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: CheckoutViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    class CheckoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productName: TextView = itemView.findViewById(R.id.productName)
        private val productCategory: TextView = itemView.findViewById(R.id.productCategory)
        private val productImage: ImageView = itemView.findViewById(R.id.productImg)
        private val productPrice: TextView = itemView.findViewById(R.id.productPrice)
        private val quantity: TextView = itemView.findViewById(R.id.itemQuantity)

        fun bind(item: DetailedCartItem) {
            productName.text = item.productName
            productCategory.text = item.productCategory
            Glide.with(itemView.context).load(item.productImage).into(productImage)
            productPrice.text = itemView.context.getString(R.string.product_price_number, item.productPrice)
            quantity.text = item.quantity.toString()
        }
    }
}
