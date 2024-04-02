package com.zebrand.app1food30s.ui.cart

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.zebrand.app1food30s.data.entity.Cart
import com.zebrand.app1food30s.data.entity.DetailedCartItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CartPresenter(private val view: CartMVPView, private val userId: String) : CoroutineScope by CoroutineScope(Dispatchers.IO) {
    private val db = FirebaseFirestore.getInstance()
    private val cartRepository = CartRepository(db)

//    init {
//        ensureCartExists()
//    }
//
//    private fun ensureCartExists() {
//        launch {
//            val cartRef = db.collection("carts").document(userId)
//            val cartSnapshot = cartRef.get().await()
//            if (!cartSnapshot.exists()) {
//                cartRef.set(Cart()).await()
////                Log.d("CartPresenter", "New cart created for user: $userId")
//            }
//        }
//    }

    fun listenToCartChanges() {
        db.collection("carts").document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    view.displayError(e.message ?: "Unknown error")
                    return@addSnapshotListener
                }

                val cart = snapshot?.toObject(Cart::class.java)
                cart?.items?.let {
                    cartRepository.fetchProductDetailsForCartItems(userId) { detailedCartItems, totalPrice ->
                        if (detailedCartItems != null) {
                            launch(Dispatchers.Main) {
                                view.displayCartItems(detailedCartItems)
                                // Optionally view.displayTotalPrice(totalPrice)
                            }
                        } else {
                            launch(Dispatchers.Main) {
                                view.displayError("Failed to fetch cart details")
                            }
                        }
                    }
                } ?: view.displayError("Cart not found")
            }
    }

    fun removeFromCart(productRef: DocumentReference) {
        val cartRef = db.collection("carts").document(userId)

        launch {
            try {
                val cartSnapshot = cartRef.get().await()
                val cart = cartSnapshot.toObject(Cart::class.java)
                cart?.let {
                    it.items.removeAll { item -> item.productId == productRef }
                    cartRef.set(it).await()
                    withContext(Dispatchers.Main) {
                        view.refreshCart(productRef)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view.displayError(e.message ?: "Unknown error")
                }
            }
        }
    }

//    fun updateCartItemQuantity(productRef: DocumentReference, newQuantity: Int) {
//        launch {
//            try {
//                // Your existing logic to find the cart item and update its quantity
//                val cartRef = db.collection("carts").document(cartId)
//                val cartSnapshot = cartRef.get().await()
//                val cart = cartSnapshot.toObject(Cart::class.java)
//                cart?.items?.find { it.productId == productRef }?.let {
//                    it.quantity = newQuantity
//                    cartRef.set(cart).await() // Save the updated cart
//                }
//            } catch (e: Exception) {
//                Log.e("CartPresenter", "Error updating item quantity", e)
//            }
//        }
//    }

    fun updateCartItemQuantity(productRef: DocumentReference, newQuantity: Int) {
        launch {
            try {
                val cartRef = db.collection("accounts").document(userId).collection("carts").document("default")
                val cartSnapshot = cartRef.get().await()
                val cart = cartSnapshot.toObject(Cart::class.java)

                cart?.let {
                    it.items.find { cartItem -> cartItem.productId == productRef }?.let { foundCartItem ->
                        foundCartItem.quantity = newQuantity
                        cartRef.set(it).await() // Update the cart with the new quantity
                        // Optionally, update the UI or log success here if needed
                    } ?: run {
                        // Handle the case where the cart item was not found
                        Log.e("CartPresenter", "Cart item not found for update")
                    }
                } ?: run {
                    // Handle the case where the cart was not found
                    Log.e("CartPresenter", "Cart not found for user")
                }
            } catch (e: Exception) {
                Log.e("CartPresenter", "Error updating item quantity", e)
                // Optionally, update the UI or log the error here if needed
            }
        }
    }
}