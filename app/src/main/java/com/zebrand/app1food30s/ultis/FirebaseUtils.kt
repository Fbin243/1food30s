package com.zebrand.app1food30s.ultis

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object FirebaseUtils {
    val fireStore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val fireStorage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    val fireAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
}

