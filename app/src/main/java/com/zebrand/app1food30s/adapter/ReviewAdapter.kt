package com.zebrand.app1food30s.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.data.entity.Review
import com.zebrand.app1food30s.utils.Utils

class ReviewAdapter(private val reviews: List<Review>): RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {
    inner class ReviewViewHolder(listItemView: View): RecyclerView.ViewHolder(listItemView) {
        val avatar: ImageView = listItemView.findViewById(R.id.avatar)
        val name: TextView = listItemView.findViewById(R.id.username)
        val rating: ViewGroup = listItemView.findViewById(R.id.rating)
        val content: TextView = listItemView.findViewById(R.id.content)
        val date: TextView = listItemView.findViewById(R.id.date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        return ReviewViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.review_card_view, parent, false))
    }

    override fun getItemCount(): Int {
        return reviews.size
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.name.text = review.name
        holder.content.text = review.content
        holder.date.text = Utils.formatDate(review.date)
        Picasso.get().load(review.avatar).placeholder(Utils.getShimmerDrawable()).into(holder.avatar)
    }
}