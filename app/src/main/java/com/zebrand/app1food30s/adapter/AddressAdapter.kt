package com.zebrand.app1food30s.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.zebrand.app1food30s.R

class AddressAdapter(context: Context, private val addresses: List<String>) :
    ArrayAdapter<String>(context, 0, addresses) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_address, parent, false)
        val textView = view.findViewById<TextView>(R.id.textViewAddress)
        textView.text = getItem(position)
        return view
    }
}
