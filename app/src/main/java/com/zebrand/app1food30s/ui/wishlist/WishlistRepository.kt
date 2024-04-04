package com.zebrand.app1food30s.ui.wishlist

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.zebrand.app1food30s.data.entity.Wishlist
import com.zebrand.app1food30s.data.entity.WishlistItem
import com.zebrand.app1food30s.utils.FireStoreUtils
import com.zebrand.app1food30s.utils.FireStoreUtils.mDBProductRef
import com.zebrand.app1food30s.utils.FireStoreUtils.mDBWishlistRef
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class  WishlistRepository(private val userId: String) {

    fun initialize() {
        // Check if a wishlist document exists for the current user
        val wishlistRef = FireStoreUtils.mDBWishlistRef.document(userId)
        wishlistRef.get().addOnSuccessListener { documentSnapshot ->
            if (!documentSnapshot.exists()) {
                // Create a new wishlist if it does not exist
                val newWishlist = Wishlist(id = userId)
                wishlistRef.set(newWishlist)
                    .addOnSuccessListener {
                        // Optionally log success or notify user/UI of the new wishlist creation
                    }
                    .addOnFailureListener { e ->
                        // Handle or log the error of creating a new wishlist
                    }
            }
        }.addOnFailureListener { e ->
            // Handle or log the error of fetching the wishlist
        }
    }

    suspend fun fetchWishlistForCurrentUser(): List<WishlistItem> = suspendCoroutine { continuation ->
        val wishlistRef = mDBWishlistRef.document(userId)
        wishlistRef.get().addOnSuccessListener { documentSnapshot ->
            val productIds = documentSnapshot.get("productIds")
                .let { it as? List<*> }
                ?.filterIsInstance<String>()
                ?: listOf()

            // Prepare Firestore read tasks for each product ID
            val tasks = productIds.map { productId ->
                mDBProductRef.document(productId).get()
            }

            // Execute all tasks and await their completion
            Tasks.whenAllSuccess<DocumentSnapshot>(tasks).addOnSuccessListener { documents ->
                val wishlistItems = documents.mapNotNull { document ->
                    WishlistItem(
                        productId = document.id,
                        name = document.getString("name") ?: "",
                        price = document.getDouble("price") ?: 0.0,
                        image = document.getString("image") ?: ""
                    )
                }
                continuation.resume(wishlistItems)
            }.addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
        }.addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }
    }

//    suspend fun toggleProductInWishlist(productId: String): Boolean = suspendCoroutine { continuation ->
//        val db = FirebaseFirestore.getInstance()
//        val wishlistRef = db.collection("wishlists").document(userId)
//
//        Log.d("WishlistToggle", "Attempting to toggle product in wishlist: $productId")
//
//        db.runTransaction { transaction ->
//            val snapshot = transaction.get(wishlistRef)
//            val currentList: MutableList<String> = snapshot.get("productIds") as? MutableList<String> ?: mutableListOf()
//
//            val wasAdded: Boolean = if (currentList.contains(productId)) {
//                currentList.remove(productId)
//                Log.d("WishlistToggle", "Product removed from wishlist: $productId")
//                false
//            } else {
//                currentList.add(productId)
//                Log.d("WishlistToggle", "Product added to wishlist: $productId")
//                true
//            }
//
//            // Instead of update, use set with merge option to create the document if it doesn't exist
//            val updateData = hashMapOf("productIds" to currentList)
//            transaction.set(wishlistRef, updateData, SetOptions.merge())
//
//            wasAdded
//        }.addOnSuccessListener { wasAdded ->
//            Log.d("WishlistToggle", "Wishlist updated successfully for product: $productId, added: $wasAdded")
//            continuation.resume(wasAdded)
//        }.addOnFailureListener { e ->
//            Log.e("WishlistToggle", "Failed to update wishlist for product: $productId", e)
//            continuation.resume(false)
//        }
//    }
//
//    suspend fun removeFromWishlist(productId: String): Boolean = suspendCoroutine { continuation ->
//        val wishlistRef = mDBWishlistRef.document(userId)
//
//        db.runTransaction { transaction ->
//            val snapshot = transaction.get(wishlistRef)
//            val currentList: MutableList<String> = snapshot.get("productIds") as? MutableList<String> ?: mutableListOf()
//
//            if (currentList.contains(productId)) {
//                // Product is in wishlist, remove it
//                currentList.remove(productId)
//                transaction.update(wishlistRef, "productIds", currentList)
//                continuation.resume(true)
//            } else {
//                // Product was not in the wishlist
//                continuation.resume(false)
//            }
//        }.addOnSuccessListener {
//            continuation.resume(true)
//        }.addOnFailureListener { e ->
//            // Log error if needed
//            continuation.resume(false)
//        }
//    }
//
//    suspend fun addToWishlist(productId: String): Boolean = suspendCoroutine { continuation ->
//        val wishlistRef = db.collection("wishlists").document(userId)
//
//        db.runTransaction { transaction ->
//            val snapshot = transaction.get(wishlistRef)
//            val currentList: MutableList<String> = snapshot.get("productIds") as? MutableList<String> ?: mutableListOf()
//
//            // Check if product ID is not already in the wishlist
//            if (!currentList.contains(productId)) {
//                // Add the product ID to the list
//                currentList.add(productId)
//                transaction.update(wishlistRef, "productIds", currentList)
//            }
//        }.addOnSuccessListener {
//            // Since we are inside the success listener, the operation was successful,
//            // but we need to determine if the product was added or was already in the list.
//            continuation.resume(true) // Assume wasAdded = true for successful transaction.
//        }.addOnFailureListener { e ->
//            continuation.resumeWithException(e) // Propagate the error.
//        }
//    }
//
//    suspend fun isProductInWishlist(productId: String): Boolean = suspendCoroutine { continuation ->
//        val wishlistRef = db.collection("wishlists").document(userId)
//        wishlistRef.get().addOnSuccessListener { documentSnapshot ->
//            val currentList: List<String> = documentSnapshot.get("productIds") as? List<String> ?: listOf()
//            continuation.resume(currentList.contains(productId))
//        }.addOnFailureListener { exception ->
//            continuation.resumeWithException(exception)
//        }
//    }
}