package com.zebrand.app1food30s.utils

import com.google.firebase.firestore.CollectionReference
import com.zebrand.app1food30s.utils.FirebaseUtils

object FireStoreUtils{
    val mDBUserRef:CollectionReference = FirebaseUtils.fireStore.collection("accounts")
    val mDBOrderRef:CollectionReference = FirebaseUtils.fireStore.collection("orders")
    val mDBCartRef: CollectionReference = FirebaseUtils.fireStore.collection("carts")
    val mDBProductRef: CollectionReference = FirebaseUtils.fireStore.collection("products")
    val mDBWishlistRef: CollectionReference = FirebaseUtils.fireStore.collection("wishlists")
}