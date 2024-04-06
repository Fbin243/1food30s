package com.zebrand.app1food30s.ui.cart

import androidx.recyclerview.widget.DiffUtil
import com.zebrand.app1food30s.data.entity.CartItem

class CartDiffCallback(
    private val oldList: List<CartItem>,
    private val newList: List<CartItem>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    // Determines if two list items are the same, checking by ID or another unique identifier
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].productId == newList[newItemPosition].productId
    }

    // Checks if the content of two list items are the same
    // This might involve comparing all fields of the items if their identity is the same
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        return oldItem == newItem // You can replace this with more sophisticated checks
    }

    // Optionally, if you use payloads, you can override getChangePayload() to return specific payloads for more granular updates
}
