package com.zebrand.app1food30s.ui.manage_product

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.zebrand.app1food30s.R

class ManageProductDetailActivity : AppCompatActivity() {
    private lateinit var productImageView: ImageView

    companion object {
        private const val REQUEST_PERMISSION = 1
        private const val PICK_IMAGE_REQUEST = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_product_detail)

        setupDropdowns()
        setupImageView()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSION)
        }
    }

    private fun setupDropdowns() {
        val prices = resources.getStringArray(R.array.prices_array)
        val offers = resources.getStringArray(R.array.offers_array)

        val priceAdapter = ArrayAdapter(this, R.layout.dropdown_menu_popup_item, prices)
        val offerAdapter = ArrayAdapter(this, R.layout.dropdown_menu_popup_item, offers)

        val priceDropdown: AutoCompleteTextView = findViewById(R.id.price_dropdown)
        val offerDropdown: AutoCompleteTextView = findViewById(R.id.offer_dropdown)

        priceDropdown.setAdapter(priceAdapter)
        offerDropdown.setAdapter(offerAdapter)
    }

    private fun setupImageView() {
        productImageView = findViewById(R.id.image_product)
        productImageView.setOnClickListener {
            val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickPhotoIntent, PICK_IMAGE_REQUEST)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Quyền đã được cấp, không cần thực hiện gì thêm ở đây
        } else {
            // Hiển thị thông báo cho người dùng biết quyền bị từ chối và chức năng không hoạt động
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
