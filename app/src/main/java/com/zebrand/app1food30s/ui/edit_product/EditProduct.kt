package com.zebrand.app1food30s.ui.edit_product

import com.zebrand.app1food30s.R
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.zebrand.app1food30s.data.entity.Product
import com.zebrand.app1food30s.databinding.ActivityEditProductBinding
import com.zebrand.app1food30s.ui.manage_product.ManageProductActivity
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

class EditProduct : AppCompatActivity() {
    private lateinit var binding: ActivityEditProductBinding
    private val fireStore = FirebaseFirestore.getInstance()
    private val fireStorage = FirebaseStorage.getInstance()
    private lateinit var categoryAutoComplete: AutoCompleteTextView
    private lateinit var offerAutoComplete: AutoCompleteTextView
    private val PICK_IMAGE_REQUEST = 71 // Unique request code

    private lateinit var nameEditText: TextInputEditText
    private lateinit var priceEditText: TextInputEditText
    private lateinit var stockEditText: TextInputEditText
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button

    private lateinit var productImageView: ImageView
    private var currentImagePath: String? = null
    private var currentCategory: String? = null
    private var currentOffer: String? = null

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

        setupUI()
    }

    private fun setupUI() {
        nameEditText = binding.inputName
        priceEditText = binding.inputPrice
        stockEditText = binding.inputStock
        descriptionEditText = binding.inputDescription
        saveButton = binding.saveBtn
        deleteButton = binding.deleteBtn
        productImageView = binding.imageProduct
        categoryAutoComplete = binding.autoCompleteCategory
        offerAutoComplete = binding.autoCompleteOffer

        saveButton.setOnClickListener {
            val productId = intent.getStringExtra("PRODUCT_ID")
            productId?.let {
                saveProductToFirestore(it)
            } ?: run {
                Toast.makeText(this, "Error: Product ID is missing.", Toast.LENGTH_SHORT).show()
            }
        }

        deleteButton.setOnClickListener {
            val productId = intent.getStringExtra("PRODUCT_ID")
            if (productId != null) {
                deleteProduct(productId)
            } else {
                Toast.makeText(this, "Error: Product ID is missing.", Toast.LENGTH_SHORT).show()
            }
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
            Picasso.get().load(imageUri).into(productImageView)
        }
    }


    private fun deleteProduct(productId: String) {
        val productRef = fireStore.collection("products").document(productId)

        productRef.get().addOnSuccessListener { document ->
            val categoryId = document.getString("idCategory")
            val offerId = document.getString("idOffer")

            // Tiếp tục xóa sản phẩm
            productRef.delete().addOnSuccessListener {
                // Cập nhật số lượng sản phẩm cho danh mục và ưu đãi
                if (categoryId != null) {
                    updateNumProductInCategory(categoryId, -1)
                }
                if (offerId != null) {
                    updateNumProductInOffer(offerId, -1)
                }

                Toast.makeText(this, "Product deleted successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ManageProductActivity::class.java)
                startActivity(intent)
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Error deleting product: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error fetching product details: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun updateNumProductInCategory(categoryId: String, amount: Int) {
        val categoryRef = fireStore.collection("categories").document(categoryId)
        categoryRef.update("numProduct", FieldValue.increment(amount.toLong()))
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error updating category: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateNumProductInOffer(offerId: String, amount: Int) {
        val offerRef = fireStore.collection("offers").document(offerId)
        offerRef.update("numProduct", FieldValue.increment(amount.toLong()))
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error updating offer: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun saveProductToFirestore(productId: String) {
        val productName = nameEditText.text.toString().trim()

        imageUri?.let { uri ->
            val fileName = "product${UUID.randomUUID()}.png"
            val storageReference = fireStorage.reference.child("images/product/$fileName")
            val uploadTask = storageReference.putFile(uri)

            uploadTask.addOnSuccessListener {
                val imagePath = "images/product/$fileName"
                updateProductDetails(productId, imagePath)
            }.addOnFailureListener {
                Toast.makeText(this, "Image upload failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
        } ?: run {
            updateProductDetails(productId, currentImagePath!!)
        }
    }

    private fun updateProductDetails(productId: String, imagePath: String) {
        val productName = nameEditText.text.toString().trim()
        val productPrice = priceEditText.text.toString().toDoubleOrNull() ?: 0.0
        val productStock = stockEditText.text.toString().toIntOrNull() ?: 0
        val productDescription = descriptionEditText.text.toString().trim()

        val selectedCategoryName = categoryAutoComplete.text.toString()
        val selectedOfferName = offerAutoComplete.text.toString()

        fireStore.collection("categories").whereEqualTo("name", selectedCategoryName).limit(1).get()
            .addOnSuccessListener { categoryDocuments ->
                if (categoryDocuments.documents.isNotEmpty()) {
                    val categoryDocumentRef = categoryDocuments.documents.first().reference

                    fireStore.collection("offers").whereEqualTo("name", selectedOfferName).limit(1).get()
                        .addOnSuccessListener { offerDocuments ->
                            if (offerDocuments.documents.isNotEmpty()) {
                                val offerDocumentRef = offerDocuments.documents.first().reference

                                val productUpdate = hashMapOf<String, Any>(
                                    "name" to productName,
                                    "idCategory" to categoryDocumentRef,
                                    "idOffer" to offerDocumentRef,
                                    "price" to productPrice,
                                    "description" to productDescription,
                                    "stock" to productStock,
                                    "image" to imagePath,
                                    "date" to Date()
                                )

                                fireStore.collection("products").document(productId).update(productUpdate)
//                                    .addOnSuccessListener {
//                                        Toast.makeText(this, "Product updated successfully", Toast.LENGTH_LONG).show()
//                                        val intent = Intent(this, ManageProductActivity::class.java)
//                                        startActivity(intent)
//                                    }
                                    .addOnSuccessListener {
                                        categoryDocumentRef.get()
                                            .addOnSuccessListener { documentSnapshot ->
                                                val currentNumProductCategory = documentSnapshot.getLong("numProduct") ?: 0
                                                val newNumProductCategory = currentNumProductCategory + 1
                                                categoryDocumentRef.update("numProduct", newNumProductCategory)
                                                    .addOnSuccessListener {
                                                        offerDocumentRef.get()
                                                            .addOnSuccessListener { offerSnapshot ->
                                                                val currentNumProductOffer = offerSnapshot.getLong("numProduct") ?: 0
                                                                val newNumProductOffer = currentNumProductOffer + 1
                                                                offerDocumentRef.update("numProduct", newNumProductOffer)
                                                                    .addOnSuccessListener {
                                                                        Toast.makeText(this, "Product updated successfully", Toast.LENGTH_SHORT).show()
                                                                        val intent = Intent(this, ManageProductActivity::class.java)
                                                                        startActivity(intent)
                                                                    }
                                                                    .addOnFailureListener { e ->
                                                                        Toast.makeText(this, "Failed to update offer count: ${e.message}", Toast.LENGTH_SHORT).show()
                                                                    }
                                                            }
                                                            .addOnFailureListener { e ->
                                                                Toast.makeText(this, "Failed to get current offer count: ${e.message}", Toast.LENGTH_SHORT).show()
                                                            }
                                                    }
                                                    .addOnFailureListener { e ->
                                                        // Handle failure for updating the category
                                                        Toast.makeText(this, "Failed to update category count: ${e.message}", Toast.LENGTH_SHORT).show()
                                                    }
                                            }
                                            .addOnFailureListener { e ->
                                                // Handle failure for getting the current category count
                                                Toast.makeText(this, "Failed to get current category count: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Error updating product: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                            } else {
                                Toast.makeText(this, "Offer not found", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error finding offer: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(this, "Category not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error finding category: ${e.message}", Toast.LENGTH_LONG).show()
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
                val adapter = ArrayAdapter(this, R.layout.dropdown_menu_popup_item, categoriesList)
                categoryAutoComplete.setAdapter(adapter)

                if (categoryStr.isNotEmpty()) {
                    categoryAutoComplete.setText(categoryStr, false) // Set text và không filter kết quả dựa trên text được set
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
                val adapter = ArrayAdapter(this, R.layout.dropdown_menu_popup_item, offerList)
                offerAutoComplete.setAdapter(adapter)

                if (offerStr.isNotEmpty()) {
                    offerAutoComplete.setText(offerStr, false) // Set text và không filter kết quả dựa trên text được set
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
                        currentImagePath = it.image

                        val offerId = it.idOffer?.id ?: "non"
                        val offersCollection = FirebaseFirestore.getInstance().collection("offers")
                        offersCollection.document(offerId).get().addOnSuccessListener { document ->
                            if (document != null && document.exists()) {
//                val category = document.toObject(Category::class.java)
//                                categoryAutoComplete.text = document.getString("name") ?: ""
                                val offerStr = document.getString("name") ?: ""
                                offersCollection.document(offerId).update("numProduct", FieldValue.increment(-1))
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
//                                categoryAutoComplete.text = document.getString("name") ?: ""
                                categoriesCollection.document(categoryId).update("numProduct", FieldValue.increment(-1))
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

//    private fun updateProductDetails(productId: String) {
//        val updatedName = binding.inputName.text.toString()
//        val updatedPrice = binding.inputPrice.text.toString().toDoubleOrNull()
//        val updatedStock = binding.inputStock.text.toString().toIntOrNull()
//        val updatedDescription = binding.inputDescription.text.toString()
//        // Lấy các giá trị cập nhật từ người dùng
//
//        lifecycleScope.launch {
//            try {
//                fireStore.collection("products").document(productId).update(
//                    mapOf(
//                        "name" to updatedName,
//                        "price" to updatedPrice,
//                        "stock" to updatedStock,
//                        "description" to updatedDescription
//                        // Cập nhật các trường khác tương tự như trên
//                    )
//                ).await()
//                // Xử lý sau khi cập nhật thành công, ví dụ: hiển thị thông báo, đóng màn hình
//            } catch (e: Exception) {
//                // Xử lý lỗi
//            }
//        }
//    }
}
