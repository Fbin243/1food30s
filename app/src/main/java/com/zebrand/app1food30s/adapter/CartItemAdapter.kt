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
    private var items: List<CartItem>,
    private val onItemDeleted: (CartItem) -> Unit,
    private val onQuantityChanged: (CartItem) -> Unit
) : RecyclerView.Adapter<CartItemAdapter.CartViewHolder>() {

    data class CartItem(
        val product: Product,
        var quantity: Int = 1
    )

    class CartViewHolder(view: View, private val onItemDeleted: (CartItem) -> Unit, private val items: List<CartItem>) : RecyclerView.ViewHolder(view) {
        val productImg: ImageView = view.findViewById(R.id.productImg)
        val productName: TextView = view.findViewById(R.id.productName)
        val productCategory: TextView = view.findViewById(R.id.productCategory)
        val productPrice: TextView = view.findViewById(R.id.productPrice)
        val minusBtn: ImageView = view.findViewById(R.id.minusBtn)
        val plusBtn: ImageView = view.findViewById(R.id.plusBtn)
        val itemQuantity: TextView = view.findViewById(R.id.itemQuantity)
        private val deleteBtn: ImageView = view.findViewById(R.id.deleteBtn)

        init {
            deleteBtn.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val cartItem = items[position]
                    onItemDeleted(cartItem)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view, onItemDeleted, items)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = items[position]
        with(holder) {

            val storageReference = FirebaseStorage.getInstance().reference.child(cartItem.product.image)
            storageReference.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(context)
                    .load(uri.toString())
//                    .placeholder(R.drawable.placeholder) // Optional: Placeholder image
//                    .error(R.drawable.error_image) // Optional: Error image
                    .into(productImg)
            }.addOnFailureListener {
                // Optional: Log or handle any errors here
            }

            productName.text = cartItem.product.name
            productPrice.text = context.getString(R.string.product_price_number, cartItem.product.price)
            // TODO
            productCategory.text = "Placeholder"
            itemQuantity.text = cartItem.quantity.toString()

            plusBtn.setOnClickListener {
                if (cartItem.quantity < cartItem.product.stock) {
                    cartItem.quantity++
                    itemQuantity.text = cartItem.quantity.toString()
                    onQuantityChanged(cartItem)
                }
            }

            minusBtn.setOnClickListener {
                if (cartItem.quantity > 1) {
                    cartItem.quantity--
                    itemQuantity.text = cartItem.quantity.toString()
                    onQuantityChanged(cartItem)
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<CartItem>) {
        items = newItems
//        Log.d("SharedViewModel", "updateItems$items")
        notifyDataSetChanged()
    }
}