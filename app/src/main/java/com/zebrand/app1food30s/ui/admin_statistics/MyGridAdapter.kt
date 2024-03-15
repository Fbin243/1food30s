package com.zebrand.app1food30s.ui.admin_statistics

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import com.zebrand.app1food30s.R

class MyGridAdapter(private val mDataset: Array<GridItem>) :
    RecyclerView.Adapter<MyGridAdapter.MyViewHolder>() {

    // Inner class defining a ViewHolder, which holds references to the views for each item
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.image_view)
        val textView: TextView = view.findViewById(R.id.text_view)
        val numberView: TextView = view.findViewById(R.id.number_view)
    }

    // Called when RecyclerView needs a new ViewHolder of the given type to represent an item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // Inflate the custom layout (item_card_view.xml) for the item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card_view, parent, false)
        // Return a new ViewHolder instance
        return MyViewHolder(view)
    }

    // Called by RecyclerView to display the data at the specified position
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = mDataset[position]
        holder.textView.text = item.text
        holder.numberView.text = item.number.toString()
        holder.imageView.setImageResource(item.imageResId)
    }

    // Returns the total number of items in the dataset held by the adapter
    override fun getItemCount() = mDataset.size

    // Define the GridItem data class outside the MyGridAdapter class
    data class GridItem(
        val text: String,
        val number: Int,
        @DrawableRes val imageResId: Int
    )
}

class GridSpacingItemDecoration(private val spanCount: Int, private val spacing: Int, private val includeEdge: Boolean) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view) // item position
        val column = position % spanCount // item column

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount

            if (position < spanCount) { // top edge
                outRect.top = spacing
            }
            outRect.bottom = spacing // item bottom
        } else {
            outRect.left = column * spacing / spanCount
            outRect.right = spacing - (column + 1) * spacing / spanCount
            if (position >= spanCount) {
                outRect.top = spacing // item top
            }
        }
    }
}
