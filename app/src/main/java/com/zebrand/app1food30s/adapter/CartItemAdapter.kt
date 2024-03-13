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

class CartItemAdapter(
    private val context: Context,
    private var items: List<Product>,
    private val onItemDeleted: (Product) -> Unit
) : RecyclerView.Adapter<CartItemAdapter.CartViewHolder>() {

    class CartViewHolder(view: View, private val onItemDeleted: (Product) -> Unit, private val items: List<Product>) : RecyclerView.ViewHolder(view) {
        val productImg: ImageView = view.findViewById(R.id.productImg)
        val productName: TextView = view.findViewById(R.id.productName)
        val productCategory: TextView = view.findViewById(R.id.productCategory)
        val productPrice: TextView = view.findViewById(R.id.productPrice)
        private val deleteBtn: ImageView = view.findViewById(R.id.deleteBtn)

        init {
            deleteBtn.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val product = items[position]
                    onItemDeleted(product)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view, onItemDeleted, items)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val product = items[position]
        with(holder) {

            val storageReference = FirebaseStorage.getInstance().reference.child(product.image)
            storageReference.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(context)
                    .load(uri.toString())
//                    .placeholder(R.drawable.placeholder) // Optional: Placeholder image
//                    .error(R.drawable.error_image) // Optional: Error image
                    .into(productImg)
            }.addOnFailureListener {
                // Optional: Log or handle any errors here
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