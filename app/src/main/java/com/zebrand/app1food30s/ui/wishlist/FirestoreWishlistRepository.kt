package com.zebrand.app1food30s.ui.wishlist

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.zebrand.app1food30s.data.WishlistItem
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirestoreWishlistRepository(private val userId: String) : WishlistRepository {
    private val db = FirebaseFirestore.getInstance()

    override suspend fun toggleProductInWishlist(productId: String): Boolean = suspendCoroutine { continuation ->
        val db = FirebaseFirestore.getInstance()
        val wishlistRef = db.collection("wishlists").document(userId)

        Log.d("WishlistToggle", "Attempting to toggle product in wishlist: $productId")

        db.runTransaction { transaction ->
            val snapshot = transaction.get(wishlistRef)
            val currentList: MutableList<String> = snapshot.get("productIds") as? MutableList<String> ?: mutableListOf()

            val wasAdded: Boolean = if (currentList.contains(productId)) {
                currentList.remove(productId)
                Log.d("WishlistToggle", "Product removed from wishlist: $productId")
                false
            } else {
                currentList.add(productId)
                Log.d("WishlistToggle", "Product added to wishlist: $productId")
                true
            }

            // Instead of update, use set with merge option to create the document if it doesn't exist
            val updateData = hashMapOf("productIds" to currentList)
            transaction.set(wishlistRef, updateData, SetOptions.merge())

            wasAdded
        }.addOnSuccessListener { wasAdded ->
            Log.d("WishlistToggle", "Wishlist updated successfully for product: $productId, added: $wasAdded")
            continuation.resume(wasAdded)
        }.addOnFailureListener { e ->
            Log.e("WishlistToggle", "Failed to update wishlist for product: $productId", e)
            continuation.resume(false)
        }
    }

    override suspend fun removeFromWishlist(productId: String): Boolean = suspendCoroutine { continuation ->
        val wishlistRef = db.collection("wishlists").document(userId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(wishlistRef)
            val currentList: MutableList<String> = snapshot.get("productIds") as? MutableList<String> ?: mutableListOf()

            if (currentList.contains(productId)) {
                // Product is in wishlist, remove it
                currentList.remove(productId)
                transaction.update(wishlistRef, "productIds", currentList)
                continuation.resume(true)
            } else {
                // Product was not in the wishlist
                continuation.resume(false)
            }
        }.addOnSuccessListener {
            continuation.resume(true)
        }.addOnFailureListener { e ->
            // Log error if needed
            continuation.resume(false)
        }
    }

    override suspend fun addToWishlist(productId: String): Boolean = suspendCoroutine { continuation ->
        val wishlistRef = db.collection("wishlists").document(userId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(wishlistRef)
            val currentList: MutableList<String> = snapshot.get("productIds") as? MutableList<String> ?: mutableListOf()

            // Check if product ID is not already in the wishlist
            if (!currentList.contains(productId)) {
                // Add the product ID to the list
                currentList.add(productId)
                transaction.update(wishlistRef, "productIds", currentList)
            }
        }.addOnSuccessListener {
            // Since we are inside the success listener, the operation was successful,
            // but we need to determine if the product was added or was already in the list.
            continuation.resume(true) // Assume wasAdded = true for successful transaction.
        }.addOnFailureListener { e ->
            continuation.resumeWithException(e) // Propagate the error.
        }
    }

    override suspend fun isProductInWishlist(productId: String): Boolean = suspendCoroutine { continuation ->
        val wishlistRef = db.collection("wishlists").document(userId)
        wishlistRef.get().addOnSuccessListener { documentSnapshot ->
            val currentList: List<String> = documentSnapshot.get("productIds") as? List<String> ?: listOf()
            continuation.resume(currentList.contains(productId))
        }.addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }
    }

//    override suspend fun getWishlistItems(): List<WishlistItem> = suspendCoroutine { continuation ->
//        val wishlistItems = mutableListOf<WishlistItem>()
//        val wishlistRef = db.collection("wishlists").document(userId)
//        wishlistRef.get().addOnSuccessListener { documentSnapshot ->
//            val productIds: List<String> = documentSnapshot.get("productIds") as? List<String> ?: listOf()
//            val tasks = productIds.map { productId ->
//                db.collection("products").document(productId).get()
//            }
//
//            Tasks.whenAllSuccess<DocumentSnapshot>(tasks).addOnSuccessListener { documents ->
//                documents.forEach { document ->
//                    val name = document.getString("name") ?: ""
//                    val price = document.getDouble("price") ?: 0.0
//                    val image = document.getString("image") ?: ""
//                    wishlistItems.add(WishlistItem(document.id, name, price, image))
//                }
//                continuation.resume(wishlistItems)
//            }.addOnFailureListener { exception ->
//                continuation.resumeWithException(exception)
//            }
//        }.addOnFailureListener { exception ->
//            continuation.resumeWithException(exception)
//        }
//    }

}
