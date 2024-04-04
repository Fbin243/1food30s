package com.zebrand.app1food30s.ui.menu

import androidx.recyclerview.widget.DiffUtil
import com.zebrand.app1food30s.data.entity.Product

class ProductDiffCallback(
    private val oldProducts: List<Product>,
    private val newProducts: List<Product>,
    private val oldWishlistedIds: Set<String>,
    private val newWishlistedIds: Set<String>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldProducts.size
    override fun getNewListSize(): Int = newProducts.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // Product IDs uniquely identify them
        return oldProducts[oldItemPosition].id == newProducts[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldProduct = oldProducts[oldItemPosition]
        val newProduct = newProducts[newItemPosition]
        // Checks if the content of the product has changed, including its wishlist state
        return oldProduct == newProduct && (oldProduct.id in oldWishlistedIds) == (newProduct.id in newWishlistedIds)
    }
}
