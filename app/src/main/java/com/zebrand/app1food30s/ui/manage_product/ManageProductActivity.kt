package com.zebrand.app1food30s.ui.manage_product

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.ManageOfferAdapter
import com.zebrand.app1food30s.adapter.ManageProductAdapter
import com.zebrand.app1food30s.data.Offer
import com.zebrand.app1food30s.data.Category
import com.zebrand.app1food30s.data.Product
import com.zebrand.app1food30s.databinding.ActivityManageProductBinding
import com.zebrand.app1food30s.ui.edit_product.EditProduct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ManageProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityManageProductBinding
    private lateinit var rcv: RecyclerView
    private val fireStore = FirebaseFirestore.getInstance()
    private val fireStorage = FirebaseStorage.getInstance()
    private lateinit var addButton: ImageView
    private lateinit var filterButton: ImageView
    private lateinit var botDialog: BottomSheetDialog
    lateinit var statusArr: Array<String>
    val customerArr = arrayOf("1- Bedroom", "2- Bedroom", "3- Bedroom")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageProductBinding.inflate(layoutInflater)

        setContentView(binding.root)
        handleDisplayProductList()

        addButton = findViewById(R.id.add_product_btn)
        filterButton = findViewById(R.id.filter_btn)

        addButton.setOnClickListener {
            val intent = Intent(this, ManageProductDetailActivity::class.java)
            startActivity(intent)
        }

        filterButton.setOnClickListener {
            showBottomSheet()
        }
    }

    private fun handleDisplayProductList() {
        lifecycleScope.launch {
            val adapter = ManageProductAdapter(getListProducts(), onProductClick = { product ->
                val intent = Intent(this@ManageProductActivity, EditProduct::class.java).apply {
                    putExtra("PRODUCT_ID", product.id)
                }
                startActivity(intent)
            })
            binding.productRcv.layoutManager = LinearLayoutManager(this@ManageProductActivity)
            binding.productRcv.adapter = adapter
        }
    }

    private fun showBottomSheet() {
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_manage_order, null)
        botDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        botDialog.setContentView(dialogView)

        // status dropdown
        val adapterStatus = ArrayAdapter(this, R.layout.item_drop_down_filter, statusArr)
        val tv_autoStatus: AutoCompleteTextView = dialogView.findViewById(R.id.spinnerStatus)
        tv_autoStatus.setAdapter(adapterStatus)
        tv_autoStatus.setOnItemClickListener { _, _, position, _ ->
            val selectedText = adapterStatus.getItem(position)
//            Toast.makeText(this, selectedText, Toast.LENGTH_LONG).show()
        }

        // customer dropdown
        val adapterCus = ArrayAdapter(this, R.layout.item_drop_down_filter, customerArr)
        val tv_autoCus: AutoCompleteTextView = dialogView.findViewById(R.id.spinnerCustomer)
        tv_autoCus.setAdapter(adapterCus)
        tv_autoCus.setOnItemClickListener { _, _, position, _ ->
            val selectedText = adapterCus.getItem(position)
//            Toast.makeText(this, selectedText, Toast.LENGTH_LONG).show()
        }

        // date picker
        val datePickerText: TextInputEditText = dialogView.findViewById(R.id.datePicker)
        val myCalendar = Calendar.getInstance()
        val datePicker = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val myFormat = "dd/MM/yyyy"
            val sdf = SimpleDateFormat(myFormat, Locale.UK)
            val formattedDate = sdf.format(myCalendar.time)
//            Log.d("dateABC", formattedDate)
            datePickerText.setText(formattedDate)
        }

        datePickerText.setOnClickListener {
            DatePickerDialog(
                this,
                R.style.MyDatePickerDialogStyle, datePicker, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        botDialog.show()
    }


    private suspend fun getListProducts(): List<Product> {
        return withContext(Dispatchers.IO) {
            try {
                val querySnapshot = fireStore.collection("products").get().await()
                querySnapshot.documents.mapNotNull { document ->
                    val id = document.id
                    val name = document.getString("name") ?: ""
                    val image = document.getString("image") ?: "images/product/product3.png"
                    val imageUrl = fireStorage.reference.child(image).downloadUrl.await().toString()
                    val price = document.getDouble("price") ?: 0.0
                    val description = document.getString("description") ?: ""
                    val stock = document.getLong("stock")?.toInt() ?: 0
                    val sold = document.getLong("sold")?.toInt() ?: 0
                    val idCategoryRef = document.getDocumentReference("idCategory")
                    val idOfferRef = document.getDocumentReference("idOffer")

                    Product(id, idCategoryRef, idOfferRef, name, imageUrl, price, description, stock, sold, null, document.getDate("date"))
                }
            } catch (e: Exception) {
                Log.e("getListProducts", "Error getting products", e)
                emptyList()
            }
        }
    }

}
