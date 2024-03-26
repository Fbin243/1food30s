package com.zebrand.app1food30s.ultis

import com.google.firebase.firestore.CollectionReference

object FireStoreUtils{
    val mDBUserRef:CollectionReference = FirebaseUtils.fireStore.collection("accounts")
    val mDBOrderRef:CollectionReference = FirebaseUtils.fireStore.collection("orders")
    val mDBCartRef: CollectionReference = FirebaseUtils.fireStore.collection("carts")
}