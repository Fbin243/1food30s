package com.zebrand.app1food30s.ui.manage_product

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.ManageProductAdapter
import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.data.entity.Product
import com.zebrand.app1food30s.databinding.ActivityManageProductBinding
import com.zebrand.app1food30s.ui.edit_product.EditProduct
import com.zebrand.app1food30s.ui.main.MainActivity
import com.zebrand.app1food30s.utils.FirebaseService
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
    private lateinit var categoryAutoComplete: AutoCompleteTextView
    private lateinit var priceAutoComplete: AutoCompleteTextView
    private lateinit var nameFilterEditText: TextInputEditText
    private lateinit var datePickerText: TextInputEditText
    private lateinit var toDatePickerText: TextInputEditText
    lateinit var categoryArr: ArrayList<String>
    val priceArr = arrayOf("1$ to 10$", "11$ to 50$", "51$ to 100$", "More than 100$")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageProductBinding.inflate(layoutInflater)

        setContentView(binding.root)
        handleDisplayProductList()

        addButton = findViewById(R.id.add_product_btn)
        filterButton = findViewById(R.id.filter_btn)


        val backIcon = findViewById<ImageView>(R.id.imageView)
        backIcon.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

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
            showShimmerEffectForProducts()
//            val db = AppDatabase.getInstance(applicationContext)
            val adapter = ManageProductAdapter(getListProducts(), onProductClick = { product ->
                val intent = Intent(this@ManageProductActivity, EditProduct::class.java).apply {
                    putExtra("PRODUCT_ID", product.id)
                }
                startActivity(intent)
            })
            binding.productRcv.layoutManager = LinearLayoutManager(this@ManageProductActivity)
            binding.productRcv.adapter = adapter
            hideShimmerEffectForProducts()
        }
    }

    fun showShimmerEffectForProducts() {
        binding.productShimmer.startShimmer()
    }

    fun hideShimmerEffectForProducts() {
        hideShimmerEffectForRcv(binding.productShimmer, binding.productRcv)
    }

    private fun hideShimmerEffectForRcv(shimmer: ShimmerFrameLayout, recyclerView: RecyclerView) {
        shimmer.stopShimmer()
        shimmer.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }

    private fun loadCategoriesFromFirebase() {
        val db = Firebase.firestore
        val categoriesList = ArrayList<String>()
//        categoriesList.add("Choose category")

        db.collection("categories").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    categoriesList.add(document.getString("name") ?: "")
                }
                // Cập nhật ArrayAdapter và thiết lập nó cho categoryAutoCompleteTextView
                val adapter = ArrayAdapter(this, R.layout.dropdown_menu_popup_item, categoriesList)
                categoryAutoComplete.setAdapter(adapter)
            }
            .addOnFailureListener { exception ->
                // Xử lý lỗi
            }
    }


    private fun showFilterProduct() {
        val dialogView = layoutInflater.inflate(R.layout.pop_up_filter_manage_product, null)
        botDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        botDialog.setContentView(dialogView)

        nameFilterEditText = dialogView.findViewById(R.id.nameFilter)
        categoryAutoComplete = dialogView.findViewById(R.id.autoCompleteCategory)
        priceAutoComplete = dialogView.findViewById(R.id.autoCompletePrice)
        datePickerText = dialogView.findViewById(R.id.datePicker)
        toDatePickerText = dialogView.findViewById(R.id.toDatePicker)

        loadCategoriesFromFirebase()

//        val adapterPrice = ArrayAdapter(this, android.R.layout.simple_spinner_item, priceArr)
//        adapterPrice.setDropDownViewResource(R.layout.dropdown_menu_popup_item)
//        priceAutoComplete.adapter = adapterPrice

        val adapterPrice = ArrayAdapter(this, R.layout.dropdown_menu_popup_item, priceArr)
//        adapterPrice.setDropDownViewResource(R.layout.dropdown_menu_popup_item)
        priceAutoComplete.setAdapter(adapterPrice)

        // date picker
        val datePickerText: TextInputEditText = dialogView.findViewById(R.id.datePicker)
        val toDatePickerText: TextInputEditText = dialogView.findViewById(R.id.toDatePicker)
        val myCalendar = Calendar.getInstance()
        val datePicker = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val myFormat = "dd/MM/yyyy"
            val sdf = SimpleDateFormat(myFormat, Locale.UK)
            val formattedDate = sdf.format(myCalendar.time)
            Log.d("dateABC", formattedDate)
            datePickerText.setText(formattedDate)
        }

        val toDatePicker = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val myFormat = "dd/MM/yyyy"
            val sdf = SimpleDateFormat(myFormat, Locale.UK)
            val formattedDate = sdf.format(myCalendar.time)
            Log.d("dateABC", formattedDate)
            toDatePickerText.setText(formattedDate)
        }

        datePickerText.setOnClickListener {
            DatePickerDialog(
                this,
                R.style.MyDatePickerDialogStyle, datePicker, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        toDatePickerText.setOnClickListener {
            DatePickerDialog(
                this,
                R.style.MyDatePickerDialogStyle, toDatePicker, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Setup for filter button
        botDialog.findViewById<MaterialButton>(R.id.saveBtn)?.setOnClickListener {
            botDialog.dismiss()
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
            val selectedCategory = categoryAutoComplete.text.toString()
            val selectedPriceRange = priceAutoComplete.text.toString()
            val selectedDate = datePickerText.text.toString()
            val selectedToDate = toDatePickerText.text.toString()

//            val allProducts = getListProducts()

            val db = AppDatabase.getInstance(this@ManageProductActivity)
            val allProducts = FirebaseService.getListProducts(db)

            var filteredProducts = allProducts

            // Chỉ áp dụng bộ lọc nếu giá trị không phải là "Choose ..."
            if (nameFilter.isNotEmpty()) {
                filteredProducts = filterProductsByName(nameFilter, filteredProducts)
            }
            if (selectedCategory != "Choose category") {
                filteredProducts = filterProductsByCategory(selectedCategory, filteredProducts)
            }
            if (selectedPriceRange != "Choose range of price") {
                filteredProducts = filterProductsByPriceRange(selectedPriceRange, filteredProducts)
            }
            if (selectedDate != "Date") {
                filteredProducts = filterProductsByDate(selectedDate, selectedToDate, filteredProducts)
            }

            displayFilteredProducts(filteredProducts)
        }
    }


    private fun filterProductsByName(nameFilter: String, products: List<Product>): List<Product> {
        return products.filter { it.name.contains(nameFilter, ignoreCase = true) }
    }

    private suspend fun filterProductsByCategory(selectedCategory: String, products: List<Product>): List<Product> {
        val db = Firebase.firestore
        return try {
            val categoriesSnapshot = db.collection("categories").whereEqualTo("name", selectedCategory).limit(1).get().await()
            if (categoriesSnapshot.documents.isNotEmpty()) {
                val categoryDocumentRef = categoriesSnapshot.documents.first().reference
                products.filter { product ->
                    // Giả sử `idCategory` là một tham chiếu đến document, chúng ta cần so sánh path của chúng
                    product.idCategory?.path == categoryDocumentRef.path
                }
            } else {
                // Xử lý trường hợp không tìm thấy danh mục phù hợp
                // Ở đây, chúng ta trả về danh sách rỗng hoặc giữ nguyên danh sách sản phẩm tùy thuộc vào yêu cầu
                products
            }
        } catch (e: Exception) {
            // Xử lý lỗi khi tìm kiếm danh mục
            products // Trả về danh sách sản phẩm ban đầu nếu có lỗi
        }
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

//    private fun filterProductsByDate(selectedDate: String, products: List<Product>): List<Product> {
//        // Parse selectedDate and filter products based on this date
//        val sdf = SimpleDateFormat("dd/MM/yy", Locale.US)
//
//        return try {
//            val dateTimeFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
//            products.filter {
//                dateTimeFormat.format(it.date) == selectedDate
//            }
//        } catch (e: ParseException) {
//            products // Trả về tất cả sản phẩm nếu có lỗi khi parse
//        }
//    }

    private fun filterProductsByDate(startDateStr: String, endDateStr: String, products: List<Product>): List<Product> {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)

        // Chuyển đổi chuỗi ngày bắt đầu và kết thúc sang kiểu Date
        var startDate: Date? = try { sdf.parse(startDateStr) } catch (e: ParseException) { null }
        var endDate: Date? = try { sdf.parse(endDateStr) } catch (e: ParseException) { null }

        // Tăng endDate lên 1 ngày
        val calendar = Calendar.getInstance()
        if (endDate != null) {
            calendar.time = endDate
            calendar.add(Calendar.DAY_OF_MONTH, 1) // Tăng lên 1 ngày
            endDate = calendar.time
        }

        // Lọc offers dựa trên khoảng ngày, bao gồm cả endDate + 1
        return products.filter {
            val productDate = it.date
            productDate != null && startDate != null && endDate != null &&
                    !productDate.before(startDate) && productDate.before(endDate)
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

//    private fun displayFilteredProducts(filteredProducts: List<Product>) {
//        showShimmerEffectForProducts()
////            val db = AppDatabase.getInstance(applicationContext)
//        val adapter = ManageProductAdapter(filteredProducts, onProductClick = { product ->
//            val intent = Intent(this@ManageProductActivity, EditProduct::class.java).apply {
//                putExtra("PRODUCT_ID", product.id)
//            }
//            startActivity(intent)
//        })
//        binding.productRcv.layoutManager = LinearLayoutManager(this@ManageProductActivity)
//        binding.productRcv.adapter = adapter
//        hideShimmerEffectForProducts()
//    }




    private suspend fun getListProducts(): List<Product> {
        return withContext(Dispatchers.IO) {
            try {
                val querySnapshot = fireStore.collection("products").orderBy("date", Query.Direction.DESCENDING).get().await()
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

                    Product(id, idCategoryRef, idOfferRef, name, imageUrl, price, description, stock, sold, document.getDate("date"))
                }
            } catch (e: Exception) {
                Log.e("getListProducts", "Error getting products", e)
                emptyList()
            }
        }
    }

}
