package com.zebrand.app1food30s.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.google.firebase.firestore.DocumentReference
import com.squareup.picasso.Picasso
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.data.Offer
import com.zebrand.app1food30s.data.Product
import com.zebrand.app1food30s.utils.Utils.formatPrice

class ProductAdapter(
    val products: List<Product>,
    private val offers: List<Offer>,
    private val isGrid: Boolean = true,
    private var wishlistedProductIds: Set<String>,
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
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onWishlistProductClick?.invoke(products[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val productCardView =
            LayoutInflater.from(parent.context).inflate(
                if (isGrid) R.layout.product_card_view_grid else R.layout.product_card_view_linear,
                parent,
                false
            )
        return ProductViewHolder(productCardView)
    }

    override fun getItemCount(): Int {
        return products.size
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product: Product = products[position]
        val shimmer: Shimmer =
            Shimmer.ColorHighlightBuilder().setBaseColor(Color.parseColor("#f3f3f3"))
                .setBaseAlpha(1.0f).setHighlightColor(Color.parseColor("#e7e7e7"))
                .setHighlightAlpha(1.0f).setDropoff(50.0f).build()

        val shimmerDrawable = ShimmerDrawable()
        shimmerDrawable.setShimmer(shimmer)
        Picasso.get().load(product.image).placeholder(shimmerDrawable).into(holder.productImg)
        holder.productTitle.text = product.name
        holder.productDescription.text = product.description

        // Find offer of product
        val oldPrice = product.price
        "$${formatPrice(oldPrice)}".also { holder.productPrice.text = it }
        if (product.idOffer != null) {
            val offer = offers.find { it.id == product.idOffer.id }
            Log.i("Fix", "onBindViewHolder: $offer")
            val newPrice = oldPrice - offer!!.discountRate * oldPrice / 100
            "$${formatPrice(oldPrice)}".also { holder.productOldPrice.text = it }
            "$${formatPrice(newPrice)}".also { holder.productPrice.text = it }
            holder.productOldPrice.visibility = View.VISIBLE
        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(product)
        }

        // Wishlist
        val isProductWishlisted = product.id in wishlistedProductIds // Check if product's ID is in the set of wishlisted product IDs
        Log.d("Test00", "$isProductWishlisted")
        holder.ivWishlist.setImageResource(
            if (isProductWishlisted) R.drawable.ic_wishlist_active else R.drawable.ic_wishlist
        )
    }

    fun updateWishlistState(newWishlistedProductIds: Set<String>) {
        this.wishlistedProductIds = newWishlistedProductIds
        notifyDataSetChanged()
    }
}
