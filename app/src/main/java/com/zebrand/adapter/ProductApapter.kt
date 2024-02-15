package com.zebrand.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zebrand.app1food30s.R
import com.zebrand.model.Product

class ProductApapter(private val products: List<Product>): RecyclerView.Adapter<ProductApapter.ProductViewHolder>() {
    inner class ProductViewHolder(listItemView: View): RecyclerView.ViewHolder(listItemView) {
        val productImg: ImageView = listItemView.findViewById(R.id.productImg)
        val productTitle: TextView = listItemView.findViewById(R.id.productTitle)
        val productDescription: TextView = listItemView.findViewById(R.id.productDescription)
        val productPrice: TextView = listItemView.findViewById(R.id.productPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val productCardView = LayoutInflater.from(parent.context).inflate(R.layout.product_card_view, parent, false)
        return ProductViewHolder(productCardView)
    }

    override fun getItemCount(): Int {
        return products.size
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product: Product = products[position]
        holder.productImg.setImageResource(product.img)
        holder.productTitle.text = product.title
        holder.productDescription.text = product.description
        holder.productPrice.text = product.price.toString()
    }
}