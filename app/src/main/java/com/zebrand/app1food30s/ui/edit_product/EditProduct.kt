package com.zebrand.app1food30s.ui.edit_product

import android.R
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.zebrand.app1food30s.data.Product
import com.zebrand.app1food30s.databinding.ActivityEditProductBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class EditProduct : AppCompatActivity() {
    private lateinit var binding: ActivityEditProductBinding
    private val fireStore = FirebaseFirestore.getInstance()
    private val fireStorage = FirebaseStorage.getInstance()
    private lateinit var categorySpinner: Spinner
    private lateinit var offerSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val productId = intent.getStringExtra("PRODUCT_ID")
        if (productId != null) {
            fetchProductDetails(productId)
        }

        categorySpinner = findViewById(com.zebrand.app1food30s.R.id.category_spinner)
        offerSpinner = findViewById(com.zebrand.app1food30s.R.id.offer_spinner)
//        loadOffersFromFirebase()
        setupListeners()
    }

    private fun setupListeners() {
        // Đặt sự kiện cho nút lưu, hủy, hoặc bất kỳ hành động nào bạn muốn xử lý
        binding.saveBtn.setOnClickListener {
            val productId = intent.getStringExtra("PRODUCT_ID")
            productId?.let {
                updateProductDetails(it)
            }
        }
    }

    private fun loadCategoriesFromFirebase(categoryStr: String) {
        val db = Firebase.firestore
        val categoriesList = ArrayList<String>()

        db.collection("categories").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    categoriesList.add(document.getString("name") ?: "")
                }
                val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, categoriesList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                categorySpinner.adapter = adapter

                val selectedPosition = categoriesList.indexOf(categoryStr)
                if (selectedPosition != -1) {
                    categorySpinner.setSelection(selectedPosition)
                }
            }
            .addOnFailureListener { exception ->
                // Xử lý lỗi ở đây
            }
    }

    private fun loadOffersFromFirebase(offerStr: String) {
        val db = Firebase.firestore
        val offerList = ArrayList<String>()

        db.collection("offers").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    offerList.add(document.getString("name") ?: "")
                }
                val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, offerList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                offerSpinner.adapter = adapter

                val selectedPosition = offerList.indexOf(offerStr)
                if (selectedPosition != -1) {
                    offerSpinner.setSelection(selectedPosition)
                }
            }
            .addOnFailureListener { exception ->
                // Xử lý lỗi ở đây
            }
    }

    private fun fetchProductDetails(productId: String) {
        lifecycleScope.launch {
            try {
                val documentSnapshot = fireStore.collection("products").document(productId).get().await()
                val product = documentSnapshot.toObject(Product::class.java)
                product?.let {
                    with(binding) {
                        inputName.setText(it.name)
                        inputPrice.setText(it.price.toString())
                        inputStock.setText(it.stock.toString())
                        inputDescription.setText(it.description)
                        val imageUrl = fireStorage.reference.child(it.image).downloadUrl.await().toString()
                        Picasso.get().load(imageUrl).into(imageProduct)

                        val offerId = it.idOffer?.id ?: "non"
                        val offersCollection = FirebaseFirestore.getInstance().collection("offers")
                        offersCollection.document(offerId).get().addOnSuccessListener { document ->
                            if (document != null && document.exists()) {
//                val category = document.toObject(Category::class.java)
//                                categorySpinner.text = document.getString("name") ?: ""
                                val offerStr = document.getString("name") ?: ""
                                loadOffersFromFirebase(offerStr)
                            } else {
                                loadOffersFromFirebase("")
                            }
                        }.addOnFailureListener {

                        }

                        val categoryId = it.idCategory?.id ?: "non"
                        val categoriesCollection = FirebaseFirestore.getInstance().collection("categories")
                        categoriesCollection.document(categoryId).get().addOnSuccessListener { document ->
                            if (document != null && document.exists()) {
//                val category = document.toObject(Category::class.java)
//                                categorySpinner.text = document.getString("name") ?: ""
                                val categoryStr = document.getString("name") ?: ""
                                loadCategoriesFromFirebase(categoryStr)
                            } else {
                                loadCategoriesFromFirebase("")
                            }
                        }.addOnFailureListener {

                        }
                        // Cập nhật các trường khác tương tự như trên
                    }
                }
            } catch (e: Exception) {
                // Xử lý lỗi
            }
        }
    }

    private fun updateProductDetails(productId: String) {
        val updatedName = binding.inputName.text.toString()
        val updatedPrice = binding.inputPrice.text.toString().toDoubleOrNull()
        val updatedStock = binding.inputStock.text.toString().toIntOrNull()
        val updatedDescription = binding.inputDescription.text.toString()
        // Lấy các giá trị cập nhật từ người dùng

        lifecycleScope.launch {
            try {
                fireStore.collection("products").document(productId).update(
                    mapOf(
                        "name" to updatedName,
                        "price" to updatedPrice,
                        "stock" to updatedStock,
                        "description" to updatedDescription
                        // Cập nhật các trường khác tương tự như trên
                    )
                ).await()
                // Xử lý sau khi cập nhật thành công, ví dụ: hiển thị thông báo, đóng màn hình
            } catch (e: Exception) {
                // Xử lý lỗi
            }
        }
    }
}
