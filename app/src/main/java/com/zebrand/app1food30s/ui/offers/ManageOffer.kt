package com.zebrand.app1food30s.ui.offers

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.ManageOfferAdapter
import com.zebrand.app1food30s.adapter.ManageProductAdapter
import com.zebrand.app1food30s.data.entity.Offer
import com.zebrand.app1food30s.databinding.ActivityManageOfferBinding
import com.zebrand.app1food30s.ui.edit_offer.EditOffer
import com.zebrand.app1food30s.ui.edit_product.EditProduct
import com.zebrand.app1food30s.ui.manage_product.ManageProductDetailActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ManageOffer : AppCompatActivity() {
    private lateinit var binding: ActivityManageOfferBinding
    private lateinit var rcv: RecyclerView
    private val fireStore = FirebaseFirestore.getInstance()
    private val fireStorage = FirebaseStorage.getInstance()

    private lateinit var addButton: ImageView
    private lateinit var filterButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageOfferBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handleDisplayOfferList()

        addButton = findViewById(R.id.add_product_btn)
        filterButton = findViewById(R.id.filter_btn)

        addButton.setOnClickListener {
            val intent = Intent(this, ManageOfferDetail::class.java)
            startActivity(intent)
        }
    }

//    private fun handleDisplayOfferList() {
//        lifecycleScope.launch {
//            rcv = binding.productRcv
//            rcv.layoutManager = LinearLayoutManager(this@ManageOffer, RecyclerView.VERTICAL, false)
//            val adapter = ManageOfferAdapter(getListOffers(), false)
//            rcv.adapter = adapter
//        }
//    }

    private fun handleDisplayOfferList() {
        lifecycleScope.launch {
//            showShimmerEffectForProducts()
//            val db = AppDatabase.getInstance(applicationContext)
            val adapter = ManageOfferAdapter(getListOffers(), onOfferClick = { offer ->
                val intent = Intent(this@ManageOffer, EditOffer::class.java).apply {
                    putExtra("OFFER_ID", offer.id)
                }
                startActivity(intent)
            })
            binding.productRcv.layoutManager = LinearLayoutManager(this@ManageOffer)
            binding.productRcv.adapter = adapter
//            hideShimmerEffectForProducts()
        }
    }

    private suspend fun getListOffers(): List<Offer> {
        return withContext(Dispatchers.IO) {
            try {
                val querySnapshot = fireStore.collection("offers").get().await()
                querySnapshot.documents.map { document ->
                    val id = document.id
                    val name = document.getString("name") ?: ""
                    val image = document.getString("image") ?: ""
                    val imageUrl = fireStorage.reference.child(image).downloadUrl.await().toString()
                    val discountRate = document.getDouble("discountRate") ?: 0.0
                    val numProduct = document.getDouble("numProduct") ?: 0
                    val date = document.getDate("date")

                    Offer(
                        id,
                        name,
                        imageUrl,
                        discountRate.toInt(),
                        numProduct.toInt(),
                        date
                    )
                }
            } catch (e: Exception) {
                Log.e("getListOffers", "Error getting products", e)
                emptyList()
            }
        }
        return listOf()
    }
}
