package com.zebrand.app1food30s.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zebrand.app1food30s.R

class MyOrderAdapter(
    private val orders: List<com.zebrand.app1food30s.data.entity.Order>,
) :
    RecyclerView.Adapter<MyOrderAdapter.MyOrderViewHolder>() {
    var onItemClick: ((com.zebrand.app1food30s.data.entity.Order) -> Unit)? = null

    inner class MyOrderViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        val productImg: ImageView = listItemView.findViewById(R.id.productImg)
        val productTitle: TextView = listItemView.findViewById(R.id.productTitle)
        val productDescription: TextView = listItemView.findViewById(R.id.productDescription)
        val productPrice: TextView = listItemView.findViewById(R.id.productPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyOrderViewHolder {
        val productCardView =
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_my_order,
                parent,
                false
            )
        return MyOrderViewHolder(productCardView)
    }

    override fun onBindViewHolder(holder: MyOrderViewHolder, position: Int) {
        val order: com.zebrand.app1food30s.data.entity.Order = orders[position]

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(order)
        }
    }

    override fun getItemCount(): Int {
        return orders.size
    }
}