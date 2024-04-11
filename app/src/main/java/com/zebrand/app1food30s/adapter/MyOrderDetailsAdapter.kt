package com.zebrand.app1food30s.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.data.entity.OrderItem
import com.zebrand.app1food30s.utils.SingletonKey
import com.zebrand.app1food30s.utils.Utils

class MyOrderDetailsAdapter(
    private val context: Context,
    private val orderItems: MutableList<OrderItem>,
    private var isDelivered: Boolean = false
) :
    RecyclerView.Adapter<MyOrderDetailsAdapter.MyOrderViewHolder>() {
    var onItemClick: ((OrderItem) -> Unit)? = null
    var onReviewBtnClick: ((OrderItem, MyOrderViewHolder) -> Unit)? = null
    var onReviewBtnClickAfterReview: ((OrderItem) -> Unit)? = null

    fun insertData(order: OrderItem) {
        this.orderItems.add(order)
        notifyItemInserted(orderItems.size - 1)
    }

    fun updateIsDelivered(status: String) {
        isDelivered = status == SingletonKey.DELIVERED
    }

    inner class MyOrderViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        val productQuantity: TextView = listItemView.findViewById(R.id.productQuantity)
        val productImg: ImageView = listItemView.findViewById(R.id.productImg)
        val productName: TextView = listItemView.findViewById(R.id.productName)
        val productCategory: TextView = listItemView.findViewById(R.id.productCategory)
        val productPrice: TextView = listItemView.findViewById(R.id.productPrice)
        val reviewBtn: TextView = listItemView.findViewById(R.id.reviewBtn)
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
        with(holder) {
            productQuantity.text = orderItem.quantity.toString()
            Glide.with(context).load(orderItem.image).into(productImg)
            productName.text = orderItem.name
            productCategory.text =
                context.resources.getString(R.string.txt_category_item_order) + orderItem.category
            productPrice.text = Utils.formatPrice(orderItem.price)

            itemView.setOnClickListener {
                onItemClick?.invoke(orderItem)
            }
            Log.i("TAG123", "onBindViewHolder: $isDelivered")
            if (isDelivered) {
                reviewBtn.visibility = View.VISIBLE
                if (orderItem.isReviewed) {
                    reviewBtn.text = context.resources.getString(R.string.txt_reviewed)
                    reviewBtn.setTextColor(context.resources.getColor(R.color.secondary))
                    reviewBtn.setOnClickListener {
                        onReviewBtnClickAfterReview?.invoke(orderItem)
                    }
                } else {
                    reviewBtn.setOnClickListener {
                        onReviewBtnClick?.invoke(orderItem, holder)
                    }
                }
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