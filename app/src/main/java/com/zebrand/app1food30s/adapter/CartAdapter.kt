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
import com.zebrand.app1food30s.data.entity.DetailedCartItem

class CartAdapter(
    private val context: Context,
    private var items: MutableList<DetailedCartItem>,
    private val onItemDeleted: (DetailedCartItem) -> Unit,
    private val onQuantityUpdated: (DetailedCartItem, Int) -> Unit,
    private val onUpdateTotalPrice: (Double) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productCategory: TextView = view.findViewById(R.id.productCategory)
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

    override fun onBindViewHolder(holder: CartViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            payloads.forEach { payload ->
                when (payload) {
                    "quantity" -> {
                        // Update only the quantity text view
                        holder.itemQuantity.text = items[position].quantity.toString()
                    }
                    // Handle other specific updates with different payloads if necessary
                }
            }
        }
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val detailedCartItem = items[position]
        with(holder) {
            Glide.with(context).load(detailedCartItem.productImage).into(productImg)
            productCategory.text = detailedCartItem.productCategory
            productName.text = detailedCartItem.productName
            productPrice.text = context.getString(R.string.product_price_number, detailedCartItem.productPrice)
            itemQuantity.text = detailedCartItem.quantity.toString()

            plusBtn.setOnClickListener {
                if (detailedCartItem.quantity < detailedCartItem.productStock) {
                    val newQuantity = detailedCartItem.quantity + 1
                    detailedCartItem.quantity = newQuantity // Update the item directly
                    notifyItemChanged(position, "quantity") // Use payload to specify what changed
                    onQuantityUpdated(detailedCartItem, newQuantity) // Notify the presenter
                }
            }

            minusBtn.setOnClickListener {
                val newQuantity = detailedCartItem.quantity - 1
                if (newQuantity >= 1) { // Ensure quantity doesn't go below 1
                    detailedCartItem.quantity = newQuantity
                    notifyItemChanged(position, "quantity") // Use payload to specify what changed
                    onQuantityUpdated(detailedCartItem, newQuantity)
                }
            }

            deleteBtn.setOnClickListener {
                onItemDeleted(detailedCartItem)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<DetailedCartItem>) {
        items = newItems.toMutableList()
        notifyDataSetChanged()
        onUpdateTotalPrice(items.sumOf { it.productPrice * it.quantity })
    }

    fun removeItemByRef(productRef: DocumentReference) {
        val indexToRemove = items.indexOfFirst { it.productId == productRef }
        if (indexToRemove != -1) {
            items.removeAt(indexToRemove)
            notifyItemRemoved(indexToRemove)
        }
    }
}