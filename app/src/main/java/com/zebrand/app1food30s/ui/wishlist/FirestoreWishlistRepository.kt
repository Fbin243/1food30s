package com.zebrand.app1food30s.ui.wishlist

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.zebrand.app1food30s.data.WishlistItem
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirestoreWishlistRepository(private val userId: String) : WishlistRepository {
    private val db = FirebaseFirestore.getInstance()

    override suspend fun toggleProductInWishlist(productId: String): Boolean = suspendCoroutine { continuation ->
        val db = FirebaseFirestore.getInstance()
        val wishlistRef = db.collection("wishlists").document(userId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(wishlistRef)
            val currentList: MutableList<String> = snapshot.get("productIds") as? MutableList<String> ?: mutableListOf()

            val wasAdded: Boolean
            if (currentList.contains(productId)) {
                // Product is already in wishlist, remove it
                currentList.remove(productId)
                wasAdded = false
            } else {
                // Product is not in wishlist, add it
                currentList.add(productId)
                wasAdded = true
            }

            transaction.update(wishlistRef, "productIds", currentList)
            // Return true if product was added, false if removed
            wasAdded
        }.addOnSuccessListener { wasAdded ->
            continuation.resume(wasAdded)
        }.addOnFailureListener { e ->
            // Log error if needed
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
