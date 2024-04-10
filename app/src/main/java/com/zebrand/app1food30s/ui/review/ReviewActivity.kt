package com.zebrand.app1food30s.ui.review

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.ReviewAdapter
import com.zebrand.app1food30s.data.entity.Review
import com.zebrand.app1food30s.databinding.ActivityReviewBinding
import com.zebrand.app1food30s.utils.Utils
import kotlinx.coroutines.launch

class ReviewActivity : AppCompatActivity(), ReviewMVPView {
    private lateinit var binding: ActivityReviewBinding
    private lateinit var reviewPresenter: ReviewPresenter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)
        binding = ActivityReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        reviewPresenter = ReviewPresenter(this)
        lifecycleScope.launch {
            reviewPresenter.getReviews(intent.getStringExtra("idProduct")!!)
        }
        handleCloseReviewScreen()
    }

    override fun showReviews(reviews: List<Review>) {
        binding.reviewRcv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.reviewRcv.adapter = ReviewAdapter(reviews)
    }

    override fun showShimmerEffectForReviews() {
        Utils.showShimmerEffect(binding.reviewShimmer, binding.reviewRcv)
    }

    override fun hideShimmerEffectForReviews() {
        Utils.hideShimmerEffect(binding.reviewShimmer, binding.reviewRcv)
    }

    private fun handleCloseReviewScreen() {
        binding.backFromReview.root.setOnClickListener {
            finish()
        }
    }
}