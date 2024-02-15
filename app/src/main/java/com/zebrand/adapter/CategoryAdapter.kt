package com.zebrand.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zebrand.app1food30s.R
import com.zebrand.model.Category

class CategoryAdapter(private val categories: List<Category>): RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    inner class CategoryViewHolder(listItemView: View): RecyclerView.ViewHolder(listItemView) {
        val cateImg = listItemView.findViewById<ImageView>(R.id.cateImg)
        val cateTitle = listItemView.findViewById<TextView>(R.id.cateTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val categoryCardView = inflater.inflate(R.layout.category_card_view, parent, false)
        val categoryViewHolder = CategoryViewHolder(categoryCardView)
        return categoryViewHolder
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category: Category = categories.get(position)
        holder.cateImg.setImageResource(category.img)
        holder.cateTitle.text = category.title
    }
}
