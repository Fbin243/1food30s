package com.zebrand.app1food30s.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.data.entity.Order
import com.zebrand.app1food30s.databinding.MyOrderStatusSpanBinding
import com.zebrand.app1food30s.utils.Utils

class ManageOrderAdapter(
    private val orders: MutableList<Order>,
) :
    RecyclerView.Adapter<ManageOrderAdapter.MyOrderViewHolder>() {
    var onItemClick: ((Order) -> Unit)? = null

    fun insertData(order: Order) {
        this.orders.add(order)
        this.orders.sortByDescending { it.date }
        val insertedIndex = this.orders.indexOf(order)
        notifyItemInserted(insertedIndex)
    }

    fun modifyData(order: Order) {
        val pos =
            orders.indexOfFirst { it.id == order.id } // Find the position of the modified item
        if (pos != -1) {
            this.orders[pos] = order
            notifyItemChanged(pos)
        }
    }

    fun removeData(order: Order) {
        val pos = orders.indexOfFirst { it.id == order.id } // Find the position of the removed item
        if (pos != -1) {
            this.orders.removeAt(pos)
            notifyItemRemoved(pos)
        }
    }

    inner class MyOrderViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        val tvOrderId: TextView = listItemView.findViewById(R.id.tvOrderId)
        val tvCustomerName: TextView = listItemView.findViewById(R.id.tvCustomerName)
        val tvDate: TextView = listItemView.findViewById(R.id.tvDate)
        val tvTotalAmount: TextView = listItemView.findViewById(R.id.tvTotalAmount)
//        val orderStatusSpan: View = listItemView.findViewById(R.id.orderStatusSpan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyOrderViewHolder {
        val orderCardView =
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_manage_order,
                parent,
                false
            )
        return MyOrderViewHolder(orderCardView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyOrderViewHolder, position: Int) {
        val order: Order = orders[position]

        with(holder){
            tvOrderId.text = Utils.formatId(order.id)
            tvCustomerName.text = order.user.firstName + " " + order.user.lastName
            tvDate.text = Utils.formatDate(order.date)
            tvTotalAmount.text = Utils.formatPrice(order.totalAmount)

            itemView.setOnClickListener {
                onItemClick?.invoke(order)
            }
        }

//        val binding = DataBindingUtil.bind<MyOrderStatusSpanBinding>(holder.orderStatusSpan)
//        binding?.orderStatusSpan = order.orderStatus
    }

    override fun getItemCount(): Int {
        return orders.size
    }
}