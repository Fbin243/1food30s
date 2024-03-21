package com.zebrand.app1food30s.ui.edit_product

import android.R
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import com.zebrand.app1food30s.data.Product
import com.zebrand.app1food30s.databinding.ActivityEditProductBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

class EditProduct : AppCompatActivity() {
    private lateinit var binding: ActivityEditProductBinding
    private val fireStore = FirebaseFirestore.getInstance()
    private val fireStorage = FirebaseStorage.getInstance()
    private lateinit var categorySpinner: Spinner
    private lateinit var offerSpinner: Spinner
    private val PICK_IMAGE_REQUEST = 71 // Unique request code

    private lateinit var nameEditText: TextInputEditText
    private lateinit var priceEditText: TextInputEditText
    private lateinit var stockEditText: TextInputEditText
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var saveButton: Button

    private lateinit var productImageView: ImageView

    // Lưu URI của hình ảnh tạm thời
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val productId = intent.getStringExtra("PRODUCT_ID")
        if (productId != null) {
            fetchProductDetails(productId)
        }

        nameEditText = findViewById(com.zebrand.app1food30s.R.id.input_name)
        priceEditText = findViewById(com.zebrand.app1food30s.R.id.input_price)
        stockEditText = findViewById(com.zebrand.app1food30s.R.id.input_stock)
        descriptionEditText = findViewById(com.zebrand.app1food30s.R.id.input_description)
        saveButton = findViewById(com.zebrand.app1food30s.R.id.save_btn)
        productImageView = findViewById(com.zebrand.app1food30s.R.id.image_product)
        categorySpinner = findViewById(com.zebrand.app1food30s.R.id.category_spinner)
        offerSpinner = findViewById(com.zebrand.app1food30s.R.id.offer_spinner)
//        loadOffersFromFirebase()

        saveButton.setOnClickListener {
            val productId = intent.getStringExtra("PRODUCT_ID")
            productId?.let {
                updateProductDetails(it)
            }
        }

        productImageView.setOnClickListener {
            startImagePicker()
        }
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

    private fun startImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data!!
            productImageView.setImageURI(imageUri)
        }
    }

    private fun saveProductToFirestore() {
        val productName = nameEditText.text.toString().trim()
        val db = com.google.firebase.ktx.Firebase.firestore

        imageUri?.let { uri ->
            val fileName = "product${UUID.randomUUID()}.png" // Tạo một tên file duy nhất
            val storageReference = com.google.firebase.ktx.Firebase.storage.reference.child("images/product/$fileName")
            val uploadTask = storageReference.putFile(uri)

            uploadTask.addOnSuccessListener {
                // Khi tải lên thành công, lưu đường dẫn cố định vào Firestore thay vì URL
                val imagePath = "images/product/$fileName" // Đường dẫn này sẽ được lưu trong Firestore
                createAndSaveProduct(imagePath)
            }.addOnFailureListener {
                // Xử lý lỗi
            }
        } ?: run {
            // Lưu sản phẩm mà không có hình ảnh
            createAndSaveProduct("")
        }
    }

    private fun updateProductDetails(imagePath: String) {
        val productName = nameEditText.text.toString().trim()
        val productPrice = priceEditText.text.toString().toDoubleOrNull() ?: 0.0
        val productStock = stockEditText.text.toString().toIntOrNull() ?: 0
        val productDescription = descriptionEditText.text.toString().trim()
        val db = com.google.firebase.ktx.Firebase.firestore

        val selectedCategoryName = categorySpinner.selectedItem.toString()
        val selectedOfferName = offerSpinner.selectedItem.toString()

        db.collection("categories").whereEqualTo("name", selectedCategoryName).limit(1).get()
            .addOnSuccessListener { categoryDocuments ->
                if (categoryDocuments.documents.isNotEmpty()) {
                    val categoryDocumentRef = categoryDocuments.documents.first().reference

                    db.collection("offers").whereEqualTo("name", selectedOfferName).limit(1).get()
                        .addOnSuccessListener { offerDocuments ->
                            if (offerDocuments.documents.isNotEmpty()) {
                                val offerDocumentRef = offerDocuments.documents.first().reference

                                val newProductRef = db.collection("products").document()

                                val newProduct = Product(
                                    id = newProductRef.id,
                                    name = productName,
                                    idCategory = categoryDocumentRef,
                                    idOffer =                                 offerDocumentRef,
                                    price = productPrice,
                                    description = productDescription,
                                    stock = productStock,
                                    image = imagePath, // Sử dụng URL của hình ảnh đã tải lên
                                    date = Date()
                                )

                                newProductRef.set(newProduct)
                                    .addOnSuccessListener {
                                        // Xử lý thành công, ví dụ: hiển thị thông báo thành công cho người dùng
                                    }
                                    .addOnFailureListener { e ->
                                        // Xử lý thất bại, ví dụ: hiển thị thông báo lỗi cho người dùng
                                    }
                            } else {
                                // Xử lý trường hợp không tìm thấy ưu đãi phù hợp
                            }
                        }
                        .addOnFailureListener { exception ->
                            // Xử lý lỗi khi tìm kiếm ưu đãi
                        }
                } else {
                    // Xử lý trường hợp không tìm thấy danh mục phù hợp
                }
            }
            .addOnFailureListener { exception ->
                // Xử lý lỗi khi tìm kiếm danh mục
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
