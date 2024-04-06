package com.zebrand.app1food30s.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.data.entity.Order
import com.zebrand.app1food30s.data.entity.OrderItem
import com.zebrand.app1food30s.databinding.MyOrderStatusSpanBinding
import com.zebrand.app1food30s.utils.Utils

class MyOrderDetailsAdapter(
    private val context: Context,
    private val orderItems: MutableList<OrderItem>,
) :
    RecyclerView.Adapter<MyOrderDetailsAdapter.MyOrderViewHolder>() {
    var onItemClick: ((OrderItem) -> Unit)? = null

    fun insertData(order: OrderItem) {
        this.orderItems.add(order)
        notifyItemInserted(orderItems.size - 1)
    }

    inner class MyOrderViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        val productQuantity: TextView = listItemView.findViewById(R.id.productQuantity)
        val productImg: ImageView = listItemView.findViewById(R.id.productImg)
        val productName: TextView = listItemView.findViewById(R.id.productName)
        val productCategory: TextView = listItemView.findViewById(R.id.productCategory)
        val productPrice: TextView = listItemView.findViewById(R.id.productPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyOrderViewHolder {
        val orderCardView =
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_order_details,
                parent,
                false
            )
        return MyOrderViewHolder(orderCardView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyOrderViewHolder, position: Int) {
        val orderItem: OrderItem = orderItems[position]

        with(holder){
            productQuantity.text = orderItem.quantity.toString()
            Glide.with(context).load(orderItem.image).into(productImg)
            productName.text = orderItem.name
            productCategory.text = "Category: ${orderItem.category}"
            productPrice.text = Utils.formatPrice(orderItem.price)

            itemView.setOnClickListener {
                onItemClick?.invoke(orderItem)
            }
        }
    }

    fun getSubTotal(): Double {
        var subTotal = 0.0
        for (orderItem in orderItems) {
            subTotal += orderItem.price * orderItem.quantity
        }
        return subTotal
    }

    fun getDiscount(): Double {
        var discount = 0.0
        for (orderItem in orderItems) {
            discount += orderItem.discount
        }
        return discount
    }

    override fun getItemCount(): Int {
        return orderItems.size
    }
}