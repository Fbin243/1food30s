package com.zebrand.app1food30s.ui.cart

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.data.entity.Cart
import com.zebrand.app1food30s.data.entity.CartEntity
import com.zebrand.app1food30s.data.entity.CartItem
import com.zebrand.app1food30s.data.entity.CartItemEntity
import com.zebrand.app1food30s.data.entity.Product
import com.zebrand.app1food30s.utils.FireStoreUtils.mDBCartRef
import com.zebrand.app1food30s.utils.FireStoreUtils.mDBProductRef
import com.zebrand.app1food30s.utils.FireStoreUtils.mDBUserRef
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CartRepository(private val firebaseDb: FirebaseFirestore) {

    fun getCartRef(userId: String, callback: (DocumentReference) -> Unit) {
        val cartRef = mDBCartRef.document(userId)
//        Log.d("Test00", "Attempting to get cart reference for user ID: $userId")

        cartRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
//                Log.d("Test00", "Successfully retrieved cart document for user ID: $cartRef")
                callback(cartRef) // Here we use cartRef directly
            } else {
//                Log.d("Test00", "Cart document does not exist for user ID: $userId. Creating a new one.")
                // Handle creating a new cart document for the user if necessary
            }
        }.addOnFailureListener { exception ->
//            Log.d("Test00", "Failed to get cart document for user ID: $userId", exception)
        }
    }

//    fun listenToCartChanges(userId: String, onResult: (List<CartItem>?, Double) -> Unit, onError: (String) -> Unit) {
////        Log.d("Test00", "listenToCartChanges called for userId: $userId")
//        CoroutineScope(Dispatchers.IO).launch {
//            getCartData(userId) { cart ->
//                if (cart != null) {
//                    // Log success and details about the fetched cart
////                    Log.d("Test00", "Successfully fetched cart for userId: $userId with ${cart.items.size} items.")
//
//                    val totalPrice = cart.items.sumOf { it.productPrice * it.quantity }
//                    onResult(cart.items, totalPrice)
//
//                    // Log the total price for additional debug information
////                    Log.d("Test00", "Total price for userId: $userId is $totalPrice")
//                } else {
//                    // Log failure
////                    Log.e("Test00", "Failed to fetch cart details for userId: $userId")
//                    onError("Failed to fetch cart details.")
//                }
//            }
//        }
//    }

    fun loadCart(cartRef: DocumentReference, onResult: (List<CartItem>?) -> Unit, onError: (String) -> Unit) {
        cartRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                onError(e.message ?: "Unknown error")
                return@addSnapshotListener
            }

            if (snapshot == null || !snapshot.exists()) {
                // called by CheckoutPresenter?
                onError("Cart does not exist.")
                return@addSnapshotListener
            }

            // used to be userId
            fetchProductDetailsForCartItems(cartRef) { detailedCartItems ->
                if (detailedCartItems != null) {
                    onResult(detailedCartItems)
                } else {
                    onError("Failed to fetch detailed cart items.")
                }
            }
        }
    }

//    fun listenToCartChanges(userId: String, onResult: (List<CartItem>?, Double) -> Unit, onError: (String) -> Unit) {
//        getCartRef(userId) { cartRef ->
//            cartRef.addSnapshotListener { _, e ->
//                if (e != null) {
//                    onError(e.message ?: "Unknown error")
//                    return@addSnapshotListener
//                }
//
//                fetchProductDetailsForCartItems(cartRef) { detailedCartItems, totalPrice ->
//                    if (detailedCartItems != null) {
//                        onResult(detailedCartItems, totalPrice)
//                    } else {
//                        onError("Failed to fetch detailed cart items.")
//                    }
//                }
//            }
//        }
//    }

    fun removeFromCart(cartRef: DocumentReference, productRef: DocumentReference, onComplete: (Boolean) -> Unit) {
        cartRef.get().addOnSuccessListener { documentSnapshot ->
            val cart = documentSnapshot.toObject(Cart::class.java)
            cart?.let {
                it.items.removeAll { item -> item.productId == productRef }
                cartRef.set(it).addOnSuccessListener {
                    onComplete(true)
                }.addOnFailureListener {
                    onComplete(false)
                }
            }
        }
    }

    fun updateCartItemQuantity(cartRef: DocumentReference, productRef: DocumentReference, newQuantity: Int, onComplete: (Boolean) -> Unit) {
        cartRef.get().addOnSuccessListener { documentSnapshot ->
            val cart = documentSnapshot.toObject(Cart::class.java)
            cart?.let {
                it.items.find { item -> item.productId == productRef }?.quantity = newQuantity
                cartRef.set(it).addOnSuccessListener {
                    onComplete(true)
                }.addOnFailureListener {
                    onComplete(false)
                }
            }
        }
    }

    // fetching from user
//    fun fetchProductDetailsForCartItems(cartRef: DocumentReference, callback: (List<CartItem>?, Double) -> Unit) {
////        mDBUserRef.document(userId).get().addOnSuccessListener { userSnapshot ->
////            val cartRef = userSnapshot.get("cartRef") as? DocumentReference
////            if (cartRef == null) {
////                Log.e("Test00", "No cartRef found in user's document for User ID: $userId")
////                callback(null, 0.0)
////                return@addOnSuccessListener
////            }
////
////            Log.d("Test00", "Found cartRef, proceeding to fetch cart for User ID: $userId")
//        cartRef.get().addOnSuccessListener { documentSnapshot ->
//            val cart = documentSnapshot.toObject(Cart::class.java)
//            if (cart == null) {
//                callback(null, 0.0)
//                return@addOnSuccessListener
//            }
//
//            val tasks = cart.items.map { cartItem ->
//                fetchProductDetailsForCartItem(cartItem)
//            }
//
//            Tasks.whenAllSuccess<CartItem>(tasks).addOnSuccessListener { detailedCartItems ->
//                val totalPrice = detailedCartItems.sumOf { item -> item.productPrice * item.quantity }
//                callback(detailedCartItems, totalPrice)
//            }.addOnFailureListener { e ->
//                callback(null, 0.0)
//            }
//        }.addOnFailureListener { e ->
//            callback(null, 0.0)
//        }
//    }

    fun fetchProductDetailsForCartItems(cartRef: DocumentReference, callback: (List<CartItem>?) -> Unit) {
        cartRef.get().addOnSuccessListener { documentSnapshot ->
            val cart = documentSnapshot.toObject(Cart::class.java)
            val cartItems = cart?.items
//            Log.d("Test00", "fetchProductDetailsForCartItems: $cartItems")
            if (cart == null) {
                callback(null)
                return@addOnSuccessListener
            }

            // Ensure cart.items is not null or empty before proceeding
            if (cartItems != null) {
                if (cartItems.isEmpty()) {
                    callback(emptyList()) // No items to process, return empty list and zero total price
                    return@addOnSuccessListener
                }
            }

            val tasks = cartItems?.map { cartItem ->
                // Skip null items, if any, to prevent NullPointerException
//                Log.d("Test00", "fetchProductDetailsForCartItems: $cartItem")
                fetchProductDetailsForCartItem(cartItem)
            }

            Tasks.whenAllSuccess<CartItem?>(tasks).addOnSuccessListener { result ->
                val detailedCartItems = result.filterNotNull() // Remove any nulls that may have slipped through
//                Log.d("Test00", "fetchProductDetailsForCartItems: $detailedCartItems")
//                val totalPrice = detailedCartItems.sumOf { item -> item.productPrice * item.quantity }
                callback(detailedCartItems)
            }.addOnFailureListener { e ->
                callback(null)
            }
        }.addOnFailureListener { e ->
            callback(null)
        }
    }

//    fun fetchProductDetailsForCartItems(userId: String, callback: (List<CartItem>?, Double) -> Unit) {
//
//        mDBCartRef.document(userId).get().addOnSuccessListener { documentSnapshot ->
//            val cart = documentSnapshot.toObject(Cart::class.java)
//            if (cart == null) {
//                Log.e("Test00", "Cart object is null for Cart ID: $userId")
//                callback(null, 0.0)
//                return@addOnSuccessListener
//            }
//
//            val tasks = cart.items.map { cartItem ->
//                fetchProductDetailsForCartItem(cartItem)
//            }
//
//            // Correctly wait for all tasks to complete and process their results
//            Tasks.whenAllSuccess<CartItem?>(tasks).addOnSuccessListener { detailedCartItems ->
//                // Filter out null values from the detailedCartItems
//                val nonNullDetailedCartItems = detailedCartItems.filterNotNull()
//
//                val totalPrice = nonNullDetailedCartItems.sumOf { it.productPrice * it.quantity }
//                Log.d("Test00", "fetchProductDetailsForCartItems: $nonNullDetailedCartItems")
//                callback(nonNullDetailedCartItems, totalPrice)
//            }.addOnFailureListener { e ->
//                Log.e("Test00", "Error fetching cart items details for Cart ID: $userId", e)
//                callback(null, 0.0)
//            }
//        }.addOnFailureListener { e ->
//            Log.e("Test00", "Error fetching cart for Cart ID: $userId", e)
//            callback(null, 0.0)
//        }
//    }

    private fun fetchProductDetailsForCartItem(cartItem: CartItem): Task<CartItem?> {
//        Log.d("Test00", "fetchProductDetailsForCartItem: ")

        if (cartItem.productId == null) {
//            Log.d("Test00", "fetchProductDetailsForCartItem: fail")
            return Tasks.forResult(null) // Early return if productId is null
        }

        return cartItem.productId.get().continueWithTask { task ->
            if (task.isSuccessful) {
//                Log.d("Test00", "fetchProductDetailsForCartItem: successful")
                val product = task.result.toObject(Product::class.java)
                if (product != null) {
//                    Log.d("Test00", "fetchProductDetailsForCartItem: product not null")
                    val storageReference = FirebaseStorage.getInstance().reference.child(product.image)
                    return@continueWithTask storageReference.downloadUrl.continueWithTask { urlTask ->
                        if (urlTask.isSuccessful) {
                            val imageUrl = urlTask.result.toString()
                            val cartItem = CartItem(
                                productId = cartItem.productId, // Use DocumentReference's ID as a string
                                // TODO
                                productCategory = "Food", // Example category, adjust as necessary
                                productName = product.name,
                                productPrice = product.price,
                                productImage = imageUrl, // Now using the URL string
                                productStock = product.stock,
                                quantity = cartItem.quantity
                            )
//                            Log.d("Test00", "fetchProductDetailsForCartItem: $cartItem")
                            Tasks.forResult(cartItem)
                        } else {
                            Tasks.forException<CartItem?>(urlTask.exception ?: Exception("Failed to fetch image URL"))
                        }
                    }
                } else {
//                    Log.d("Test00", "fetchProductDetailsForCartItem: product null")
                    return@continueWithTask Tasks.forResult(null)
                }
            } else {
//                Log.d("Test00", "fetchProductDetailsForCartItem: task fails")
                task.exception?.let { exception ->
                    return@continueWithTask Tasks.forException<CartItem?>(exception)
                }
            }
        }
    }

    // called by CheckoutPresenter
    fun placeOrderAndClearCart(cartId: String, cartItems: List<CartItem>, completion: (Boolean) -> Unit) {
        // Fetch each product document to prepare updates
        val productFetchTasks = cartItems.mapNotNull { cartItem ->
            cartItem.productId?.get()
        }

        Tasks.whenAllSuccess<DocumentSnapshot>(productFetchTasks).addOnSuccessListener { documentSnapshots ->
            val batch = firebaseDb.batch()
            val cartRef = mDBCartRef.document(cartId)

            // Update the cart to have no items, effectively clearing it
            batch.update(cartRef, "items", emptyList<CartItem>())

            // Prepare to update each product based on the cart item details
            documentSnapshots.forEach { documentSnapshot ->
                val product = documentSnapshot.toObject(Product::class.java)
                val cartItem = cartItems.firstOrNull { it.productId?.id == documentSnapshot.id } // Match cart item to product
                if (product != null && cartItem != null) {
                    val newStock = product.stock - cartItem.quantity
                    val newSold = product.sold + cartItem.quantity
                    // Prepare product updates
                    batch.update(documentSnapshot.reference, mapOf("stock" to newStock, "sold" to newSold))
                }
            }

            // Commit all prepared operations in the batch
            batch.commit().addOnSuccessListener {
                // Log success or notify UI
                completion(true)
            }.addOnFailureListener { e ->
                // Log error or notify UI
                completion(false)
            }
        }.addOnFailureListener { e ->
            // Log error or notify UI
            completion(false)
        }
    }


    //    private suspend fun getCartData(userId: String, onDataReady: (Cart?) -> Unit) {
//        // First, try to get the cart data from the local cache
//        val cachedCart = roomDb.cartDao().getCartByUserId(userId)
//
//        if (cachedCart != null) {
//            // If the cart is available in the local cache, return it
//            val cart = convertCartEntityToCart(userId, roomDb)
////            Log.d("Test00", "getCartData: cached. $cart")
//            withContext(Dispatchers.Main) {
//                onDataReady(cart) // Ensure UI updates happen on the main thread
//            }
//        } else {
//            // If the cart is not available locally, fetch it from Firestore
////            Log.d("Test00", "getCartData: firebase")
//            fetchFirestoreToLocal(userId, onDataReady)
//        }
//    }

//    private suspend fun convertCartEntityToCart(userId: String, db: AppDatabase): Cart {
//        // Retrieve all CartItemEntities associated with the userId
//        val cartItemEntities = db.cartItemDao().getCartItemsForUser(userId)
//
//        // Convert each CartItemEntity to a CartItem
//        val items = cartItemEntities.map { cartItemEntity ->
//            CartItem(
//                productId = mDBProductRef.document(cartItemEntity.productId),
//                productName = cartItemEntity.productName,
//                quantity = cartItemEntity.quantity,
//                productCategory = cartItemEntity.productCategory,
//                productImage = cartItemEntity.productImage,
//                productPrice = cartItemEntity.productPrice,
//                productStock = cartItemEntity.productStock
//            )
//        }
//
//        return Cart("", mDBUserRef.document(userId), items.toMutableList())
//    }

//    private fun fetchFirestoreToLocal(userId: String, onDataReady: (Cart?) -> Unit) {
//        mDBCartRef.document(userId).get()
//            .addOnSuccessListener { document ->
//                val cart = document.toObject(Cart::class.java)
//                if (cart != null && cart.items.isNotEmpty()) {
////                    Log.d("Test00", "fetchFirestoreToLocal: $cart")
//
//                    // Fetch details for each cart item
//                    val fetchTasks = cart.items.map { cartItem ->
//                        fetchProductDetailsForCartItem(cartItem).continueWith { task ->
//                            if (task.isSuccessful) {
//                                task.result
//                            } else {
//                                null // Handle failure to fetch product details
//                            }
//                        }
//                    }
//
//                    Tasks.whenAllSuccess<CartItem?>(fetchTasks).addOnSuccessListener { detailedCartItems ->
//                        // Filter out nulls if any task failed
//                        val filteredItems = detailedCartItems.filterNotNull()
//
//                        // Update the cart's items with detailed items
//                        cart.items = filteredItems.toMutableList()
//
//                        // Update local cache with detailed cart
//                        updateLocalCartData(userId, cart)
//
//                        onDataReady(cart)
//                    }.addOnFailureListener { e ->
////                        Log.e("Test00", "Failed to fetch cart item details", e)
//                        onDataReady(null)
//                    }
//                } else {
////                    Log.d("Test00", "Cart is empty or null")
//                    onDataReady(null)
//                }
//            }
//            .addOnFailureListener { e ->
////                Log.e("Test00", "Failed to fetch cart from Firestore", e)
//                onDataReady(null)
//            }
//    }

    // convert Cart into cache
//    private fun updateLocalCartData(userId: String, cart: Cart) {
//        val cartEntity = CartEntity(userId = userId)
//        val cartItemEntities = cart.items.map { item ->
//            item.productId?.let {
//                CartItemEntity(
//                    cartUserId = userId,
//                    productId = it.id,
//                    productCategory = item.productCategory,
//                    productName = item.productName,
//                    productPrice = item.productPrice,
//                    productImage = item.productImage,
//                    productStock = item.productStock,
//                    quantity = item.quantity
//                )
//            }
//        }
//
//        CoroutineScope(Dispatchers.IO).launch {
//            // Insert or update the cart
//            roomDb.cartDao().insertCart(cartEntity)
//            // Insert or update cart items
//            cartItemEntities.forEach { item ->
//                if (item != null) {
//                    roomDb.cartItemDao().insertCartItem(item)
//                }
//            }
//        }
//    }
}

