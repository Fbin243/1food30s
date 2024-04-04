package com.zebrand.app1food30s.ui.edit_offer

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
import com.zebrand.app1food30s.data.entity.Offer
import com.zebrand.app1food30s.data.entity.Product
import com.zebrand.app1food30s.databinding.ActivityEditOfferBinding
import com.zebrand.app1food30s.ui.manage_product.ManageProductActivity
import com.zebrand.app1food30s.ui.offers.ManageOffer
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

class EditOffer : AppCompatActivity() {
    private lateinit var binding: ActivityEditOfferBinding
    private val fireStore = FirebaseFirestore.getInstance()
    private val fireStorage = FirebaseStorage.getInstance()
    private val PICK_IMAGE_REQUEST = 71 // Unique request code

    private lateinit var nameEditText: TextInputEditText
    private lateinit var discountRateEditText: TextInputEditText
    private lateinit var numProductEditText: TextInputEditText
    private lateinit var saveButton: Button

    private lateinit var offerImageView: ImageView
    private var currentImagePath: String? = null

    // Lưu URI của hình ảnh tạm thời
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditOfferBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val offerId = intent.getStringExtra("OFFER_ID")
        if (offerId != null) {
            fetchProductDetails(offerId)
        }

        setupUI()
    }

    private fun setupUI() {
        nameEditText = binding.inputName
        discountRateEditText = binding.inputDiscountRate
        numProductEditText = binding.inputNumProduct
        saveButton = binding.createBtn
        offerImageView = binding.imageOffer

        saveButton.setOnClickListener {
            val offerId = intent.getStringExtra("OFFER_ID")
            offerId?.let {
                saveOfferToFirestore(it)
            } ?: run {
                Toast.makeText(this, "Error: Product ID is missing.", Toast.LENGTH_SHORT).show()
            }
        }

        offerImageView.setOnClickListener {
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
            Picasso.get().load(imageUri).into(offerImageView)
        }
    }

    private fun saveOfferToFirestore(offerId: String) {
        val offerName = nameEditText.text.toString().trim()

        imageUri?.let { uri ->
            val fileName = "product${UUID.randomUUID()}.png"
            val storageReference = fireStorage.reference.child("images/offer/$fileName")
            val uploadTask = storageReference.putFile(uri)

            uploadTask.addOnSuccessListener {
                val imagePath = "images/offer/$fileName"
                updateOfferDetails(offerId, imagePath)
            }.addOnFailureListener {
                Toast.makeText(this, "Image upload failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
        } ?: run {
            updateOfferDetails(offerId, currentImagePath!!)
        }
    }

    private fun updateOfferDetails(offerId: String, imagePath: String) {
        val offerName = nameEditText.text.toString().trim()
        val offerDiscountRate = discountRateEditText.text.toString().toIntOrNull() ?: 0
        val offerNumProduct = numProductEditText.text.toString().toIntOrNull() ?: 0

        val offerUpdate = hashMapOf<String, Any>(
            "name" to offerName,
            "discountRate" to offerDiscountRate,
            "numProduct" to offerNumProduct,
            "image" to imagePath,
            "date" to Date()
        )

        fireStore.collection("offers").document(offerId).update(offerUpdate)
            .addOnSuccessListener {
                // Xử lý thành công, ví dụ: hiển thị thông báo thành công cho người dùng
                val intent = Intent(this, ManageOffer::class.java)
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                // Xử lý thất bại, ví dụ: hiển thị thông báo lỗi cho người dùng
            }
    }

    private fun fetchProductDetails(offerId: String) {
        lifecycleScope.launch {
            try {
                val documentSnapshot = fireStore.collection("offers").document(offerId).get().await()
                val offer = documentSnapshot.toObject(Offer::class.java)
                offer?.let {
                    with(binding) {
                        inputName.setText(it.name)
                        inputDiscountRate.setText(it.discountRate.toString())
                        inputNumProduct.setText(it.numProduct.toString())
                        val imageUrl = fireStorage.reference.child(it.image).downloadUrl.await().toString()
                        Picasso.get().load(imageUrl).into(imageOffer)
                        currentImagePath = it.image
                    }
                }
            } catch (e: Exception) {
                // Xử lý lỗi
            }
        }
    }
}
