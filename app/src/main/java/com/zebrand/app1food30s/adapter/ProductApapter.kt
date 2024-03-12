package com.zebrand.app1food30s.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.data.Product
import com.bumptech.glide.Glide
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

// TODO
//class ProductApapter(private val products: List<Product>, private val isGrid: Boolean = true) :
//    RecyclerView.Adapter<ProductApapter.ProductViewHolder>() {
//    var onItemClick: ((ProductApapter.ProductViewHolder) -> Unit)? = null
//    inner class ProductViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
//        val productImg: ImageView = listItemView.findViewById(R.id.productImg)
//        val productTitle: TextView = listItemView.findViewById(R.id.productTitle)
//        val productDescription: TextView = listItemView.findViewById(R.id.productDescription)
//        val productPrice: TextView = listItemView.findViewById(R.id.productPrice)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
//        val productCardView =
//            LayoutInflater.from(parent.context).inflate(if(isGrid) R.layout.product_card_view_grid else R.layout.product_card_view_linear, parent, false)
//        return ProductViewHolder(productCardView)
//    }
//
//    override fun getItemCount(): Int {
//        return products.size
//    }
//
//    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
//        val product: Product = products[position]
////        holder.productImg.setImageResource(product.image)
//        holder.productTitle.text = product.name
//        holder.productDescription.text = product.description
//        holder.productPrice.text = "$${String.format("%.2f", product.price).replace(",", ".")}"
//        holder.itemView.setOnClickListener {
//            onItemClick?.invoke(holder)
//        }
//    }
//}

class ProductAdapter(private val products: List<Product>, private val isGrid: Boolean = true) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {
    var onItemClick: ((Product) -> Unit)? = null
    var onAddButtonClick: ((Product) -> Unit)? = null

    inner class ProductViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        val productImg: ImageView = listItemView.findViewById(R.id.productImg)
        val productTitle: TextView = listItemView.findViewById(R.id.productTitle)
        val productDescription: TextView = listItemView.findViewById(R.id.productDescription)
        val productPrice: TextView = listItemView.findViewById(R.id.productPrice)
        private val addButton: Button = listItemView.findViewById(R.id.addBtn)

        init {
            listItemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(products[position])
                }
            }
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
            LayoutInflater.from(parent.context).inflate(if (isGrid) R.layout.product_card_view_grid else R.layout.product_card_view_linear, parent, false)
        return ProductViewHolder(productCardView)
    }

    override fun getItemCount(): Int = products.size

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product: Product = products[position]
        with(holder) {
            // Get the reference to the storage
            val storageReference = Firebase.storage.reference.child(product.image)

            // Fetch the download URL
            storageReference.downloadUrl.addOnSuccessListener { uri ->
                // Now you have the download URL, use Glide to load it
                Glide.with(productImg.context)
                    .load(uri.toString())
                    .into(productImg)
            }.addOnFailureListener {
                // Handle any errors
            }

            productTitle.text = product.name
            productDescription.text = product.description
            productPrice.text = "$${String.format("%.2f", product.price)}"
        }
    }
}
