package com.zebrand.app1food30s.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.data.Offer

class ManageOfferAdapter(private val offers: List<Offer>, private val isGrid: Boolean = true) :
    RecyclerView.Adapter<ManageOfferAdapter.OfferViewHolder>() {
    var onItemClick: ((ManageOfferAdapter.OfferViewHolder) -> Unit)? = null
    inner class OfferViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        val offerImg: ImageView = listItemView.findViewById(R.id.offerImg)
        val offerName: TextView = listItemView.findViewById(R.id.offerName)
        val offerRate: TextView = listItemView.findViewById(R.id.offerRate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferViewHolder {
        val offerCardView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_manage_offer, parent, false)
        return OfferViewHolder(offerCardView)
    }

    override fun getItemCount(): Int {
        return offers.size
    }

    override fun onBindViewHolder(holder: OfferViewHolder, position: Int) {
        val offer: Offer = offers[position]
        holder.offerImg.setImageResource(offer.img)
        holder.offerName.text = offer.name
        holder.offerRate.text = offer.discountRate.toString()
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(holder)
        }
    }
}