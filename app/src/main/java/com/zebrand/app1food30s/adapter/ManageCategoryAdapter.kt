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
import com.zebrand.app1food30s.data.entity.Category
import com.zebrand.app1food30s.data.entity.Product
import com.zebrand.app1food30s.utils.Utils

class ManageCategoryAdapter(private val categories: List<Category>, private val onCategoryClick: (Category) -> Unit) :
    RecyclerView.Adapter<ManageCategoryAdapter.CategoryViewHolder>() {
    inner class CategoryViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        val categoryImg: ImageView = listItemView.findViewById(R.id.categoryImg)
        val categoryName: TextView = listItemView.findViewById(R.id.categoryName)
        val categoryNumProduct: TextView = listItemView.findViewById(R.id.noProduct)
        val categoryDate: TextView = listItemView.findViewById(R.id.productDate)
        val editBtn: Button = listItemView.findViewById(R.id.addBtn)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCategoryClick(categories[position])
                }
            }
            editBtn.setOnClickListener {
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
        Picasso.get().load(category.image).placeholder(Utils.getShimmerDrawable()).into(holder.categoryImg)
        holder.categoryName.text = category.name
        holder.categoryNumProduct.text = category.numProduct.toString()

        // Display time and date with "-" as the separator
        category.date?.let {
            // Định dạng giờ và ngày để giờ đứng trước và ngày theo sau, được ngăn cách bằng "-"
            val dateTimeFormat = java.text.SimpleDateFormat("HH:mm:ss  dd/MM/yyyy", java.util.Locale.getDefault())
            holder.categoryDate.text = dateTimeFormat.format(it)
        } ?: run {
            // Handle case where date is null
            holder.categoryDate.text = "No date provided"
        }
    }
}