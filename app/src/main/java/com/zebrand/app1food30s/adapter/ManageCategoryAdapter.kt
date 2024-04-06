package com.zebrand.app1food30s.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.data.entity.Offer
import com.zebrand.app1food30s.data.entity.Category
import com.zebrand.app1food30s.data.entity.Product

class ManageCategoryAdapter(private val categories: List<Category>, private val onCategoryClick: (Category) -> Unit) :
    RecyclerView.Adapter<ManageCategoryAdapter.CategoryViewHolder>() {
    inner class CategoryViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        val categoryImg: ImageView = listItemView.findViewById(R.id.categoryImg)
        val categoryName: TextView = listItemView.findViewById(R.id.categoryName)
        val categoryNumProduct: TextView = listItemView.findViewById(R.id.noProduct)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCategoryClick(categories[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val categoryCardView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_manage_category, parent, false)
        return CategoryViewHolder(categoryCardView)
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category: Category = categories[position]
        Picasso.get().load(category.image).into(holder.categoryImg)
        holder.categoryName.text = category.name
        holder.categoryNumProduct.text = category.numProduct.toString()
    }
}