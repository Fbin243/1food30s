package com.zebrand.app1food30s.ui.profile

import com.zebrand.app1food30s.R
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
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
import com.zebrand.app1food30s.data.User
import com.zebrand.app1food30s.databinding.ActivityEditProfileBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private val fireStore = FirebaseFirestore.getInstance()
    private val fireStorage = FirebaseStorage.getInstance()
    private val PICK_IMAGE_REQUEST = 71 // Unique request code

    private lateinit var firstNameEditText: TextInputEditText
    private lateinit var lastNameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var addressEditText: TextInputEditText
    private lateinit var phoneEditText: TextInputEditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = intent.getStringExtra("USER_ID")
        if (userId != null) {
            fetchUserInformation(userId)
        }

        setupUI()
    }

    private fun setupUI() {
        firstNameEditText = binding.editFirstName
        lastNameEditText = binding.editLastName
        emailEditText = binding.editEmail
        addressEditText = binding.editAddress
        phoneEditText = binding.editPhone
        saveButton = binding.saveBtn

        saveButton.setOnClickListener {
            val userId = intent.getStringExtra("USER_ID")
            userId?.let {
                saveUserInforToFirestore(it)
            } ?: run {
                Toast.makeText(this, "Error: User ID is missing.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data!!
            Picasso.get().load(imageUri).into(productImageView)
        }
    }

    private fun saveUserInforToFirestore(userId: String) {
        val productName = firstNameEditText.text.toString().trim()

        imageUri?.let { uri ->
            val fileName = "product${UUID.randomUUID()}.png"
            val storageReference = fireStorage.reference.child("images/product/$fileName")
            val uploadTask = storageReference.putFile(uri)

            uploadTask.addOnSuccessListener {
                val imagePath = "images/product/$fileName"
                updateProductDetails(userId, imagePath)
            }.addOnFailureListener {
                Toast.makeText(this, "Image upload failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
        } ?: run {
            updateProductDetails(userId, currentImagePath!!)
        }
    }

    private fun updateProductDetails(userId: String, imagePath: String) {
        val productName = firstNameEditText.text.toString().trim()
        val productPrice = lastNameEditText.text.toString().toDoubleOrNull() ?: 0.0
        val productStock = stockEditText.text.toString().toIntOrNull() ?: 0
        val productDescription = descriptionEditText.text.toString().trim()

        val selectedCategoryName = categorySpinner.selectedItem.toString()
        val selectedOfferName = offerSpinner.selectedItem.toString()

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

                                fireStore.collection("products").document(userId).update(productUpdate)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Product updated successfully", Toast.LENGTH_LONG).show()
                                        val intent = Intent(this, ManageProductActivity::class.java)
                                        startActivity(intent)
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
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoriesList)
                adapter.setDropDownViewResource(R.layout.dropdown_menu_popup_item)
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
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, offerList)
                adapter.setDropDownViewResource(R.layout.dropdown_menu_popup_item)
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

    private fun fetchUserInformation(userId: String) {
        lifecycleScope.launch {
            try {
                val documentSnapshot = fireStore.collection("products").document(userId).get().await()
                val product = documentSnapshot.toObject(Product::class.java)
                product?.let {
                    with(binding) {
                        edit_first_name.setText(it.name)
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

//    private fun updateProductDetails(userId: String) {
//        val updatedName = binding.edit_first_name.text.toString()
//        val updatedPrice = binding.inputPrice.text.toString().toDoubleOrNull()
//        val updatedStock = binding.inputStock.text.toString().toIntOrNull()
//        val updatedDescription = binding.inputDescription.text.toString()
//        // Lấy các giá trị cập nhật từ người dùng
//
//        lifecycleScope.launch {
//            try {
//                fireStore.collection("products").document(userId).update(
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
