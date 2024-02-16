package com.zebrand.app1food30s.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.data.Offer

class OfferAdapter(private val offers: List<Offer>): RecyclerView.Adapter<OfferAdapter.OfferViewHolder>() {
    inner class OfferViewHolder(listItemView: View): RecyclerView.ViewHolder(listItemView) {
        val offerImg: ImageView = listItemView.findViewById(R.id.offerImg)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferViewHolder {
        val offerCardView: View = LayoutInflater.from(parent.context).inflate(R.layout.offer_card_view, parent, false)
        return OfferViewHolder(offerCardView)
    }

    override fun getItemCount(): Int {
        return offers.size
    }

    override fun onBindViewHolder(holder: OfferViewHolder, position: Int) {
        val offer = offers[position]
        holder.offerImg.setImageResource(offer.img)
    }
}