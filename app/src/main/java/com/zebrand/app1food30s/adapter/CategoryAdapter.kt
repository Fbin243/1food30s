package com.zebrand.app1food30s.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.model.Category

class CategoryAdapter(private val categories: List<Category>): RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    inner class CategoryViewHolder(listItemView: View): RecyclerView.ViewHolder(listItemView) {
        val cateImg: ImageView = listItemView.findViewById(R.id.cateImg)
        val cateTitle: TextView = listItemView.findViewById(R.id.cateTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val categoryCardView = inflater.inflate(R.layout.category_card_view, parent, false)
        return CategoryViewHolder(categoryCardView)
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category: Category = categories[position]
        holder.cateImg.setImageResource(category.img)
        holder.cateTitle.text = category.title
    }
}
