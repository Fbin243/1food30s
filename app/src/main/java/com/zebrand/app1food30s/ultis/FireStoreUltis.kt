package com.zebrand.app1food30s.ultis

import com.google.firebase.firestore.CollectionReference

object FireStoreUltis{
    val mDBUserRef:CollectionReference = FirebaseUtils.fireStore.collection("accounts")
    val mDBOrderRef:CollectionReference = FirebaseUtils.fireStore.collection("orders")
}