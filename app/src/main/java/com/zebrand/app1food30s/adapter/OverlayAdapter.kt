package com.zebrand.app1food30s.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zebrand.app1food30s.R

class OverlayAdapter(private val itemCount: Int) : RecyclerView.Adapter<OverlayAdapter.OverlayViewHolder>() {
    inner class OverlayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OverlayViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.overlay_item, parent, false)
        return OverlayViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemCount
    }

    override fun onBindViewHolder(holder: OverlayViewHolder, position: Int) {

    }
}