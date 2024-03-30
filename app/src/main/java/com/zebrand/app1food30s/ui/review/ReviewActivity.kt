package com.zebrand.app1food30s.ui.review

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.data.entity.Review
import com.zebrand.app1food30s.databinding.ActivityReviewBinding

class ReviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReviewBinding
    private lateinit var rcv: RecyclerView
    private val fireStore = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)
        binding = ActivityReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handleDisplayReviewList()
        handleCloseReviewScreen()
    }

    private fun handleDisplayReviewList() {
        rcv = binding.reviewRcv
        rcv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        // TODO
//        val adapter = ReviewAdapter(getListReviews())
//        rcv.adapter = adapter
    }

    private fun handleCloseReviewScreen() {
        binding.backFromReview.root.setOnClickListener {
            finish()
        }
    }

    private fun getListReviews(): List<Review> {
        var list = listOf<Review>()

//        list = list + Review(R.drawable.ava1, "", 5, "test", "")
//        list = list + Review(R.drawable.ava1, "", 5, "test", "")
//        list = list + Review(R.drawable.ava1, "", 5, "test", "")
//        list = list + Review(R.drawable.ava1, "", 5, "test", "")
//        list = list + Review(R.drawable.ava1, "", 5, "test", "")
//        list = list + Review(R.drawable.ava1, "", 5, "test", "")
//        list = list + Review(R.drawable.ava1, "", 5, "test", "")
//        list = list + Review(R.drawable.ava1, "", 5, "test", "")
//        list = list + Review(R.drawable.ava1, "", 5, "test", "")
        return list
    }
}