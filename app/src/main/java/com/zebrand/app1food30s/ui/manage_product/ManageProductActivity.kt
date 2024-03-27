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
import com.google.android.material.button.MaterialButton
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
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
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
    private lateinit var nameFilterEditText: TextInputEditText
    private lateinit var datePickerText: TextInputEditText
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
        nameFilterEditText = dialogView.findViewById(R.id.nameFilter)
        categorySpinner = dialogView.findViewById(R.id.spinnerCategory)
        priceSpinner = dialogView.findViewById(R.id.spinnerPrice)
        datePickerText = dialogView.findViewById(R.id.datePicker)

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

        // Setup for filter button
        botDialog.findViewById<MaterialButton>(R.id.saveBtn)?.setOnClickListener {
            filterProducts()
        }

        // Setup for cancel button
        botDialog.findViewById<MaterialButton>(R.id.cancelBtn)?.setOnClickListener {
            botDialog.dismiss()
        }

        botDialog.show()
    }



    private fun filterProducts() {
        lifecycleScope.launch {
            val nameFilter = nameFilterEditText.text.toString().trim()
            val selectedCategory = categorySpinner.selectedItem.toString()
            val selectedPriceRange = priceSpinner.selectedItem.toString()
            val selectedDate = datePickerText.text.toString()

            val allProducts = getListProducts() // Ensure this is your current method to fetch all products

            var filteredProducts = filterProductsByName(nameFilter, allProducts)
            filteredProducts = filterProductsByCategory(selectedCategory, filteredProducts)
            filteredProducts = filterProductsByPriceRange(selectedPriceRange, filteredProducts)
            filteredProducts = filterProductsByDate(selectedDate, filteredProducts)

            displayFilteredProducts(filteredProducts)
        }
    }

    private fun filterProductsByName(nameFilter: String, products: List<Product>): List<Product> {
        return products.filter { it.name.contains(nameFilter, ignoreCase = true) }
    }

    private fun filterProductsByCategory(selectedCategory: String, products: List<Product>): List<Product> {
        // This assumes you have a method to resolve category names to their IDs or directly filter by name
        val db = Firebase.firestore
        db.collection("categories").whereEqualTo("name", selectedCategory).limit(1).get()
            .addOnSuccessListener { categoryDocuments ->
                if (categoryDocuments.documents.isNotEmpty()) {
                    val categoryDocumentRef = categoryDocuments.documents.first().reference
                    products.filter { it.idCategory == categoryDocumentRef }
                } else {
                    // Xử lý trường hợp không tìm thấy danh mục phù hợp
                }
            }
            .addOnFailureListener { exception ->
                // Xử lý lỗi khi tìm kiếm danh mục
            }
        return products
    }

    private fun filterProductsByPriceRange(selectedPriceRange: String, products: List<Product>): List<Product> {
        // Parse selectedPriceRange to min and max values, then filter
        // Ví dụ: chuyển đổi "1$ to 10$" thành một cặp giá trị (1.0, 10.0)
        val range = selectedPriceRange.split(" to ").mapNotNull { it.filter { char -> char.isDigit() }.toDoubleOrNull() }
        if (range.size == 2) {
            return products.filter {
                it.price >= range[0] && it.price <= range[1]
            }
        }
        return products
    }

    private fun filterProductsByDate(selectedDate: String, products: List<Product>): List<Product> {
        // Parse selectedDate and filter products based on this date
        val sdf = SimpleDateFormat("MM/dd/yy", Locale.US)
        return try {
            val parsedDate = sdf.parse(selectedDate)
            products.filter {
                it.date == parsedDate
            }
        } catch (e: ParseException) {
            products // Trả về tất cả sản phẩm nếu có lỗi khi parse
        }
    }

    private fun displayFilteredProducts(filteredProducts: List<Product>) {
        // Update RecyclerView with filteredProducts
        val adapter = ManageProductAdapter(filteredProducts, onProductClick = { product ->
            val intent = Intent(this@ManageProductActivity, EditProduct::class.java).apply {
                putExtra("PRODUCT_ID", product.id)
            }
            startActivity(intent)
        })
        binding.productRcv.layoutManager = LinearLayoutManager(this@ManageProductActivity)
        binding.productRcv.adapter = adapter
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
