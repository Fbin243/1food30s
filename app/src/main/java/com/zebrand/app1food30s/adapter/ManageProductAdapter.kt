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

class ManageProductAdapter(private val products: List<Product>, private val isGrid: Boolean = true) :
    RecyclerView.Adapter<ManageProductAdapter.ProductViewHolder>() {
    var onItemClick: ((ManageProductAdapter.ProductViewHolder) -> Unit)? = null
    inner class ProductViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        val productImg: ImageView = listItemView.findViewById(R.id.productImg)
        val productTitle: TextView = listItemView.findViewById(R.id.productTitle)
        val productDescription: TextView = listItemView.findViewById(R.id.productDescription)
        val productPrice: TextView = listItemView.findViewById(R.id.productPrice)
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
//        holder.productDescription.text = product.idCategory
        // Lấy tên danh mục từ Firestore
        // Sử dụng ID danh mục để truy vấn Firestore và lấy tên danh mục
//        val categoryId = product.idCategory.id
        // Giả định rằng bạn có một bộ sưu tập tên là "categories" trong Firestore
//        val categoriesCollection = FirebaseFirestore.getInstance().collection("categories")
//        categoriesCollection.document(categoryId).get().addOnSuccessListener { document ->
//            if (document != null && document.exists()) {
//                val category = document.toObject(Category::class.java)
//                holder.productDescription.text = category?.name ?: "Danh mục không xác định"
//            } else {
//                holder.productDescription.text = "Danh mục không tồn tại"
//            }
//        }.addOnFailureListener {
//            holder.productDescription.text = "Lỗi khi lấy danh mục"
//        }
        holder.productPrice.text = "$${String.format("%.2f", product.price).replace(",", ".")}"
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(holder)
        }
    }
}