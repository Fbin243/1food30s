package com.zebrand.app1food30s.adapter

import android.util.Log
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
    private val products: List<Product>,
    private val offers: List<Offer>,
    private val isGrid: Boolean = true
) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {
    var onItemClick: ((Product) -> Unit)? = null
    var onAddButtonClick: ((Product) -> Unit)? = null

    inner class ProductViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        val productImg: ImageView = listItemView.findViewById(R.id.productImg)
        val productTitle: TextView = listItemView.findViewById(R.id.productTitle)
        val productDescription: TextView = listItemView.findViewById(R.id.productDescription)
        val productPrice: TextView = listItemView.findViewById(R.id.productPrice)
        val productOldPrice: TextView = listItemView.findViewById(R.id.productOldPrice)
        private val addButton: Button = listItemView.findViewById(R.id.addBtn)

        init {
            addButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onAddButtonClick?.invoke(products[position])
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
        Picasso.get().load(product.image).placeholder(getShimmerDrawable()).into(holder.productImg)
        holder.productTitle.text = product.name
        holder.productDescription.text = product.description

        // Find offer of product
        val oldPrice = product.price
        "$${formatPrice(oldPrice)}".also { holder.productPrice.text = it }
        if (product.idOffer != null) {
            val idOffer = product.idOffer!!.id
            val offer = offers.find { it.id == idOffer }
            Log.i("Fix", "onBindViewHolder: $offer")
            val newPrice = oldPrice - offer!!.discountRate * oldPrice / 100
            "$${formatPrice(oldPrice)}".also { holder.productOldPrice.text = it }
            "$${formatPrice(newPrice)}".also { holder.productPrice.text = it }
            holder.productOldPrice.visibility = View.VISIBLE
        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(product)
        }
    }
}
