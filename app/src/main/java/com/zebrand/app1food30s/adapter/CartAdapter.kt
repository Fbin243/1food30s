package com.zebrand.app1food30s.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentReference
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.data.entity.CartItem
import com.zebrand.app1food30s.ui.cart.CartDiffCallback

class CartAdapter(
    private val context: Context,
    private var items: MutableList<CartItem>,
    private val onItemDeleted: (CartItem) -> Unit,
    private val onQuantityUpdated: (CartItem, Int) -> Unit,
    private val onUpdateTotalPrice: (Double) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productCategory: TextView = view.findViewById(R.id.productCategory)
        val productImg: ImageView = view.findViewById(R.id.productImg)
        val productName: TextView = view.findViewById(R.id.productName)
        val productOldPrice: TextView = view.findViewById(R.id.productOldPrice)
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

            // Check if there's a discount by comparing the old price with the new price
            if (detailedCartItem.oldPrice > detailedCartItem.productPrice) {
                // There's a discount, show the old price and apply strikethrough effect
                productOldPrice.visibility = View.VISIBLE
                productOldPrice.text = context.getString(R.string.product_price_number, detailedCartItem.oldPrice)
                // Optionally, apply strikethrough directly if not using a background drawable for it
                productOldPrice.paintFlags = productOldPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                // No discount, hide the old price TextView
                productOldPrice.visibility = View.GONE
            }

            itemQuantity.text = detailedCartItem.quantity.toString()

//            plusBtn.setOnClickListener {
//                if (detailedCartItem.quantity < detailedCartItem.productStock) {
//                    val newQuantity = detailedCartItem.quantity + 1
//                    detailedCartItem.quantity = newQuantity // Update the item directly
//                    notifyItemChanged(position, "quantity") // Use payload to specify what changed
////                    onQuantityUpdated(detailedCartItem, newQuantity)
//                    onUpdateTotalPrice(items.sumOf { it.productPrice * it.quantity })
//                }
//            }
//
//            minusBtn.setOnClickListener {
//                val newQuantity = detailedCartItem.quantity - 1
//                if (newQuantity >= 1) { // Ensure quantity doesn't go below 1
//                    detailedCartItem.quantity = newQuantity
//                    notifyItemChanged(position, "quantity") // Use payload to specify what changed
////                    onQuantityUpdated(detailedCartItem, newQuantity)
//                    onUpdateTotalPrice(items.sumOf { it.productPrice * it.quantity })
//                }
//            }

            val grey = ContextCompat.getColor(context, R.color.grey_30)
            val primary = ContextCompat.getColor(context, R.color.primary)

            plusBtn.setOnClickListener {
                if (detailedCartItem.quantity < detailedCartItem.productStock) {
                    val newQuantity = detailedCartItem.quantity + 1
                    detailedCartItem.quantity = newQuantity
                    notifyItemChanged(position, "quantity")
                    onUpdateTotalPrice(items.sumOf { it.productPrice * it.quantity })
                    // Enable minus button if the quantity increases above 1
                    holder.minusBtn.setColorFilter(primary, PorterDuff.Mode.SRC_IN)
                }
                // Disable the plus button if the quantity reaches the stock limit
                if (detailedCartItem.quantity >= detailedCartItem.productStock) {
                    holder.plusBtn.setColorFilter(grey, PorterDuff.Mode.SRC_IN)
                } else {
                    holder.plusBtn.setColorFilter(primary, PorterDuff.Mode.SRC_IN)
                }
            }

            minusBtn.setOnClickListener {
                val newQuantity = detailedCartItem.quantity - 1
                if (newQuantity >= 1) {
                    detailedCartItem.quantity = newQuantity
                    notifyItemChanged(position, "quantity")
                    onUpdateTotalPrice(items.sumOf { it.productPrice * it.quantity })
                    // Enable the plus button if the quantity goes below the stock limit
                    holder.plusBtn.setColorFilter(primary, PorterDuff.Mode.SRC_IN)
                }
                // Disable the minus button if the quantity decreases to 1
                if (newQuantity <= 1) {
                    holder.minusBtn.setColorFilter(grey, PorterDuff.Mode.SRC_IN)
                } else {
                    holder.minusBtn.setColorFilter(primary, PorterDuff.Mode.SRC_IN)
                }
            }

            deleteBtn.setOnClickListener {
                onItemDeleted(detailedCartItem)
            }
        }
    }

    fun getItems(): List<CartItem> {
        return items.toList()  // Create a copy of the list to prevent external modifications
    }
    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<CartItem>) {
        val diffCallback = CartDiffCallback(items, newItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        items.clear()
        items.addAll(newItems)
        diffResult.dispatchUpdatesTo(this) // Efficiently updates the list with minimal refreshes

        onUpdateTotalPrice(items.sumOf { it.productPrice * it.quantity })
    }

    fun loadItems(newItems: List<CartItem>) {
        items.clear() // Clear existing items
        items.addAll(newItems) // Add new items
        notifyDataSetChanged() // Notify the adapter to refresh the UI
        onUpdateTotalPrice(items.sumOf { it.productPrice * it.quantity }) // Update total price
    }

    fun removeItemByRef(productRef: DocumentReference) {
        val indexToRemove = items.indexOfFirst { it.productId == productRef }
        if (indexToRemove != -1) {
            items.removeAt(indexToRemove)
            notifyItemRemoved(indexToRemove)
        }
    }

//    fun removeItem(position: Int) {
//        items.removeAt(position)
//        notifyItemRemoved(position)
//        notifyItemRangeChanged(position, items.size) // Update positions of remaining items
//        onUpdateTotalPrice(items.sumOf { it.productPrice * it.quantity })
//    }
//
//    fun addItem(item: CartItem) {
//        items.add(item)
//        notifyItemInserted(items.size - 1)
//        onUpdateTotalPrice(items.sumOf { it.productPrice * it.quantity })
//    }
}