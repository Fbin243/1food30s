package com.zebrand.app1food30s.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.data.entity.Offer
import com.zebrand.app1food30s.data.entity.Product
import com.zebrand.app1food30s.utils.Utils.formatPrice
import com.zebrand.app1food30s.utils.Utils.getShimmerDrawable

class ProductAdapter(
    var products: List<Product>,
    private var offers: List<Offer>,
    private var wishlistedProductIds: MutableSet<String>,
    private var noItemLayout: View? = null
) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {
    var onItemClick: ((Product) -> Unit)? = null
    var onAddButtonClick: ((Product) -> Unit)? = null
    var onWishlistProductClick: ((Product) -> Unit)? = null

    inner class ProductViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        val productImg: ImageView = listItemView.findViewById(R.id.productImg)
        val productTitle: TextView = listItemView.findViewById(R.id.productTitle)
        val productDescription: TextView = listItemView.findViewById(R.id.productDescription)
        val productPrice: TextView = listItemView.findViewById(R.id.productPrice)
        val productOldPrice: TextView = listItemView.findViewById(R.id.productOldPrice)
        private val addButton: Button = listItemView.findViewById(R.id.addBtn)
        val ivWishlist: ImageView = listItemView.findViewById(R.id.ivWishlist)

        init {
            addButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onAddButtonClick?.invoke(products[position])
                }
            }

            ivWishlist.setOnClickListener {
//                Log.d("Test00", "ivWishlist: ")
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onWishlistProductClick?.invoke(products[position])
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (products[position].isGrid) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val productCardView =
            LayoutInflater.from(parent.context).inflate(
                if (viewType == 1) R.layout.product_card_view_grid else R.layout.product_card_view_linear,
                parent,
                false
            )
        return ProductViewHolder(productCardView)
    }

    override fun getItemCount(): Int {
        return products.size
    }

    // make sure that wishlist is up to date
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product: Product = products[position]
        Picasso.get().load(product.image).placeholder(getShimmerDrawable()).into(holder.productImg)
        holder.productTitle.text = product.name
        holder.productDescription.text = product.description

        // Find offer of product
        val oldPrice = product.price
        "${formatPrice(oldPrice, holder.itemView.context)}".also { holder.productPrice.text = it }
        if (product.idOffer != null) {
            val idOffer = product.idOffer!!.id
            val offer = offers.find { it.id == idOffer }
            val newPrice = oldPrice - offer!!.discountRate * oldPrice / 100
            formatPrice(oldPrice, holder.itemView.context).also {
                holder.productOldPrice.text = it
            }
            formatPrice(newPrice, holder.itemView.context).also {
                holder.productPrice.text = it
            }
            holder.productOldPrice.visibility = View.VISIBLE
        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(product)
        }

        // Wishlist
        val productId = product.id
        val isProductWishlisted = productId in wishlistedProductIds
//        Log.d("Test00", "onBindViewHolder: $productId")
//        Log.d("Test00", "onBindViewHolder: $wishlistedProductIds")
//        Log.d("Test00", "onBindViewHolder: $isProductWishlisted")
        holder.ivWishlist.setImageResource(
            if (isProductWishlisted) R.drawable.ic_wishlist_active else R.drawable.ic_wishlist
        )
    }

    fun updateWishlistProductIds(newWishlistedProductIds: Set<String>) {
//        Log.d("Test00", "updateWishlistProductIds: new: $newWishlistedProductIds")
//        wishlistedProductIds.clear()
//        wishlistedProductIds.addAll(newWishlistedProductIds)
        wishlistedProductIds = newWishlistedProductIds.toMutableSet()
//        Log.d("Test00", "updateWishlistProductIds: $wishlistedProductIds")
    }

    fun updateWishlistState(newWishlistedProductIds: Set<String>) {
//        Log.d("WishlistDebug", "Starting to update wishlist UI state.")

        // Update the adapter's state with the new set of wishlisted products
//        wishlistedProductIds.clear()
//        wishlistedProductIds.addAll(newWishlistedProductIds)\
        wishlistedProductIds = newWishlistedProductIds.toMutableSet()
//        Log.d("WishlistDebug", "Wishlist updated with new items: $wishlistedProductIds")

        // Notify the entire dataset has changed. This is not the most efficient way to update
        // the adapter, but it's a straightforward approach when diff comparison isn't needed.
        // TODO
        notifyDataSetChanged()
//        Log.d("WishlistDebug", "Adapter notified of entire data set change.")
    }

//    fun updateWishlistState(newWishlistedProductIds: Set<String>) {
//        Log.d("WishlistDebug", "Starting to update wishlist state.")
//
//        // Immediately capture the current state as the old state before any changes
//        val oldWishlistedProductIds = HashSet(wishlistedProductIds)
//        Log.d("WishlistDebug", "Old wishlist state captured: $oldWishlistedProductIds")
//
//        // Update the adapter's state to the new state
//        wishlistedProductIds.clear()
//        Log.d("WishlistDebug", "Cleared old wishlist items.")
//
//        wishlistedProductIds.addAll(newWishlistedProductIds)
//        Log.d("WishlistDebug", "Updated with new wishlist items: $wishlistedProductIds")
//
//        // Set up the DiffUtil callback
//        val diffCallback = object : DiffUtil.Callback() {
//            override fun getOldListSize(): Int = products.size
//            override fun getNewListSize(): Int = products.size
//
//            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
//                return products[oldItemPosition].id == products[newItemPosition].id
//            }
//
//            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
//                // Now correctly compares old and new state
//                val oldProduct = products[oldItemPosition]
//                val isItemWishlistedBefore = oldProduct.id in oldWishlistedProductIds
//                val isItemWishlistedNow = oldProduct.id in wishlistedProductIds
//                val contentSame = isItemWishlistedBefore == isItemWishlistedNow
//
//                Log.d("WishlistDebug", "Comparing contents for item at position $oldItemPosition: " +
//                        "Wishlisted before: $isItemWishlistedBefore, Wishlisted now: $isItemWishlistedNow, " +
//                        "Contents same: $contentSame")
//
//                return contentSame
//            }
//        }
//
//        // Calculate the diff and dispatch updates to the adapter
//        val diffResult = DiffUtil.calculateDiff(diffCallback)
//        Log.d("WishlistDebug", "Diff result calculated.")
//
//        diffResult.dispatchUpdatesTo(this)
//        Log.d("WishlistDebug", "Adapter notified of data changes.")
//    }


//    fun updateWishlistState(newWishlistedProductIds: Set<String>) {
//        // Immediately capture the current state as the old state before any changes
//        val oldWishlistedProductIds = HashSet(wishlistedProductIds)
//
////        Log.d("Test00", "old: $oldWishlistedProductIds")
//        // Update the adapter's state to the new state
//        wishlistedProductIds.clear()
//        wishlistedProductIds.addAll(newWishlistedProductIds)
//        Log.d("Test00", "updateWishlistState: new wishlist: $wishlistedProductIds")
//
////        // Create a copy of the current state for comparison
////        val oldWishlistedProductIds = wishlistedProductIds.toSet()
////        Log.d("Test00", "old: $oldWishlistedProductIds")
////        // Update the adapter's state with the new set
////        wishlistedProductIds = newWishlistedProductIds
////        Log.d("Test00", "new: $wishlistedProductIds")
//
//        val diffCallback = object : DiffUtil.Callback() {
//            override fun getOldListSize(): Int = products.size
//            override fun getNewListSize(): Int = products.size
//
//            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
//                return products[oldItemPosition].id == products[newItemPosition].id
//            }
//
//            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
//                // Now correctly compares old and new state
//                val oldProduct = products[oldItemPosition]
//                return oldProduct.id in oldWishlistedProductIds == oldProduct.id in wishlistedProductIds
//            }
//        }
//
//        val diffResult = DiffUtil.calculateDiff(diffCallback)
//        diffResult.dispatchUpdatesTo(this)
//    }

    fun updateData(newProducts: List<Product>, newOffers: List<Offer>) {
        products = newProducts
        offers = newOffers
        if (products.isEmpty()) {
            noItemLayout?.visibility = View.VISIBLE
        } else {
            noItemLayout?.visibility = View.GONE
        }
        notifyDataSetChanged()
    }
}
