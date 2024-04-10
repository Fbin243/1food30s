package com.zebrand.app1food30s.ui.review

import com.zebrand.app1food30s.data.entity.Review

interface ReviewMVPView {
    fun showReviews(reviews: List<Review>)
    fun showShimmerEffectForReviews()
    fun hideShimmerEffectForReviews()

}