package com.zebrand.app1food30s.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentReference
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.data.model.DetailedCartItem

class CartItemAdapter(
    private val context: Context,
    private var items: List<DetailedCartItem>,
    private val onItemDeleted: (DetailedCartItem) -> Unit,
    private val onQuantityChanged: (DetailedCartItem) -> Unit,
    private val onUpdateTotalPrice: (Double) -> Unit
) : RecyclerView.Adapter<CartItemAdapter.CartViewHolder>() {

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productImg: ImageView = view.findViewById(R.id.productImg)
        val productName: TextView = view.findViewById(R.id.productName)
        val productPrice: TextView = view.findViewById(R.id.productPrice)
        val minusBtn: ImageView = view.findViewById(R.id.minusBtn)
        val plusBtn: ImageView = view.findViewById(R.id.plusBtn)
        val itemQuantity: TextView = view.findViewById(R.id.itemQuantity)
        val deleteBtn: ImageView = view.findViewById(R.id.deleteBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val detailedCartItem = items[position]
        with(holder) {
            Glide.with(context).load(detailedCartItem.productImage).into(productImg)
            productName.text = detailedCartItem.productName
            productPrice.text = context.getString(R.string.product_price_number, detailedCartItem.productPrice)
            itemQuantity.text = detailedCartItem.quantity.toString()

            plusBtn.setOnClickListener {
                if (detailedCartItem.quantity < Int.MAX_VALUE) { // Assuming stock check is handled elsewhere
                    val newQuantity = detailedCartItem.quantity + 1
                    onQuantityChanged(detailedCartItem.copy(quantity = newQuantity))
                }
            }

            minusBtn.setOnClickListener {
                if (detailedCartItem.quantity > 1) {
                    val newQuantity = detailedCartItem.quantity - 1
                    onQuantityChanged(detailedCartItem.copy(quantity = newQuantity))
                }
            }

            deleteBtn.setOnClickListener {
                onItemDeleted(detailedCartItem)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<DetailedCartItem>) {
        items = newItems
        notifyDataSetChanged()
        onUpdateTotalPrice(items.sumOf { it.productPrice * it.quantity })
    }

    fun removeItemByRef(productRef: DocumentReference) {
        val indexToRemove = items.indexOfFirst { it.productId == productRef }
        if (indexToRemove != -1) {
            (items as MutableList).removeAt(indexToRemove)
            notifyItemRemoved(indexToRemove)
        }
    }
}