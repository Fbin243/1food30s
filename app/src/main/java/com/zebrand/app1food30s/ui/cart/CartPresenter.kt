package com.zebrand.app1food30s.ui.cart

import android.content.Context
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.data.entity.Cart
import com.zebrand.app1food30s.data.entity.CartItem
import com.zebrand.app1food30s.utils.FireStoreUtils
import com.zebrand.app1food30s.utils.FireStoreUtils.mDBCartRef
import com.zebrand.app1food30s.utils.FirebaseUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CartPresenter(private val view: CartMVPView, private val userId: String, context: Context) : CoroutineScope by CoroutineScope(Dispatchers.IO) {
    private var cartRef: DocumentReference? = null
    private val repository = CartRepository(FirebaseFirestore.getInstance())

    init {
        setUpCart()
    }

    private fun setUpCart() {
        repository.getCartRef(userId) { ref ->
            cartRef = ref
            view.showShimmerEffectForCart()
            loadCart()
        }
    }


    private fun loadCart() {
        cartRef?.let { ref ->
            repository.loadCart(ref, onResult = { detailedCartItems ->
                if (detailedCartItems != null) {
                    if (detailedCartItems.isNotEmpty()) {
                        view.hideShimmerEffectForCart()
                        view.loadCart(detailedCartItems)
                    } else {
                        // TODO: this is called when checkout clears cart
                        view.hideShimmerEffectForCart()
                        view.displayEmptyCart()
                    }
                }
            }, onError = { error ->
                view.displayError(error)
            })
        } ?: run {
            view.displayError("Cart reference not found.")
        }
    }

    fun updateCartOnExit(cartItems: List<CartItem>) {
        val cartRef = mDBCartRef.document(userId)

        FirebaseUtils.fireStore.runTransaction { transaction ->
            val cartSnapshot = transaction.get(cartRef)
            if (cartSnapshot.exists()) {
                val cart = cartSnapshot.toObject(Cart::class.java)
                cart?.let {
                    // A list of items currently in Firestore
                    val firestoreItems = it.items.toMutableList()

                    // Identify items to be removed
                    val newCartItemIds = cartItems.mapNotNull { it.productId?.id }.toSet()
                    val itemsToRemove = firestoreItems.filter { item -> !newCartItemIds.contains(item.productId?.id) }

                    // Remove items that are no longer present
                    firestoreItems.removeAll { item -> itemsToRemove.contains(item) }

                    // Update quantities of existing items
                    firestoreItems.forEach { item ->
                        cartItems.find { ci -> ci.productId?.id == item.productId?.id }?.also { newItem ->
                            item.quantity = newItem.quantity
                        }
                    }

                    // Add new items that are not in Firestore
//                    cartItems.filter { newItem -> newItem.productId?.id !in firestoreItems.map { it.productId?.id } }
//                        .forEach { newItem ->
//                            firestoreItems.add(newItem)
//                        }

                    // Set the updated list back to Firestore
                    transaction.set(cartRef, mapOf("items" to firestoreItems))
                }
            } else {
                // Optionally create a new cart if it doesn't exist
                Log.d("CartAdapter", "No cart to update: ${cartRef.path}")
            }
        }.addOnSuccessListener {
            Log.d("CartAdapter", "Transaction success for cart: ${userId}")
        }.addOnFailureListener { e ->
            Log.e("CartAdapter", "Transaction failure for cart: ${userId}", e)
        }
    }

//    fun updateCartOnExit(cartItems: List<CartItem>) {
//        val cartRef = mDBCartRef.document(userId)
//
//        FirebaseUtils.fireStore.runTransaction { transaction ->
//            val cartSnapshot = transaction.get(cartRef)
//            if (cartSnapshot.exists()) {
//                val cart = cartSnapshot.toObject(Cart::class.java)
//                cart?.let {
//                    it.items.forEach { item ->
//                        val cartItemToUpdate = cartItems.find { ci -> ci.productId?.id == item.productId?.id }
//                        if (cartItemToUpdate != null) {
//                            item.quantity = cartItemToUpdate.quantity
//                        }
//                    }
//
//                    transaction.set(cartRef, it)
//                }
//            } else {
//                Log.d("CartAdapter", "No cart to update: ${cartRef.path}")
//            }
//        }.addOnSuccessListener {
//            Log.d("CartAdapter", "Transaction success for cart: ${userId}")
//        }.addOnFailureListener { e ->
//            Log.e("CartAdapter", "Transaction failure for cart: ${userId}", e)
//        }
//    }

    fun removeFromCart(productRef: DocumentReference) {
        cartRef?.let { ref ->
            repository.removeFromCart(ref, productRef) { isSuccess, isEmpty ->
                if (isSuccess) {
                    if (isEmpty) {
                        // If the operation was successful and the cart is now empty, display the empty cart UI
                        CoroutineScope(Dispatchers.Main).launch {
                            view.displayEmptyCart()
                        }
                    } else {
                        // If the operation was successful but the cart is not empty, perform other UI updates as necessary
                    }
                } else {
                    // If the operation failed, display an error
                    CoroutineScope(Dispatchers.Main).launch {
                        view.displayError("Failed to remove item")
                    }
                }
            }
        }
    }

//    fun removeFromCart(cartRef: DocumentReference, productRef: DocumentReference) {
//        repository.removeFromCart(cartRef, productRef) { isSuccess, isEmpty ->
//            if (isSuccess) {
//                if (isEmpty) {
//                    // If the operation was successful and the cart is now empty, display the empty cart UI
//                    CoroutineScope(Dispatchers.Main).launch {
//                        view.displayEmptyCart()
//                    }
//                } else {
//                    // If the operation was successful but the cart is not empty, perform other UI updates as necessary
//                }
//            } else {
//                // If the operation failed, display an error
//                CoroutineScope(Dispatchers.Main).launch {
//                    view.displayError("Failed to remove item")
//                }
//            }
//        }
//    }

    fun updateCartItemQuantity(productRef: DocumentReference, newQuantity: Int) {
        cartRef?.let { ref ->
            repository.updateCartItemQuantity(ref, productRef, newQuantity) { success ->
                if (success) {
                    // TODO
                    // Update UI accordingly
                } else {
                    view.displayError("Failed to update quantity")
                }
            }
        }
    }

//    private fun listenToCartChanges() {
//        repository.listenToCartChanges(userId, onResult = { detailedCartItems, _ ->
//            if (detailedCartItems != null) {
//                view.displayCartItems(detailedCartItems)
//            } else {
//                view.displayError("Failed to fetch cart details.")
//            }
//        }, onError = { error ->
//            view.displayError(error)
//        })
//    }
}