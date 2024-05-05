package com.zebrand.app1food30s.ui.edit_category

import com.zebrand.app1food30s.R
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
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
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.zebrand.app1food30s.data.entity.Offer
import com.zebrand.app1food30s.data.entity.Category
import com.zebrand.app1food30s.data.entity.Product
import com.zebrand.app1food30s.databinding.ActivityEditCategoryBinding
import com.zebrand.app1food30s.ui.manage_product.ManageProductActivity
import com.zebrand.app1food30s.ui.offers.ManageOffer
import com.zebrand.app1food30s.ui.manage_category.ManageCategory
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

class EditCategory : AppCompatActivity() {
    private lateinit var binding: ActivityEditCategoryBinding
    private val fireStore = FirebaseFirestore.getInstance()
    private val fireStorage = FirebaseStorage.getInstance()
    private val PICK_IMAGE_REQUEST = 71 // Unique request code

    private lateinit var nameEditText: TextInputEditText
    private lateinit var saveButton: Button
    private lateinit var removeButton: Button

    private lateinit var categoryImageView: ImageView
    private var currentImagePath: String? = null

    // Lưu URI của hình ảnh tạm thời
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val categoryId = intent.getStringExtra("CATEGORY_ID")
        if (categoryId != null) {
            fetchProductDetails(categoryId)
        }

        setupUI()
    }

    private fun setupUI() {
        nameEditText = binding.inputName
        saveButton = binding.createBtn
        removeButton = binding.removeBtn
        categoryImageView = binding.imageCategory

        val backIcon = findViewById<View>(R.id.backIcon)
        backIcon.setOnClickListener {
            finish() // Kết thúc Activity hiện tại
        }

        saveButton.setOnClickListener {
            val categoryId = intent.getStringExtra("CATEGORY_ID")
            categoryId?.let {
                saveCategoryToFirestore(it)
            } ?: run {
                Toast.makeText(this, "Error: Product ID is missing.", Toast.LENGTH_SHORT).show()
            }
        }

        removeButton.setOnClickListener {
            val categoryId = intent.getStringExtra("CATEGORY_ID")
            categoryId?.let {
                deleteCategory(it)
            } ?: run {
                Toast.makeText(this, "Error: Offer ID is missing.", Toast.LENGTH_SHORT).show()
            }
        }


        categoryImageView.setOnClickListener {
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
            Picasso.get().load(imageUri).into(categoryImageView)
        }
    }

//    private fun deleteCategory(categoryId: String) {
//        val categoryRef = fireStore.collection("categories").document(categoryId)
//        categoryRef
//            .delete()
//            .addOnSuccessListener {
//                Toast.makeText(this, "Category deleted successfully", Toast.LENGTH_SHORT).show()
//                // Cập nhật các sản phẩm có idCategory là categoryRef
//                unsetcategoryIdInProducts(categoryRef)
//                val intent = Intent(this, ManageOffer::class.java)
//                startActivity(intent)
//            }
//            .addOnFailureListener { e ->
//                Toast.makeText(this, "Error deleting offer: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//    }

    private fun deleteCategory(categoryId: String) {
        // Tạo một tham chiếu đến document của danh mục cần xóa
        val categoryRef = fireStore.collection("categories").document(categoryId)

        // Truy vấn để tìm tất cả sản phẩm liên quan đến danh mục dựa trên DocumentReference
        fireStore.collection("products")
            .whereEqualTo("idCategory", categoryRef)
            .get()
            .addOnSuccessListener { documents ->
                // Tạo một batch để xóa nhiều document cùng lúc cho hiệu suất
                val batch = fireStore.batch()
                for (document in documents) {
                    // Thêm mỗi document sản phẩm vào batch để xóa
                    batch.delete(fireStore.collection("products").document(document.id))
                }

                // Thực thi batch
                batch.commit().addOnCompleteListener {
                    // Xóa danh mục sau khi tất cả sản phẩm liên quan đã được xóa
                    categoryRef.delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Category and all related products deleted successfully", Toast.LENGTH_SHORT).show()
                            finish() // Hoặc chuyển hướng người dùng về màn hình quản lý danh mục
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error deleting category: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to delete related products: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error finding related products: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



    private fun unsetcategoryIdInProducts(categoryRef: DocumentReference) {
        fireStore.collection("products")
            .whereEqualTo("idCategory", categoryRef)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    fireStore.collection("products").document(document.id)
                        .update("idCategory", null)
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to update product: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                Toast.makeText(this, "All related products have been updated.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error finding related products: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun saveCategoryToFirestore(categoryId: String) {
        val categoryName = nameEditText.text.toString().trim()

        if (categoryName.isEmpty()) {
            Toast.makeText(this, "Please enter category name!", Toast.LENGTH_SHORT).show()
            return
        }

        imageUri?.let { uri ->
            val fileName = "product${UUID.randomUUID()}.png"
            val storageReference = fireStorage.reference.child("images/category/$fileName")
            val uploadTask = storageReference.putFile(uri)

            uploadTask.addOnSuccessListener {
                val imagePath = "images/category/$fileName"
                updateCategoryDetails(categoryId, imagePath)
            }.addOnFailureListener {
                Toast.makeText(this, "Image upload failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
        } ?: run {
            updateCategoryDetails(categoryId, currentImagePath!!)
        }
    }

    private fun updateCategoryDetails(categoryId: String, imagePath: String) {
        val categoryName = nameEditText.text.toString().trim()

        val categoryUpdate = hashMapOf<String, Any>(
            "name" to categoryName,
            "image" to imagePath,
            "date" to Date()
        )

        fireStore.collection("categories").document(categoryId).update(categoryUpdate)
            .addOnSuccessListener {
                // Xử lý thành công, ví dụ: hiển thị thông báo thành công cho người dùng
                finish()
            }
            .addOnFailureListener { e ->
                // Xử lý thất bại, ví dụ: hiển thị thông báo lỗi cho người dùng
            }
    }

    private fun fetchProductDetails(categoryId: String) {
        lifecycleScope.launch {
            try {
                val documentSnapshot = fireStore.collection("categories").document(categoryId).get().await()
                val category = documentSnapshot.toObject(Offer::class.java)
                category?.let {
                    with(binding) {
                        inputName.setText(it.name)
                        val imageUrl = fireStorage.reference.child(it.image).downloadUrl.await().toString()
                        Picasso.get().load(imageUrl).into(imageCategory)
                        currentImagePath = it.image
                    }
                }
            } catch (e: Exception) {
                // Xử lý lỗi
            }
        }
    }
}
