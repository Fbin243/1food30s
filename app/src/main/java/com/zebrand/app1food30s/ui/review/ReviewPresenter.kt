package com.zebrand.app1food30s.ui.review

import android.util.Log
import com.google.firebase.firestore.toObject
import com.zebrand.app1food30s.data.entity.Review
import com.zebrand.app1food30s.data.entity.User
import com.zebrand.app1food30s.utils.FireStoreUtils
import com.zebrand.app1food30s.utils.FirebaseService
import com.zebrand.app1food30s.utils.FirebaseUtils
import kotlinx.coroutines.tasks.await

class ReviewPresenter(private val view: ReviewMVPView) {
    suspend fun getReviews(idProduct: String) {
        try {
            view.showShimmerEffectForReviews()
            val reviews = FirebaseService.getListReviewsOfProduct(idProduct)
            view.showReviews(reviews)
            view.hideShimmerEffectForReviews()
        } catch (e: Exception) {
            Log.e("Error", "getReviews: $e")
        }
    }
}