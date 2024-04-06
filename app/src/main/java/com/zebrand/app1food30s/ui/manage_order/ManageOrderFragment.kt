package com.zebrand.app1food30s.ui.manage_order

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.ManageOrderAdapter
import com.zebrand.app1food30s.data.entity.Order
import com.zebrand.app1food30s.databinding.FragmentManageOrderBinding
import com.zebrand.app1food30s.ui.manage_order.manage_order_details.ManageOrderDetailsActivity
import com.zebrand.app1food30s.utils.GlobalUtils
import com.zebrand.app1food30s.utils.MySharedPreferences
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ManageOrderFragment : Fragment(), ManageOrderMVPView{
    private var _binding: FragmentManageOrderBinding? = null
    private val binding get() = _binding!!
    private lateinit var presenter: ManageOrderPresenter
    private lateinit var preferences: MySharedPreferences
    private lateinit var botDialog: BottomSheetDialog
    lateinit var statusArr: Array<String>
    val customerArr = arrayOf("1- Bedroom", "2- Bedroom", "3- Bedroom")

    private lateinit var manageOrderAdapter: ManageOrderAdapter
    private var manageOrderList: MutableList<Order> = mutableListOf()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        preferences = MySharedPreferences.getInstance(context)
        presenter = ManageOrderPresenter(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentManageOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        events()

        lifecycleScope.launch {
            getManageOrders()
        }
    }

    private fun init(){
        statusArr = resources.getStringArray(R.array.delivery_array)
    }

    private fun events() {
//        binding.root.findViewById<View>(R.id.testOrderStatus).setOnClickListener {
//            val intent = Intent(requireContext(), ManageOrderDetailsActivity::class.java)
//            startActivity(intent)
//        }

        binding.root.findViewById<View>(R.id.filterBtn).setOnClickListener {
            showBottomSheet()
        }
    }



    private fun showBottomSheet() {
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_manage_order, null)
        botDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        botDialog.setContentView(dialogView)

        // status dropdown
        val adapterStatus = ArrayAdapter(requireContext(), R.layout.item_drop_down_filter, statusArr)
        val tv_autoStatus: AutoCompleteTextView = dialogView.findViewById(R.id.spinnerStatus)
        tv_autoStatus.setAdapter(adapterStatus)
        tv_autoStatus.setOnItemClickListener { _, _, position, _ ->
            val selectedText = adapterStatus.getItem(position)
//            Toast.makeText(this, selectedText, Toast.LENGTH_LONG).show()
        }

        // customer dropdown
        val adapterCus = ArrayAdapter(requireContext(), R.layout.item_drop_down_filter, customerArr)
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
                requireContext(),
                R.style.MyDatePickerDialogStyle, datePicker, myCalendar.get(Calendar.YEAR), myCalendar.get(
                    Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        botDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override suspend fun getManageOrders() {
        manageOrderList = presenter.getManageOrders()
        manageOrderAdapter = ManageOrderAdapter(manageOrderList)
        manageOrderAdapter.onItemClick = {
            GlobalUtils.myStartActivityWithString(requireContext(), ManageOrderDetailsActivity::class.java, "idOrder", it.id)
        }

        // Set layout manager và adapter cho RecyclerView
        binding.rcvManageOrder.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        binding.rcvManageOrder.adapter = manageOrderAdapter

//        //getData
//        presenter.getPrevOrderList(userId, myPrevOrderAdapter)
    }
}