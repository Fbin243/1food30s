package com.zebrand.app1food30s.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.data.Product
import com.zebrand.app1food30s.data.Category
import kotlinx.coroutines.tasks.await

class ManageProductAdapter(private val products: List<Product>, private val isGrid: Boolean = true) :
    RecyclerView.Adapter<ManageProductAdapter.ProductViewHolder>() {
    var onItemClick: ((ManageProductAdapter.ProductViewHolder) -> Unit)? = null
    inner class ProductViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        val productImg: ImageView = listItemView.findViewById(R.id.productImg)
        val productTitle: TextView = listItemView.findViewById(R.id.productTitle)
        val productDescription: TextView = listItemView.findViewById(R.id.productDescription)
        val productPrice: TextView = listItemView.findViewById(R.id.productPrice)
        val productDate: TextView = listItemView.findViewById(R.id.productDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val productCardView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_manage_product, parent, false)
        return ProductViewHolder(productCardView)
    }

    override fun getItemCount(): Int {
        return products.size
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product: Product = products[position]
        Picasso.get().load(product.image).into(holder.productImg)
//        holder.productImg.setImageResource(product.image)
        holder.productTitle.text = product.name
        val categoryId = product.idCategory?.id ?: "non"
        val categoriesCollection = FirebaseFirestore.getInstance().collection("categories")
        categoriesCollection.document(categoryId).get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
//                val category = document.toObject(Category::class.java)
                holder.productDescription.text = document.getString("name") ?: ""
            } else {
                holder.productDescription.text = "Danh mục không tồn tại"
            }
        }.addOnFailureListener {
            holder.productDescription.text = "Lỗi khi lấy danh mục"
        }

        // Display time and date with "-" as the separator
        product.date?.let {
            // Định dạng giờ và ngày để giờ đứng trước và ngày theo sau, được ngăn cách bằng "-"
            val dateTimeFormat = java.text.SimpleDateFormat("HH:mm:ss  dd/MM/yyyy", java.util.Locale.getDefault())
            holder.productDate.text = dateTimeFormat.format(it)
        } ?: run {
            // Handle case where date is null
            holder.productDate.text = "No date provided"
        }

        holder.productPrice.text = "$${String.format("%.2f", product.price).replace(",", ".")}"
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(holder)
        }
    }
}