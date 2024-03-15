package com.zebrand.app1food30s.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.squareup.picasso.Picasso
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.data.Product

class ProductAdapter(private val products: List<Product>, private val isGrid: Boolean = true) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {
    var onItemClick: ((ProductAdapter.ProductViewHolder) -> Unit)? = null

    inner class ProductViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        val productImg: ImageView = listItemView.findViewById(R.id.productImg)
        val productTitle: TextView = listItemView.findViewById(R.id.productTitle)
        val productDescription: TextView = listItemView.findViewById(R.id.productDescription)
        val productPrice: TextView = listItemView.findViewById(R.id.productPrice)
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
        holder.productPrice.text = "$${String.format("%.2f", product.price).replace(",", ".")}"
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(holder)
        }
    }
}