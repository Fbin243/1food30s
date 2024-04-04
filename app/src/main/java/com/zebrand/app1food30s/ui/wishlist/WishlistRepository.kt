package com.zebrand.app1food30s.ui.wishlist

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.zebrand.app1food30s.data.entity.Product
import com.zebrand.app1food30s.data.entity.Wishlist
import com.zebrand.app1food30s.data.entity.WishlistItem
import com.zebrand.app1food30s.utils.FireStoreUtils
import com.zebrand.app1food30s.utils.FireStoreUtils.mDBProductRef
import com.zebrand.app1food30s.utils.FireStoreUtils.mDBWishlistRef
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class  WishlistRepository(private val userId: String) {
    private val wishlistRef = mDBWishlistRef.document(userId)

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

    private suspend fun addToWishlist(productId: String): Boolean = suspendCancellableCoroutine { cont ->
        wishlistRef.update("productIds", FieldValue.arrayUnion(productId))
            .addOnSuccessListener { cont.resume(true) }
            .addOnFailureListener { e -> cont.resumeWithException(e) }
    }

    private suspend fun removeFromWishlist(productId: String): Boolean = suspendCancellableCoroutine { cont ->
        wishlistRef.update("productIds", FieldValue.arrayRemove(productId))
            .addOnSuccessListener { cont.resume(true) }
            .addOnFailureListener { e -> cont.resumeWithException(e) }
    }

    suspend fun toggleWishlist(productId: String): Boolean {
        val document = wishlistRef.get().await()  // Assuming this is a call to Firebase Firestore.
        val productIds = (document["productIds"] as? List<*>)?.filterIsInstance<String>() ?: listOf()

        val isProductWishlisted = productId in productIds
//        Log.d("Test00", "toggleWishlist: $isProductWishlisted")

        // Perform the toggle operation.
        return if (isProductWishlisted) {
            // If the product is currently wishlisted, attempt to remove it.
            val success = removeFromWishlist(productId)
            !success
        } else {
            // If the product is not currently wishlisted, attempt to add it.
            val success = addToWishlist(productId)
            success
        }
    }

}