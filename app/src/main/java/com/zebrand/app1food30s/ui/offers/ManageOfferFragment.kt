package com.zebrand.app1food30s.ui.offers

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.ManageOfferAdapter
import com.zebrand.app1food30s.adapter.ManageProductAdapter
import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.data.entity.Category
import com.zebrand.app1food30s.data.entity.Offer
import com.zebrand.app1food30s.data.entity.Product
import com.zebrand.app1food30s.databinding.ActivityManageOfferBinding
import com.zebrand.app1food30s.databinding.FragmentManageOfferBinding
import com.zebrand.app1food30s.ui.edit_offer.EditOffer
import com.zebrand.app1food30s.ui.edit_product.EditProduct
import com.zebrand.app1food30s.ui.main.MainActivity
import com.zebrand.app1food30s.ui.manage_product.ManageProductDetailActivity
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

class ManageOfferFragment : Fragment() {
    private var _binding: FragmentManageOfferBinding? = null
    private val binding get() = _binding!!
    private lateinit var rcv: RecyclerView
    private val fireStore = FirebaseFirestore.getInstance()
    private val fireStorage = FirebaseStorage.getInstance()

    private lateinit var addButton: ImageView
    private lateinit var filterButton: ImageView
    private lateinit var botDialog: BottomSheetDialog
    private lateinit var nameFilterEditText: TextInputEditText
    private lateinit var discountRateAutoComplete: AutoCompleteTextView
    private lateinit var numProductAutoComplete: AutoCompleteTextView
    private lateinit var datePickerText: TextInputEditText
    private lateinit var toDatePickerText: TextInputEditText
    //    private lateinit var toDatePickerText: TextInputEditText
    val discountArray = arrayOf("1% to 10%", "11% to 50%", "More than 50%")
    val numProductArray = arrayOf("Empty", "1 to 10", "11 to 50", "51 to 100", "More than 100")


//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityManageOfferBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//        handleDisplayOfferList()
//
//        addButton = findViewById(R.id.add_product_btn)
//        filterButton = findViewById(R.id.filter_btn)
//
//        val backIcon = findViewById<ImageView>(R.id.imageView)
//        backIcon.setOnClickListener {
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//        }
//
//        addButton.setOnClickListener {
//            val intent = Intent(this, ManageOfferDetail::class.java)
//            startActivity(intent)
//        }
//
//        filterButton.setOnClickListener {
////            categoryArr = resources.getStringArray(R.array.delivery_array)
//            showFilterOffer()
//        }
//    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentManageOfferBinding.inflate(inflater, container, false)
        setupUI()
        return binding.root
    }

    private fun setupUI() {

        handleDisplayOfferList()

        binding.addProductBtn.setOnClickListener {
            startActivity(Intent(requireContext(), ManageOfferDetail::class.java))
        }

        binding.filterBtn.setOnClickListener {
            showFilterOffer()
        }

        // Initialize RecyclerView and other UI components here
    }

    private fun showFilterOffer() {
        val dialogView = layoutInflater.inflate(R.layout.pop_up_filter_manage_offer, null)
        botDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        botDialog.setContentView(dialogView)

        nameFilterEditText = dialogView.findViewById(R.id.nameFilter)
        discountRateAutoComplete = dialogView.findViewById(R.id.autoCompleteDiscountRate)
        numProductAutoComplete = dialogView.findViewById(R.id.autoCompleteNumProduct)
        datePickerText = dialogView.findViewById(R.id.datePicker)
        toDatePickerText = dialogView.findViewById(R.id.toDatePicker)

        val adapterDiscount = ArrayAdapter(requireContext(), R.layout.dropdown_menu_popup_item, discountArray)
//        adapterPrice.setDropDownViewResource(R.layout.dropdown_menu_popup_item)
        discountRateAutoComplete.setAdapter(adapterDiscount)

        val adapterNumProduct = ArrayAdapter(requireContext(), R.layout.dropdown_menu_popup_item, numProductArray)
//        adapterPrice.setDropDownViewResource(R.layout.dropdown_menu_popup_item)
        numProductAutoComplete.setAdapter(adapterNumProduct)

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
                requireContext(),
                R.style.MyDatePickerDialogStyle, datePicker, myCalendar.get(Calendar.YEAR), myCalendar.get(
                    Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        toDatePickerText.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                R.style.MyDatePickerDialogStyle, toDatePicker, myCalendar.get(Calendar.YEAR), myCalendar.get(
                    Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Setup for filter button
        botDialog.findViewById<MaterialButton>(R.id.saveBtn)?.setOnClickListener {
            botDialog.dismiss()
            filterOffers()
        }

        // Setup for cancel button
        botDialog.findViewById<MaterialButton>(R.id.cancelBtn)?.setOnClickListener {
            botDialog.dismiss()
        }

        botDialog.show()
    }

    private fun filterOffers() {
        lifecycleScope.launch {
            val nameFilter = nameFilterEditText.text.toString().trim()
            val selectedDiscount = discountRateAutoComplete.text.toString()
            val selectedNumProduct = numProductAutoComplete.text.toString()
            val selectedDate = datePickerText.text.toString()
            val selectedToDate = toDatePickerText.text.toString()

//            val allOffers = getListOffers()

            val db = AppDatabase.getInstance(requireContext())
            val allOffers = FirebaseService.getListOffers(db)

            var filteredOffers = allOffers

            if (nameFilter.isNotEmpty()) {
                filteredOffers = filterOffersByName(nameFilter, filteredOffers)
            }
            if (selectedDiscount != "Choose discount rate") {
                filteredOffers = filterOffersByDiscount(selectedDiscount, filteredOffers)
            }
            if (selectedNumProduct != "Choose number of product") {
                filteredOffers = filterOffersByNumProduct(selectedNumProduct, filteredOffers)
            }
            if (selectedDate != "Date") {
                filteredOffers = filterOffersByDate(selectedDate, selectedToDate, filteredOffers)
            }

            displayFilteredOffers(filteredOffers)
        }
    }

//    private fun filterOffersByDate(selectedDate: String, offers: List<Offer>): List<Offer> {
//        // Parse selectedDate and filter products based on this date
//        val sdf = SimpleDateFormat("dd/MM/yy", Locale.US)
//
//        return try {
//            val dateTimeFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
//            offers.filter {
//                dateTimeFormat.format(it.date) == selectedDate
//            }
//        } catch (e: ParseException) {
//            offers // Trả về tất cả sản phẩm nếu có lỗi khi parse
//        }
//    }


    private fun filterOffersByDate(startDateStr: String, endDateStr: String, offers: List<Offer>): List<Offer> {
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
        return offers.filter {
            val offerDate = it.date
            offerDate != null && startDate != null && endDate != null &&
                    !offerDate.before(startDate) && offerDate.before(endDate)
        }
    }


    private fun filterOffersByName(nameFilter: String, offers: List<Offer>): List<Offer> {
        return offers.filter { it.name.contains(nameFilter, ignoreCase = true) }
    }

    private fun filterOffersByDiscount(selectedDiscount: String, offers: List<Offer>): List<Offer> {
        val range = selectedDiscount.split(" to ").mapNotNull { it.filter { char -> char.isDigit() }.toIntOrNull() }
        if (range.size == 2) {
            return offers.filter {
                it.discountRate >= range[0] && it.discountRate <= range[1]
            }
        }
        return offers
    }

    private fun filterOffersByNumProduct(selectedNumProduct: String, offers: List<Offer>): List<Offer> {
        return when (selectedNumProduct) {
            "Empty" -> offers.filter { it.numProduct == 0 }
            "More than 100" -> offers.filter { it.numProduct > 100 }
            else -> {
                val range = selectedNumProduct.split(" to ").mapNotNull { it.toIntOrNull() }
                if (range.size == 2) {
                    offers.filter { it.numProduct >= range[0] && it.numProduct <= range[1] }
                } else offers
            }
        }
    }

    private fun displayFilteredOffers(filteredOffers: List<Offer>) {
        // Update RecyclerView with filteredOffers
        val adapter = ManageOfferAdapter(filteredOffers, onOfferClick = { offer ->
            val intent = Intent(requireContext(), EditOffer::class.java).apply {
                putExtra("OFFER_ID", offer.id)
            }
            startActivity(intent)
        })
        binding.productRcv.layoutManager = LinearLayoutManager(requireContext())
        binding.productRcv.adapter = adapter
    }


    private fun handleDisplayOfferList() {
        lifecycleScope.launch {
            showShimmerEffectForProducts()
//            val db = AppDatabase.getInstance(applicationContext)
            val adapter = ManageOfferAdapter(getListOffers(), onOfferClick = { offer ->
                val intent = Intent(requireContext(), EditOffer::class.java).apply {
                    putExtra("OFFER_ID", offer.id)
                }
                startActivity(intent)
            })
            binding.productRcv.layoutManager = LinearLayoutManager(requireContext())
            binding.productRcv.adapter = adapter
            hideShimmerEffectForProducts()
        }
    }

    override fun onResume() {
        super.onResume()
        handleDisplayOfferList()
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

    private suspend fun getListOffers(): List<Offer> {
        return withContext(Dispatchers.IO) {
            try {
                val querySnapshot = fireStore.collection("offers").orderBy("date", Query.Direction.DESCENDING).get().await()
                querySnapshot.documents.map { document ->
                    val id = document.id
                    val name = document.getString("name") ?: ""
                    val image = document.getString("image") ?: ""
                    val imageUrl = fireStorage.reference.child(image).downloadUrl.await().toString()
                    val discountRate = document.getDouble("discountRate") ?: 0.0
                    val numProduct = document.getDouble("numProduct") ?: 0
                    val date = document.getDate("date")

                    Offer(
                        id,
                        name,
                        imageUrl,
                        discountRate.toInt(),
                        numProduct.toInt(),
                        date
                    )
                }
            } catch (e: Exception) {
                Log.e("getListOffers", "Error getting products", e)
                emptyList()
            }
        }
        return listOf()
    }
}
