package com.zebrand.app1food30s.ui.offers

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.zebrand.app1food30s.adapter.ManageOfferAdapter
import com.zebrand.app1food30s.data.entity.Offer
import com.zebrand.app1food30s.databinding.ActivityManageOfferBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ManageOffer : AppCompatActivity() {
    private lateinit var binding: ActivityManageOfferBinding
    private lateinit var rcv: RecyclerView
    private val fireStore = FirebaseFirestore.getInstance()
    private val fireStorage = FirebaseStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageOfferBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handleDisplayOfferList()
    }

    private fun handleDisplayOfferList() {
        lifecycleScope.launch {
            rcv = binding.productRcv
            rcv.layoutManager = LinearLayoutManager(this@ManageOffer, RecyclerView.VERTICAL, false)
            val adapter = ManageOfferAdapter(getListProducts(), false)
            rcv.adapter = adapter
        }
    }

    private suspend fun getListProducts(): List<Offer> {
        return withContext(Dispatchers.IO) {
            try {
                val querySnapshot = fireStore.collection("offers").get().await()
                querySnapshot.documents.map { document ->
                    val id = document.id
                    val name = document.getString("name") ?: ""
                    val image = document.getString("image") ?: ""
                    val imageUrl = fireStorage.reference.child(image).downloadUrl.await().toString()
                    val price = document.getDouble("price") ?: 0.0
                    val numProduct = document.getDouble("numProduct") ?: 0
                    val date = document.getDate("date")

                    Offer(
                        id,
                        name,
                        imageUrl,
                        price.toInt(),
                        numProduct.toInt(),
                        date
                    )
                }
            } catch (e: Exception) {
                Log.e("getListProducts", "Error getting products", e)
                emptyList()
            }
        }
        return listOf()
    }
}
