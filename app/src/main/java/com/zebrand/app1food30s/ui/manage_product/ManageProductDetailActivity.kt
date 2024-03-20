package com.zebrand.app1food30s.ui.manage_product

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.data.Category

class ManageProductDetailActivity : AppCompatActivity() {
    private lateinit var productImageView: ImageView
    private lateinit var categorySpinner: Spinner
    // Assume there's a Spinner for offers if you want to implement it similarly for offers

    companion object {
        private const val REQUEST_PERMISSION = 1
        private const val PICK_IMAGE_REQUEST = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_product_detail)

        setupImageView()
        categorySpinner = findViewById(R.id.category_spinner)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSION)
        }

        loadCategoriesFromFirebase()
    }

    private fun setupImageView() {
        productImageView = findViewById(R.id.image_product)
        productImageView.setOnClickListener {
            val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickPhotoIntent, PICK_IMAGE_REQUEST)
        }
    }

    private fun loadCategoriesFromFirebase() {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Categories")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val categories = ArrayList<String>()
                for (postSnapshot in dataSnapshot.children) {
                    val category = postSnapshot.getValue(Category::class.java)
                    category?.name?.let { categories.add(it) }
                }
                val categoryAdapter = ArrayAdapter(this@ManageProductDetailActivity, android.R.layout.simple_spinner_dropdown_item, categories)
                categorySpinner.adapter = categoryAdapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, no further action required here
        } else {
            // Display a message to the user explaining that the permission was denied and the feature will not work
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            productImageView.setImageURI(selectedImageUri)
        }
    }
}
