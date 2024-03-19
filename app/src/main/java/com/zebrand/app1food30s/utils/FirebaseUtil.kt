package com.zebrand.app1food30s.utils

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object FirebaseUtil {
    val fireStore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val fireStorage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
}