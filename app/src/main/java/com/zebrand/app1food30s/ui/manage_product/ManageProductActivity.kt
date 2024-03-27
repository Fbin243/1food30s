package com.zebrand.app1food30s.ui.manage_product

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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
    private lateinit var categorySpinner: Spinner
    private lateinit var priceSpinner: Spinner
    lateinit var categoryArr: ArrayList<String>
    val priceArr = arrayOf("1$ to 10$", "11$ to 50$", "51$ to 100$", "More than 100$")

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
//            categoryArr = resources.getStringArray(R.array.delivery_array)
            showFilterProduct()
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


    private fun showFilterProduct() {
        val dialogView = layoutInflater.inflate(R.layout.pop_up_filter_manage_product, null)
        botDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        botDialog.setContentView(dialogView)

        categorySpinner = dialogView.findViewById(R.id.spinnerCategory)
        priceSpinner = dialogView.findViewById(R.id.spinnerPrice)

        loadCategoriesFromFirebase()

        val adapterPrice = ArrayAdapter(this, android.R.layout.simple_spinner_item, priceArr)
        adapterPrice.setDropDownViewResource(R.layout.dropdown_menu_popup_item)
        priceSpinner.adapter = adapterPrice

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
