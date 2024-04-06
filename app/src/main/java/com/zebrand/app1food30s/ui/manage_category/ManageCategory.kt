package com.zebrand.app1food30s.ui.manage_category

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.ManageCategoryAdapter
import com.zebrand.app1food30s.adapter.ManageOfferAdapter
import com.zebrand.app1food30s.adapter.ManageProductAdapter
import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.data.entity.Offer
import com.zebrand.app1food30s.data.entity.Category
import com.zebrand.app1food30s.data.entity.Product
import com.zebrand.app1food30s.databinding.ActivityManageCategoryBinding
import com.zebrand.app1food30s.ui.edit_category.EditCategory
import com.zebrand.app1food30s.ui.edit_offer.EditOffer
import com.zebrand.app1food30s.ui.edit_product.EditProduct
import com.zebrand.app1food30s.ui.manage_product.ManageProductDetailActivity
import com.zebrand.app1food30s.utils.FirebaseService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ManageCategory : AppCompatActivity() {
    private lateinit var binding: ActivityManageCategoryBinding
    private lateinit var rcv: RecyclerView
    private val fireStore = FirebaseFirestore.getInstance()
    private val fireStorage = FirebaseStorage.getInstance()

    private lateinit var addButton: ImageView
    private lateinit var filterButton: ImageView
    private lateinit var botDialog: BottomSheetDialog
    private lateinit var nameFilterEditText: TextInputEditText
    private lateinit var numProductAutoComplete: AutoCompleteTextView
    private lateinit var datePickerText: TextInputEditText
    val numProductArray = arrayOf("0 to 1", "2 to 10", "11 to 50", "51 to 100", "More than 100")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handleDisplayCategoryList()

        addButton = findViewById(R.id.add_product_btn)
        filterButton = findViewById(R.id.filter_btn)

        addButton.setOnClickListener {
            val intent = Intent(this, ManageCategoryDetail::class.java)
            startActivity(intent)
        }

        filterButton.setOnClickListener {
//            categoryArr = resources.getStringArray(R.array.delivery_array)
            showFilterCategory()
        }
    }

    private fun showFilterCategory() {
        val dialogView = layoutInflater.inflate(R.layout.pop_up_filter_manage_category, null)
        botDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        botDialog.setContentView(dialogView)

        nameFilterEditText = dialogView.findViewById(R.id.nameFilter)
        numProductAutoComplete = dialogView.findViewById(R.id.autoCompleteNumProduct)
        datePickerText = dialogView.findViewById(R.id.datePicker)

        val adapterNumProduct = ArrayAdapter(this, R.layout.dropdown_menu_popup_item, numProductArray)
//        adapterPrice.setDropDownViewResource(R.layout.dropdown_menu_popup_item)
        numProductAutoComplete.setAdapter(adapterNumProduct)

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
            Log.d("dateABC", formattedDate)
            datePickerText.setText(formattedDate)
        }

        datePickerText.setOnClickListener {
            DatePickerDialog(
                this,
                R.style.MyDatePickerDialogStyle, datePicker, myCalendar.get(Calendar.YEAR), myCalendar.get(
                    Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Setup for filter button
        botDialog.findViewById<MaterialButton>(R.id.saveBtn)?.setOnClickListener {
            botDialog.dismiss()
            filterCategories()
        }

        // Setup for cancel button
        botDialog.findViewById<MaterialButton>(R.id.cancelBtn)?.setOnClickListener {
            botDialog.dismiss()
        }

        botDialog.show()
    }

    private fun filterCategories() {
        lifecycleScope.launch {
            val nameFilter = nameFilterEditText.text.toString().trim()
            val selectedNumProduct = numProductAutoComplete.text.toString()
            val selectedDate = datePickerText.text.toString()

            val allCategories = getListCategories()

//            val db = AppDatabase.getInstance(applicationContext)
//            val allCategories = FirebaseService.getListProducts(db)

            var filteredCategories = allCategories

            if (nameFilter.isNotEmpty()) {
                filteredCategories = filterCategoriesByName(nameFilter, filteredCategories)
            }
            if (selectedNumProduct != "Choose number of product") {
                filteredCategories = filterCategoriesByNumProduct(selectedNumProduct, filteredCategories)
            }
            if (selectedDate != "Choose date") {
                filteredCategories = filterCategoriesByDate(selectedDate, filteredCategories)
            }

            displayFilteredCategories(filteredCategories)
        }
    }

    private fun filterCategoriesByDate(selectedDate: String, categories: List<Category>): List<Category> {
        // Parse selectedDate and filter products based on this date
        val sdf = SimpleDateFormat("dd/MM/yy", Locale.US)

        return try {
            val dateTimeFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            categories.filter {
                dateTimeFormat.format(it.date) == selectedDate
            }
        } catch (e: ParseException) {
            categories // Trả về tất cả sản phẩm nếu có lỗi khi parse
        }
    }

    private fun filterCategoriesByName(nameFilter: String, categories: List<Category>): List<Category> {
        return categories.filter { it.name.contains(nameFilter, ignoreCase = true) }
    }

    private fun filterCategoriesByNumProduct(selectedNumProduct: String, categories: List<Category>): List<Category> {
        val range = selectedNumProduct.split(" to ").mapNotNull { it.filter { char -> char.isDigit() }.toIntOrNull() }
        if (range.size == 2) {
            return categories.filter {
                it.numProduct >= range[0] && it.numProduct <= range[1]
            }
        }
        return categories
    }

    private fun displayFilteredCategories(filteredCategories: List<Category>) {
        // Update RecyclerView with filteredCategories
        val adapter = ManageCategoryAdapter(filteredCategories, onCategoryClick = { category ->
//            val intent = Intent(this@ManageCategory, EditCategory::class.java).apply {
//                putExtra("CATEGORY_ID", category.id)
//            }
//            startActivity(intent)
        })
        binding.productRcv.layoutManager = LinearLayoutManager(this@ManageCategory)
        binding.productRcv.adapter = adapter
    }


    private fun handleDisplayCategoryList() {
        lifecycleScope.launch {
            showShimmerEffectForProducts()
//            val db = AppDatabase.getInstance(applicationContext)
            val adapter = ManageCategoryAdapter(getListCategories(), onCategoryClick = { category ->
                val intent = Intent(this@ManageCategory, EditCategory::class.java).apply {
                    putExtra("CATEGORY_ID", category.id)
                }
                startActivity(intent)
            })
            binding.productRcv.layoutManager = LinearLayoutManager(this@ManageCategory)
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

    private suspend fun getListCategories(): List<Category> {
        return withContext(Dispatchers.IO) {
            try {
                val querySnapshot = fireStore.collection("categories").get().await()
                querySnapshot.documents.map { document ->
                    val id = document.id
                    val name = document.getString("name") ?: ""
                    val image = document.getString("image") ?: ""
                    val imageUrl = fireStorage.reference.child(image).downloadUrl.await().toString()
                    val numProduct = document.getDouble("numProduct") ?: 0
                    val date = document.getDate("date")

                    Category(
                        id,
                        name,
                        imageUrl,
                        numProduct.toInt(),
                        date
                    )
                }
            } catch (e: Exception) {
                Log.e("getListCategories", "Error getting products", e)
                emptyList()
            }
        }
        return listOf()
    }
}
