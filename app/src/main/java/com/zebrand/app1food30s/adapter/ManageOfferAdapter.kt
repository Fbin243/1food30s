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
import com.zebrand.app1food30s.data.entity.Product

class ManageOfferAdapter(private val offers: List<Offer>, private val onOfferClick: (Offer) -> Unit) :
    RecyclerView.Adapter<ManageOfferAdapter.OfferViewHolder>() {
    inner class OfferViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        val offerImg: ImageView = listItemView.findViewById(R.id.offerImg)
        val offerName: TextView = listItemView.findViewById(R.id.offerName)
        val offerRate: TextView = listItemView.findViewById(R.id.offerRate)
        val offerNumProduct: TextView = listItemView.findViewById(R.id.noProduct)
        val offerDate: TextView = listItemView.findViewById(R.id.productDate)
        val editBtn: Button = listItemView.findViewById(R.id.addBtn)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onOfferClick(offers[position])
                }
            }
            editBtn.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onOfferClick(offers[position])
                }
            }
        }
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
        Picasso.get().load(offer.image).into(holder.offerImg)
        holder.offerName.text = offer.name
//        holder.offerRate.text = offer.discountRate.toString()

        holder.offerRate.text = "${offer.discountRate}%"
        holder.offerNumProduct.text = offer.numProduct.toString()

        // Display time and date with "-" as the separator
        offer.date?.let {
            // Định dạng giờ và ngày để giờ đứng trước và ngày theo sau, được ngăn cách bằng "-"
            val dateTimeFormat = java.text.SimpleDateFormat("HH:mm:ss  dd/MM/yyyy", java.util.Locale.getDefault())
            holder.offerDate.text = dateTimeFormat.format(it)
        } ?: run {
            // Handle case where date is null
            holder.offerDate.text = "No date provided"
        }
    }
}