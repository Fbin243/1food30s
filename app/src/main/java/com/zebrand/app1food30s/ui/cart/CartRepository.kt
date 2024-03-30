package com.zebrand.app1food30s.ui.cart

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.zebrand.app1food30s.data.entity.Cart
import com.zebrand.app1food30s.data.entity.CartItem
import com.zebrand.app1food30s.data.entity.DetailedCartItem
import com.zebrand.app1food30s.data.entity.Product

class CartRepository(private val db: FirebaseFirestore) {

    fun fetchProductDetailsForCartItems(cartId: String, callback: (List<DetailedCartItem>?, Double) -> Unit) {
        db.collection("carts").document(cartId).get().addOnSuccessListener { documentSnapshot ->
            val cart = documentSnapshot.toObject(Cart::class.java)
            cart?.let { cart ->
                val tasks = cart.items.map { cartItem ->
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

    fun placeOrderAndClearCart(cartId: String, detailedCartItems: List<DetailedCartItem>, completion: (Boolean) -> Unit) {
        // Fetch each product document to prepare updates
        val productFetchTasks = detailedCartItems.mapNotNull { cartItem ->
            cartItem.productId?.get()
        }

        Tasks.whenAllSuccess<DocumentSnapshot>(productFetchTasks).addOnSuccessListener { documentSnapshots ->
            val batch = db.batch()
            val cartRef = db.collection("carts").document(cartId)
            batch.delete(cartRef) // Prepare to clear the cart

            // Prepare to update each product based on the cart item details
            documentSnapshots.forEach { documentSnapshot ->
                val product = documentSnapshot.toObject(Product::class.java)
                val cartItem = detailedCartItems.firstOrNull { it.productId?.id == documentSnapshot.id } // Match cart item to product
                if (product != null && cartItem != null) {
                    val newStock = product.stock - cartItem.quantity
                    val newSold = product.sold + cartItem.quantity
                    batch.update(documentSnapshot.reference, "stock", newStock, "sold", newSold) // Prepare product update
                }
            }

            // Commit all prepared operations in the batch
            batch.commit().addOnSuccessListener {
//                Log.d("Checkout", "Order placed and cart cleared successfully.")
                completion(true)
            }.addOnFailureListener { e ->
//                Log.e("Checkout", "Failed to place order and clear cart.", e)
                completion(false)
            }
        }.addOnFailureListener { e ->
//            Log.e("Checkout", "Failed to fetch products for order.", e)
            completion(false)
        }
    }

}

