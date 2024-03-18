package com.zebrand.app1food30s.ui.cart

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.zebrand.app1food30s.data.Cart
import com.zebrand.app1food30s.data.CartItem
import com.zebrand.app1food30s.data.DetailedCartItem
import com.zebrand.app1food30s.data.Product

class CartRepository(private val db: FirebaseFirestore) {

    fun fetchProductDetailsForCartItems(cartId: String, callback: (List<DetailedCartItem>?, Double) -> Unit) {
        db.collection("carts").document(cartId).get().addOnSuccessListener { documentSnapshot ->
            val cart = documentSnapshot.toObject(Cart::class.java)
            cart?.let { cart ->
                val tasks = cart.items.mapNotNull { cartItem ->
                    fetchProductDetailsForCartItem(cartItem)
                }
                Tasks.whenAllSuccess<DetailedCartItem>(tasks).addOnSuccessListener { detailedCartItems ->
                    val totalPrice = detailedCartItems.sumOf { item -> item.productPrice * item.quantity }
                    callback(detailedCartItems, totalPrice)
                }.addOnFailureListener {
                    callback(null, 0.0)
                }
            }
        }.addOnFailureListener {
            callback(null, 0.0)
        }
    }

    private fun fetchProductDetailsForCartItem(cartItem: CartItem): Task<DetailedCartItem?> {
        if (cartItem.productId == null) {
            return Tasks.forResult(null) // Early return if productId is null
        }

        return cartItem.productId.get().continueWithTask { task ->
            if (task.isSuccessful) {
                val product = task.result.toObject(Product::class.java)
                if (product != null) {
                    val storageReference = FirebaseStorage.getInstance().reference.child(product.image)
                    return@continueWithTask storageReference.downloadUrl.continueWithTask { urlTask ->
                        if (urlTask.isSuccessful) {
                            val imageUrl = urlTask.result.toString()
                            val detailedCartItem = DetailedCartItem(
                                productId = cartItem.productId, // Use DocumentReference's ID as a string
                                productCategory = "Food", // Example category, adjust as necessary
                                productName = product.name,
                                productPrice = product.price,
                                productImage = imageUrl, // Now using the URL string
                                productStock = product.stock,
                                quantity = cartItem.quantity
                            )
                            Tasks.forResult(detailedCartItem)
                        } else {
//                            Log.e("CartRepository", "Error fetching image URL: ${urlTask.exception}")
                            Tasks.forException<DetailedCartItem?>(urlTask.exception ?: Exception("Failed to fetch image URL"))
                        }
                    }
                } else {
                    return@continueWithTask Tasks.forResult(null)
                }
            } else {
                task.exception?.let { exception ->
                    return@continueWithTask Tasks.forException<DetailedCartItem?>(exception)
                }
            }
        }
    }


}

