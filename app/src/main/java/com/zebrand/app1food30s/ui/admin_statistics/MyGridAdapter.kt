package com.zebrand.app1food30s.ui.admin_statistics

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zebrand.app1food30s.R

// RecyclerView is a powerful and flexible widget introduced in Android's support library (now part of AndroidX) to
// display a collection of items in a list, grid, or other custom layouts.
// It's an advanced and more flexible version of ListView, designed to be more efficient and adaptable.

// An adapter class in Android development plays a crucial role in connecting data to user interface (UI) components like
// ListView, GridView, or RecyclerView. The adapter acts as a bridge between the data source and the UI component,
// responsible for converting each data item into a view that can be displayed on the screen.

// Custom adapter class for a RecyclerView
class MyGridAdapter(private val mDataset: Array<String>) : RecyclerView.Adapter<MyGridAdapter.MyViewHolder>() {

    // Inner class defining a ViewHolder, which holds references to the views for each item
    class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        // Reference to a TextView within the item layout
        val textView: TextView = view.findViewById(R.id.text_view) // Replace with the actual ID of the TextView in your item layout
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
        // Set the text of the TextView to the data of the item at the current position
        holder.textView.text = mDataset[position]
    }

    // Returns the total number of items in the dataset held by the adapter
    override fun getItemCount() = mDataset.size
}