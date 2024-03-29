package com.zebrand.app1food30s.ui.manage_product

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.zebrand.app1food30s.data.Category
import com.zebrand.app1food30s.data.Product
import com.zebrand.app1food30s.R
import java.util.Date
import java.util.UUID
import com.google.firebase.storage.ktx.storage

class ManageProductDetailActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 71 // Unique request code

    private lateinit var nameEditText: TextInputEditText
    private lateinit var priceEditText: TextInputEditText
    private lateinit var stockEditText: TextInputEditText
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var createButton: Button
    private lateinit var categorySpinner: Spinner
    private lateinit var offerSpinner: Spinner

    private lateinit var productImageView: ImageView

    // Lưu URI của hình ảnh tạm thời
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_product_detail)

        nameEditText = findViewById(R.id.input_name)
        priceEditText = findViewById(R.id.input_price)
        stockEditText = findViewById(R.id.input_stock)
        descriptionEditText = findViewById(R.id.input_description)
        createButton = findViewById(R.id.create_btn)
        categorySpinner = findViewById(R.id.category_spinner)
        offerSpinner = findViewById(R.id.offer_spinner)
        productImageView = findViewById(R.id.image_product)
        loadCategoriesFromFirebase()
        loadOffersFromFirebase()

        createButton.setOnClickListener {
            saveProductToFirestore()
        }

        productImageView.setOnClickListener {
            startImagePicker()
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
        val db = Firebase.firestore

        imageUri?.let { uri ->
            val fileName = "product${UUID.randomUUID()}.png" // Tạo một tên file duy nhất
            val storageReference = Firebase.storage.reference.child("images/product/$fileName")
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

    private fun createAndSaveProduct(imagePath: String) {
        val productName = nameEditText.text.toString().trim()
        val productPrice = priceEditText.text.toString().toDoubleOrNull() ?: 0.0
        val productStock = stockEditText.text.toString().toIntOrNull() ?: 0
        val productDescription = descriptionEditText.text.toString().trim()
        val db = Firebase.firestore

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
                                        val intent = Intent(this, ManageProductActivity::class.java)
                                        startActivity(intent)
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

    private fun loadCategoriesFromFirebase() {
        val db = Firebase.firestore
        val categoriesList = ArrayList<String>()

        db.collection("categories").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    categoriesList.add(document.getString("name") ?: "")
                }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoriesList)
                adapter.setDropDownViewResource(R.layout.dropdown_menu_popup_item)
                categorySpinner.adapter = adapter
            }
            .addOnFailureListener { exception ->
                // Xử lý lỗi ở đây
            }
    }

    private fun loadOffersFromFirebase() {
        val db = Firebase.firestore
        val offersList = ArrayList<String>()

        db.collection("offers").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    offersList.add(document.getString("name") ?: "")
                }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, offersList)
                adapter.setDropDownViewResource(R.layout.dropdown_menu_popup_item)
                offerSpinner.adapter = adapter
            }
            .addOnFailureListener { exception ->
                // Xử lý lỗi ở đây
            }
    }
}

