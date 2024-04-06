package com.zebrand.app1food30s.ui.manage_category

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
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.zebrand.app1food30s.data.entity.Offer
import com.zebrand.app1food30s.data.entity.Category
import com.zebrand.app1food30s.R
import java.util.Date
import java.util.UUID
import com.google.firebase.storage.ktx.storage

class ManageCategoryDetail : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 71 // Unique request code

    private lateinit var nameEditText: TextInputEditText
    private lateinit var numProductEditText: TextInputEditText
    private lateinit var createButton: Button

    private lateinit var categoryImageView: ImageView

    // Lưu URI của hình ảnh tạm thời
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_category_detail)

        nameEditText = findViewById(R.id.input_name)
        numProductEditText = findViewById(R.id.input_num_product)
        createButton = findViewById(R.id.create_btn)
        categoryImageView = findViewById(R.id.image_category)

        createButton.setOnClickListener {
            saveProductToFirestore()
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
            categoryImageView.setImageURI(imageUri)
        }
    }

    private fun saveProductToFirestore() {
        val categoryName = nameEditText.text.toString().trim()
        val db = Firebase.firestore

        imageUri?.let { uri ->
            val fileName = "product${UUID.randomUUID()}.png" // Tạo một tên file duy nhất
            val storageReference = Firebase.storage.reference.child("images/category/$fileName")
            val uploadTask = storageReference.putFile(uri)

            uploadTask.addOnSuccessListener {
                // Khi tải lên thành công, lưu đường dẫn cố định vào Firestore thay vì URL
                val imagePath = "images/category/$fileName" // Đường dẫn này sẽ được lưu trong Firestore
                createAndSaveCategory(imagePath)
            }.addOnFailureListener {
                // Xử lý lỗi
            }
        } ?: run {
            // Lưu sản phẩm mà không có hình ảnh
            createAndSaveCategory("")
        }
    }

    private fun createAndSaveCategory(imagePath: String) {
        val categoryName = nameEditText.text.toString().trim()
        val categoryNumProduct = numProductEditText.text.toString().toIntOrNull() ?: 0
        val db = Firebase.firestore

        val newCategoryRef = db.collection("categories").document()

        val newCategory = Category(
            id = newCategoryRef.id,
            name = categoryName,
            numProduct = categoryNumProduct,
            image = imagePath, // Sử dụng URL của hình ảnh đã tải lên
            date = Date()
        )

        newCategoryRef.set(newCategory)
            .addOnSuccessListener {
                // Xử lý thành công, ví dụ: hiển thị thông báo thành công cho người dùng
                val intent = Intent(this, ManageCategory::class.java)
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                // Xử lý thất bại, ví dụ: hiển thị thông báo lỗi cho người dùng
            }
    }
}

