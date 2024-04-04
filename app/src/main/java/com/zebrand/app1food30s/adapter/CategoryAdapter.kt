package com.zebrand.app1food30s.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.data.entity.Category
import com.zebrand.app1food30s.utils.Utils.getShimmerDrawable

class CategoryAdapter(private var categories: List<Category>, private val hasUnderline: Boolean = false, private var initialPosition: Int = 0): RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    var onItemClick: ((CategoryViewHolder) -> Unit)? = null
    var lastItemClicked: CategoryViewHolder? = null

    inner class CategoryViewHolder(listItemView: View): RecyclerView.ViewHolder(listItemView) {
        val cateImg: ImageView = listItemView.findViewById(R.id.cateImg)
        val cateTitle: TextView = listItemView.findViewById(R.id.cateTitle)
        val cateUnderline: TextView = listItemView.findViewById(R.id.cateUnderline)
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
        if(position == initialPosition && hasUnderline) {
            if(lastItemClicked != null) {
                lastItemClicked?.cateUnderline?.setBackgroundResource(0)
                lastItemClicked?.cateTitle?.setTextColor(lastItemClicked!!.itemView.resources.getColor(R.color.black))
            }
            holder.cateUnderline.setBackgroundResource(R.drawable.category_underline)
            holder.cateTitle.setTextColor(holder.itemView.resources.getColor(R.color.primary))
            lastItemClicked = holder
        }
        Picasso.get().load(category.image).placeholder(getShimmerDrawable()).into(holder.cateImg)
        holder.cateTitle.text = category.name
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(holder)
            lastItemClicked = holder
        }
    }

    fun updateData(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged()
    }

    fun updateInitialPosition(newPosition: Int) {
        initialPosition = newPosition
    }
}
