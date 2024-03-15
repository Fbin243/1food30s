package com.zebrand.app1food30s.ui.cart_checkout

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.zebrand.app1food30s.data.model.Cart
import com.zebrand.app1food30s.data.model.CartItem
import com.zebrand.app1food30s.data.model.DetailedCartItem
import com.zebrand.app1food30s.data.model.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

interface CartView {
    fun displayCartItems(detailedCartItems: List<DetailedCartItem>)
    fun displayTotalPrice(totalPrice: Double)
    fun displayError(error: String)
}

class CartPresenter(private val view: CartView) : CoroutineScope by CoroutineScope(Dispatchers.IO) {
    private val db = FirebaseFirestore.getInstance()
    private val cartId = "mdXn8lvirHaAogStOY1K"

    fun listenToCartChanges() {
        db.collection("carts").document(cartId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    view.displayError(e.message ?: "Unknown error")
                    return@addSnapshotListener
                }

                val cart = snapshot?.toObject(Cart::class.java)
                cart?.items?.let { cartItems ->
                    fetchProductDetailsForCartItems(cartItems) { detailedCartItems ->
                        view.displayCartItems(detailedCartItems)
                        val totalPrice = detailedCartItems.sumOf { it.productPrice * it.quantity }
                        view.displayTotalPrice(totalPrice)
                    }
                } ?: view.displayError("Cart not found")
            }
    }

    private fun fetchProductDetailsForCartItem(cartItem: CartItem): Task<DetailedCartItem?> {
        return cartItem.productId?.get()?.continueWithTask { task ->
            if (task.isSuccessful) {
                val product = task.result.toObject(Product::class.java)
                if (product != null) {
                    // Here you fetch the URL from Firebase Storage
                    val storageReference = FirebaseStorage.getInstance().reference.child(product.image)
                    storageReference.downloadUrl.continueWithTask { urlTask ->
                        if (urlTask.isSuccessful) {
                            val imageUrl = urlTask.result.toString()
                            val detailedCartItem = DetailedCartItem(
                                productName = product.name ?: "",
                                productPrice = product.price ?: 0.0,
                                productImage = imageUrl,
                                quantity = cartItem.quantity
                            )
                            Tasks.forResult(detailedCartItem)
                        } else {
                            // Handle error in fetching image URL
                            Tasks.forResult<DetailedCartItem?>(null)
                        }
                    }
                } else {
                    Tasks.forResult(null)
                }
            } else {
                task.exception?.let { exception ->
                    Tasks.forException<DetailedCartItem?>(exception)
                } ?: Tasks.forResult(null)
            }
        } ?: Tasks.forResult(null)
    }

    private fun fetchProductDetailsForCartItems(cartItems: List<CartItem>, callback: (List<DetailedCartItem>) -> Unit) {
        val tasks = cartItems.map { cartItem ->
            fetchProductDetailsForCartItem(cartItem)
        }

        Tasks.whenAllSuccess<DetailedCartItem>(tasks).addOnSuccessListener { detailedCartItems ->
            // detailedCartItems contains all DetailedCartItem objects
            callback(detailedCartItems)
        }.addOnFailureListener { exception ->
            // Handle error
        }
    }

    private fun calculateTotalPrice(items: List<CartItem>) {
        launch {
            var totalPrice = 0.0
            items.forEach { cartItem ->
                val productRef = cartItem.productId
                val product = productRef?.get()?.await()?.toObject(Product::class.java)
                product?.let {
                    totalPrice += it.price * cartItem.quantity
                }
            }
            withContext(Dispatchers.Main) {
                view.displayTotalPrice(totalPrice)
            }
        }
    }
}