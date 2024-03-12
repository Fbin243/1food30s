package com.zebrand.app1food30s.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.data.Product

class CartItemAdapter(private val context: Context, private var items: List<Product>) : RecyclerView.Adapter<CartItemAdapter.CartViewHolder>() {

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productImg: ImageView = view.findViewById(R.id.productImg)
        val productName: TextView = view.findViewById(R.id.productName)
        val productCategory: TextView = view.findViewById(R.id.productCategory)
        val productPrice: TextView = view.findViewById(R.id.productPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val product = items[position]
        with(holder) {
//            Log.d("image bug", product.image.toString())

            // Get the reference to the storage
            val storageReference = FirebaseStorage.getInstance().reference.child(product.image)

            // Fetch the download URL
            storageReference.downloadUrl.addOnSuccessListener { uri ->
                // Now you have the download URL, use Glide to load it
                Glide.with(context)
                    .load(uri.toString())
//                    .placeholder(R.drawable.placeholder) // Optional: Placeholder image
//                    .error(R.drawable.error_image) // Optional: Error image
                    .into(productImg)
            }.addOnFailureListener {
                // Optional: Log or handle any errors here
//                Log.e("CartItemAdapter", "Image load failed", it)
            }

            productName.text = product.name
            productPrice.text = context.getString(R.string.product_price_number, product.price)
            // TODO
            productCategory.text = "Placeholder"
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<Product>) {
        items = newItems
        // Log.d("updateItems", items.toString())
        notifyDataSetChanged()
    }
}