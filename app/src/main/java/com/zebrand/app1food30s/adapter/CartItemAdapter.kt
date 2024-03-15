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
import com.zebrand.app1food30s.data.model.Product
import com.zebrand.app1food30s.data.model.CartItem

class CartItemAdapter(
    private val context: Context,
    private var items: List<CartItem>,
    private val onItemDeleted: (CartItem) -> Unit,
    private val onQuantityChanged: (CartItem) -> Unit,
    private val getProductById: (String, (Product?) -> Unit) -> Unit,
    private val onUpdateTotalPrice: (Double) -> Unit
) : RecyclerView.Adapter<CartItemAdapter.CartViewHolder>() {

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productImg: ImageView = view.findViewById(R.id.productImg)
        val productName: TextView = view.findViewById(R.id.productName)
        val productCategory: TextView = view.findViewById(R.id.productCategory)
        val productPrice: TextView = view.findViewById(R.id.productPrice)
        val minusBtn: ImageView = view.findViewById(R.id.minusBtn)
        val plusBtn: ImageView = view.findViewById(R.id.plusBtn)
        val itemQuantity: TextView = view.findViewById(R.id.itemQuantity)
        val deleteBtn: ImageView = view.findViewById(R.id.deleteBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = items[position]
        // Log.d("Test00", "onBindViewHolder - cartItem: $cartItem")
        getProductById(cartItem.productId) { product ->
            product?.let { prod ->
                with(holder) {
                    // Log.d("Test00", "onBindViewHolder prod: $prod")

                    val storageReference = FirebaseStorage.getInstance().reference.child(prod.image)
                    storageReference.downloadUrl.addOnSuccessListener { uri ->
                        Glide.with(context).load(uri.toString()).into(productImg)
                    }.addOnFailureListener {
                        // Handle error, e.g., by displaying a placeholder image
                    }

                    productName.text = prod.name
                    productPrice.text = context.getString(R.string.product_price_number, prod.price)
                    productCategory.text = "Placeholder"
                    itemQuantity.text = cartItem.quantity.toString()

                    plusBtn.setOnClickListener {
                        if (cartItem.quantity < prod.stock) {
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

                    deleteBtn.setOnClickListener {
                        onItemDeleted(cartItem)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<CartItem>) {
        items = newItems
        notifyDataSetChanged()

//        // Direct calculation of total price if CartItem includes price
//        val totalPrice = items.sumOf { cartItem ->
//            // Assuming cartItem includes a price field or you've fetched the price beforehand
//            cartItem.quantity * cartItem.productPrice
//        }
//
//        // Update the UI with the total price
//        onUpdateTotalPrice(totalPrice)
    }
}