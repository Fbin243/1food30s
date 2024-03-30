package com.zebrand.app1food30s.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.data.entity.Category
import com.zebrand.app1food30s.data.entity.Offer
import com.zebrand.app1food30s.data.entity.Product
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object FirebaseUtils {
    val fireStore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val fireStorage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    val fireAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
}