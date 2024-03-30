package com.zebrand.app1food30s.ui.cart

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.zebrand.app1food30s.data.Cart
import com.zebrand.app1food30s.data.DetailedCartItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CartPresenter(private val view: CartMVPView) : CoroutineScope by CoroutineScope(Dispatchers.IO) {
    private val db = FirebaseFirestore.getInstance()
    private val cartId = "mdXn8lvirHaAogStOY1K"
    private var detailedCartItems: List<DetailedCartItem> = emptyList()
    private var totalPrice: Double = 0.0
    private val cartRepository = CartRepository(db)

    fun listenToCartChanges() {
        db.collection("carts").document(cartId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    view.displayError(e.message ?: "Unknown error")
                    return@addSnapshotListener
                }

                val cart = snapshot?.toObject(Cart::class.java)
                cart?.items?.let { cartItems ->
                    cartRepository.fetchProductDetailsForCartItems(cartId) { detailedCartItems, totalPrice ->
                        if (detailedCartItems != null) {
                            launch(Dispatchers.Main) {
                                view.displayCartItems(detailedCartItems)
//                                view.displayTotalPrice(totalPrice)
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

//    private fun fetchProductDetailsForCartItem(cartItem: CartItem): Task<DetailedCartItem?> {
//        // Ensure cartItem.productId is correctly initialized as a DocumentReference
//        if (cartItem.productId == null) {
////            Log.d("Test00", "fetchProductDetailsForCartItem: ProductId is null")
//            return Tasks.forResult(null)
//        }
//
//        return cartItem.productId.get().continueWithTask { task ->
//            if (task.isSuccessful) {
//                val product = task.result.toObject(Product::class.java)
//                if (product != null) {
//                    // No need to recreate a DocumentReference; use the existing one
//                    val productDocumentReference = cartItem.productId
//
//                    // Here you fetch the URL from Firebase Storage
//                    val storageReference = FirebaseStorage.getInstance().reference.child(product.image)
//                    return@continueWithTask storageReference.downloadUrl.continueWithTask { urlTask ->
//                        if (urlTask.isSuccessful) {
//                            val imageUrl = urlTask.result.toString()
//                            val detailedCartItem = DetailedCartItem(
//                                productId = productDocumentReference, // Directly use the existing DocumentReference
//                                productCategory = "Food",
//                                productName = product.name ?: "",
//                                productPrice = product.price ?: 0.0,
//                                productImage = imageUrl,
//                                productStock = product.stock,
//                                quantity = cartItem.quantity,
//                            )
//                            Tasks.forResult(detailedCartItem)
//                        } else {
//                            Tasks.forResult<DetailedCartItem?>(null)
//                        }
//                    }
//                } else {
////                    Log.d("Test00", "Product is null")
//                    return@continueWithTask Tasks.forResult<DetailedCartItem?>(null)
//                }
//            } else {
////                Log.e("Test00", "Error fetching product details: ", task.exception)
//                return@continueWithTask task.exception?.let {
//                    Tasks.forException<DetailedCartItem?>(
//                        it
//                    )
//                }
//            }
//        }
//    }
//
//    private fun fetchProductDetailsForCartItems(cartItems: List<CartItem>, callback: (List<DetailedCartItem>) -> Unit) {
//        val tasks = cartItems.map { cartItem ->
//            fetchProductDetailsForCartItem(cartItem)
//        }
//
//        Tasks.whenAllSuccess<DetailedCartItem>(tasks).addOnSuccessListener { detailedCartItems ->
//            this.detailedCartItems = detailedCartItems
//            this.totalPrice = detailedCartItems.sumOf { it.productPrice * it.quantity }
//            callback(detailedCartItems)
//        }.addOnFailureListener { exception ->
//            // Handle error
//        }
//    }

    fun removeFromCart(productRef: DocumentReference) {
        val cartRef = db.collection("carts").document(cartId)

        launch {
            try {
                val cartSnapshot = cartRef.get().await()
                val cart = cartSnapshot.toObject(Cart::class.java)
                cart?.let {
                    it.items.removeAll { item -> item.productId == productRef }
                    cartRef.set(it).await()
                    withContext(Dispatchers.Main) {
                        view.refreshCart(productRef) // Pass the productRef to refreshCart
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view.displayError(e.message ?: "Unknown error")
                }
            }
        }
    }

    fun updateCartItemQuantity(productRef: DocumentReference, newQuantity: Int) {
        launch {
            try {
                // Your existing logic to find the cart item and update its quantity
                val cartRef = db.collection("carts").document(cartId)
                val cartSnapshot = cartRef.get().await()
                val cart = cartSnapshot.toObject(Cart::class.java)
                cart?.items?.find { it.productId == productRef }?.let {
                    it.quantity = newQuantity
                    cartRef.set(cart).await() // Save the updated cart
                }
            } catch (e: Exception) {
                Log.e("CartPresenter", "Error updating item quantity", e)
            }
        }
    }

    fun getCartSummary(): Pair<List<String>, Double> {
        val itemDescriptions = detailedCartItems.map { item ->
            "${item.productImage} - ${item.productName} - ${item.productCategory} - \$${item.productPrice} - ${item.quantity}"
        }
        val totalPrice = detailedCartItems.sumOf { it.productPrice * it.quantity }
        return Pair(itemDescriptions, totalPrice)
    }
}