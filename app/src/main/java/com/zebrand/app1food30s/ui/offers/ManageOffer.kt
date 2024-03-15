package com.zebrand.app1food30s.ui.offers

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.CategoryAdapter
import com.zebrand.app1food30s.adapter.ManageOfferAdapter
import com.zebrand.app1food30s.adapter.ProductApapter
import com.zebrand.app1food30s.data.Category
import com.zebrand.app1food30s.data.Offer
import com.zebrand.app1food30s.databinding.ActivityManageOfferBinding

class ManageOffer : AppCompatActivity() {
    private lateinit var binding: ActivityManageOfferBinding
    private lateinit var rcv: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageOfferBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handleDisplayOfferList()
    }

    private fun handleDisplayOfferList() {
        rcv = binding.productRcv
        rcv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val adapter = ManageOfferAdapter(getListProducts(), false)
        rcv.adapter = adapter
    }

    private fun getListProducts(): List<Offer> {
        return listOf(
            Offer(
                R.drawable.offer1,
                10,
                "Savory and Satisfying"
            ),
            Offer(
                R.drawable.offer2,
                10,
                "Savory and Satisfying"
            ),
            Offer(
                R.drawable.offer1,
                10,
                "Savory and Satisfying"
            ),
            Offer(
                R.drawable.offer2,
                10,
                "Savory and Satisfying"
            ),
            Offer(
                R.drawable.offer1,
                10,
                "Savory and Satisfying"
            ),
            Offer(
                R.drawable.offer2,
                10,
                "Savory and Satisfying"
            ),
            Offer(
                R.drawable.offer1,
                10,
                "Savory and Satisfying"
            ),
            Offer(
                R.drawable.offer2,
                10,
                "Savory and Satisfying"
            ),
            // Add more products as needed
        )
    }
}
