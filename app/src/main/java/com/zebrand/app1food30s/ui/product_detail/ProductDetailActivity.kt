package com.zebrand.app1food30s.ui.product_detail

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.ProductApapter
import com.zebrand.app1food30s.adapter.ReviewAdapter
import com.zebrand.app1food30s.data.Product
import com.zebrand.app1food30s.data.Review
import com.zebrand.app1food30s.databinding.ActivityProductDetailBinding
import com.zebrand.app1food30s.ui.review.ReviewActivity

class ProductDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductDetailBinding
    private lateinit var rcv: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handleDisplayReview()
        handleDisplayRelatedProducts()
        handleOpenReviewScreen()
    }

    private fun handleOpenReviewScreen() {
        binding.viewAllBtn.setOnClickListener {
            val intent = Intent(this, ReviewActivity::class.java)
            startActivity(intent)
        }
    }

    private fun handleDisplayRelatedProducts() {
        rcv = binding.relatedProductRcv
        rcv.layoutManager = GridLayoutManager(this,  2)
        rcv.adapter = ProductApapter(getListProducts())
    }

    private fun handleDisplayReview() {
        rcv = binding.reviewRcv
        rcv.layoutManager = LinearLayoutManager(this,  RecyclerView.VERTICAL, false)
        rcv.adapter = ReviewAdapter(getListReviews())
    }

    private fun getListReviews(): List<Review> {
        var list = listOf<Review>()
        list = list + Review(R.drawable.ava1, "", 5, "test", "")
        list = list + Review(R.drawable.ava1, "", 5, "test", "")
        list = list + Review(R.drawable.ava1, "", 5, "test", "")
        return list
    }

    private fun getListProducts(): List<Product> {
        var list = listOf<Product>()
        list = list + Product(
            R.drawable.product1,
            "Sweet & Sour Chicken",
            "Sweet and sour chicken with crispy chicken, pineapple and delicious chilly sauce.",
            4.5
        )
        list = list + Product(
            R.drawable.product1,
            "Sweet & Sour Chicken",
            "Sweet and sour chicken with crispy chicken, pineapple and delicious chilly sauce.",
            4.5
        )
        list = list + Product(
            R.drawable.product1,
            "Sweet & Sour Chicken",
            "Sweet and sour chicken with crispy chicken, pineapple and delicious chilly sauce.",
            4.5
        )
        list = list + Product(
            R.drawable.product1,
            "Sweet & Sour Chicken",
            "Sweet and sour chicken with crispy chicken, pineapple and delicious chilly sauce.",
            4.5
        )
        return list
    }
}