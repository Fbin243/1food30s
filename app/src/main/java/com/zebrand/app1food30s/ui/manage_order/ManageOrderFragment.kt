package com.zebrand.app1food30s.ui.manage_order

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.ManageChatAdapter
import com.zebrand.app1food30s.adapter.ManageOrderAdapter
import com.zebrand.app1food30s.data.entity.Chat
import com.zebrand.app1food30s.data.entity.Order
import com.zebrand.app1food30s.databinding.FragmentManageOrderBinding
import com.zebrand.app1food30s.ui.chat.ChatManager
import com.zebrand.app1food30s.ui.manage_order.manage_order_details.ManageOrderDetailsActivity
import com.zebrand.app1food30s.utils.GlobalUtils
import com.zebrand.app1food30s.utils.MySharedPreferences
import com.zebrand.app1food30s.utils.Utils
import org.w3c.dom.Text
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.reflect.typeOf

class ManageOrderFragment : Fragment(), ManageOrderMVPView, SwipeRefreshLayout.OnRefreshListener{
    private var _binding: FragmentManageOrderBinding? = null
    private val binding get() = _binding!!
    private lateinit var presenter: ManageOrderPresenter
    private lateinit var preferences: MySharedPreferences
    private var botDialog: BottomSheetDialog? = null
    private var statusArr = mutableListOf<String>()
    private var customerArr = mutableSetOf<String>()

    private lateinit var manageOrderAdapter: ManageOrderAdapter
    private var manageOrderList: MutableList<Order> = mutableListOf()
//    Status
    private lateinit var filterStatus: String
//    Customer
    private lateinit var filterCustomer: String
//    Date
    private var startDate: String = ""
    private var endDate: String = ""
    private var selectedBool: Boolean = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        preferences = MySharedPreferences.getInstance(context)
        presenter = ManageOrderPresenter(context, this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentManageOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        events()

        getManageOrders()
//        lifecycleScope.launch {}
    }

    private fun init(){
        statusArr.addAll(resources.getStringArray(R.array.delivery_array_v3))
        filterStatus = resources.getString(R.string.txt_status)
        filterCustomer = resources.getString(R.string.txt_customer)
        // Make function reloading data when swipe down
        Utils.initSwipeRefreshLayout(binding.swipeRefreshLayout, this, resources)
    }

    private fun events() {
        binding.root.findViewById<View>(R.id.filterBtn).setOnClickListener {
            botDialog?.show()
        }
        binding.root.findViewById<View>(R.id.ivChatScreen).setOnClickListener {
            val intent = Intent(requireContext(), ChatManager::class.java)
            startActivity(intent)
        }
        setupChatListener()
    }



    @SuppressLint("SetTextI18n", "CutPasteId")
    private fun setupFilterBottomSheet() {
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_manage_order, null)
        botDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        botDialog?.setContentView(dialogView)

        // status dropdown
        val adapterStatus = ArrayAdapter(requireContext(), R.layout.item_drop_down_filter, statusArr)
        val tv_autoStatus: AutoCompleteTextView = dialogView.findViewById(R.id.spinnerStatus)
        tv_autoStatus.setAdapter(adapterStatus)
        tv_autoStatus.setOnItemClickListener { _, _, position, _ ->
            val selectedText = adapterStatus.getItem(position)
            filterStatus = selectedText.toString()
//            Toast.makeText(requireContext(), selectedText, Toast.LENGTH_LONG).show()
        }

        val customerArray = customerArr.filter {
            true
        }.toMutableList()
        // customer dropdown
        val adapterCus = ArrayAdapter(requireContext(), R.layout.item_drop_down_filter, customerArray)
        val tv_autoCus: AutoCompleteTextView = dialogView.findViewById(R.id.spinnerCustomer)
        tv_autoCus.setAdapter(adapterCus)
        tv_autoCus.setOnItemClickListener { _, _, position, _ ->
            val selectedText = adapterCus.getItem(position)
            filterCustomer = selectedText.toString()
//            Toast.makeText(requireContext(), selectedText, Toast.LENGTH_LONG).show()
        }

        // date picker
        val datePickerText: TextInputEditText = dialogView.findViewById(R.id.datePicker)

        datePickerText.setOnClickListener {
            val dialog = Dialog(requireContext())
            val dialogDatePickerView = layoutInflater.inflate(R.layout.item_date_range, null)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setContentView(dialogDatePickerView)

            val calendarView: MaterialCalendarView = dialogDatePickerView.findViewById(R.id.calendarView)

            val confirmBtn: Button = dialogDatePickerView.findViewById(R.id.confirmBtn)
            val cancelBtn: Button = dialogDatePickerView.findViewById(R.id.cancelBtn)

            calendarView.setOnRangeSelectedListener{ widget, dates ->
                startDate = Utils.formatCalendarView(dates[0])
                endDate = Utils.formatCalendarView(dates[dates.size - 1])
                Log.d("CalendarView", dates[0].toString() +  " " + dates[dates.size - 1].toString())
                Log.d("CalendarView", startDate +  " " + endDate + " " + selectedBool)
            }
            calendarView.setOnDateChangedListener { widget, date, selected ->
                selectedBool = selected
                startDate = Utils.formatCalendarView(date)
                endDate = ""
                Log.d("CalendarView", startDate +  " " + endDate + " " + selectedBool)
            }

            confirmBtn.setOnClickListener {
                if(selectedBool){
                    if(endDate != ""){
                        datePickerText.setText("$startDate - $endDate")
                    }else{
                        datePickerText.setText(startDate)
                    }
                }
                dialog.dismiss()
            }

            cancelBtn.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }

        // clear and search
        val searchBtn: Button = dialogView.findViewById(R.id.saveBtn)
        val clearBtn: Button = dialogView.findViewById(R.id.cancelBtn)

        searchBtn.setOnClickListener {
//            manageOrderAdapter.filterOrders()
            searchOrders()
            botDialog?.dismiss()
        }

        clearBtn.setOnClickListener {
            dialogView.findViewById<TextInputEditText>(R.id.orderID).setText("")
            filterStatus = resources.getString(R.string.txt_status)
            filterCustomer = resources.getString(R.string.txt_customer)
            dialogView.findViewById<AutoCompleteTextView>(R.id.spinnerStatus).setText("")
            dialogView.findViewById<AutoCompleteTextView>(R.id.spinnerCustomer).setText("")
            dialogView.findViewById<TextInputEditText>(R.id.datePicker).setText(resources.getString(R.string.txt_default_date))

            presenter.getManageOrders(manageOrderAdapter, manageOrderList, customerArr)

            botDialog?.dismiss()
        }

//        botDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupChatListener() {
        val db = Firebase.firestore
        db.collection("chats")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("ChatManagerActivity", "Listen failed.", e)
                    return@addSnapshotListener
                }

                var anyUnseen = false // Biến kiểm tra xem có tin nhắn chưa đọc không

                snapshots?.documents?.forEach { doc ->
                    doc.toObject(Chat::class.java)?.let {
                        if (!it.seen && it.messages.isNotEmpty()) {
                            anyUnseen = true // Đặt anyUnseen thành true nếu tìm thấy tin nhắn chưa đọc
                        }
                    }
                }

                // Cập nhật icon một lần dựa trên biến anyUnseen
                if (anyUnseen) {
                    binding.ivChatScreen.setImageResource(R.drawable.chat_round_unread_svgrepo_com)
                } else {
                    binding.ivChatScreen.setImageResource(R.drawable.ic_chat)
                }
            }
    }


    override fun getManageOrders() {
        manageOrderAdapter = ManageOrderAdapter(mutableListOf())
        manageOrderAdapter.onItemClick = {
            GlobalUtils.myStartActivityWithString(requireContext(), ManageOrderDetailsActivity::class.java, "idOrder", it.id)
        }

        // Set layout manager và adapter cho RecyclerView
        binding.rcvManageOrder.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        binding.rcvManageOrder.adapter = manageOrderAdapter

//        //getData
        presenter.getManageOrders(manageOrderAdapter, manageOrderList, customerArr)
    }

    override fun setManageOrderUI(size: Int) {
//        Log.d("Test01", "size123: ${manageOrderAdapter.itemCount}")
        if(size == 0){
            binding.rcvManageOrder.visibility = View.GONE
            binding.noItemLayout.visibility = View.VISIBLE
        }else{
            binding.rcvManageOrder.visibility = View.VISIBLE
            binding.noItemLayout.visibility = View.GONE
        }
//        Setup onFilterOrders
//        Log.d("ManageOrderPresenter", "User: ${customerArr.toString()}")
        setupFilterBottomSheet()
    }

    private fun searchOrders(){
        val orderID = botDialog?.findViewById<TextInputEditText>(R.id.orderID)?.text.toString().trim()
        val dateText = botDialog?.findViewById<TextInputEditText>(R.id.datePicker)?.text.toString().trim()

        Log.d("filterOrders", manageOrderList.size.toString())
        val filterOrders = mutableListOf<Order>()

        for (order in manageOrderList) {
            var check = true

            if (orderID.trim().isNotEmpty()) {
                check = Utils.formatId(order.id).contains(orderID)
            }
            if (!check) continue

            if (filterStatus != resources.getString(R.string.txt_status)) {
                check = order.orderStatus == filterStatus
            }
            if (!check) continue

            if (filterCustomer != resources.getString(R.string.txt_customer)) {
                check = "${order.user.firstName} ${order.user.lastName}" == filterCustomer
                Log.d("filterOrders", "${order.user.firstName} ${order.user.lastName} $filterCustomer")
            }
            if (!check) continue

            if (selectedBool && dateText != resources.getString(R.string.txt_default_date)) {
                val sdf = SimpleDateFormat("hh:MM:ss dd/MM/yyyy", Locale.getDefault())
                val startDateParse = sdf.parse("00:00:00 $startDate")
                val endDatParse = sdf.parse("23:59:59 $endDate")

                check = order.date.after(startDateParse) && order.date.before(endDatParse)
            }

            if (check) {
                filterOrders.add(order)
            }
        }

//        Log.d("filterOrders", manageOrderList.toString() + " " + filterOrders.size.toString())
        manageOrderAdapter.setData(filterOrders)

        if(filterOrders.size == 0){
            binding.rcvManageOrder.visibility = View.GONE
            binding.noItemLayout.visibility = View.VISIBLE
        }else{
            binding.rcvManageOrder.visibility = View.VISIBLE
            binding.noItemLayout.visibility = View.GONE
        }
    }

    override fun showShimmerEffectForOrders(size: Int) {
//        Log.d("Test01", "size345: $size size 2 ${manageOrderList.size} ${manageOrderAdapter.itemCount}")
        for (i in 0 until size) {
            val shimmerLayout = layoutInflater.inflate(R.layout.item_manage_order_shimmer, binding.linearShimmer, false)
            // Add the inflated layout to the parent LinearLayout
            binding.linearShimmer.addView(shimmerLayout)
        }

        Utils.showShimmerEffect(binding.orderShimmer, binding.orderItemList)
    }

    override fun hideShimmerEffectForOrders() {
        Utils.hideShimmerEffect(binding.orderShimmer, binding.orderItemList)
    }

    override fun onRefresh() {
        presenter.getManageOrders(manageOrderAdapter, manageOrderList ,customerArr)
//        Log.d("manageOrderRefresh", "onRefresh")
        Utils.handler.postDelayed({
            binding.swipeRefreshLayout.isRefreshing = false
        }, 500)
    }
}

//        val filterOrders = manageOrderList.filter {order ->
//            Log.d("filterOrders", manageOrderList.size.toString() + "item")
//            var check = true
//            if(orderID.trim() != ""){
//                check = Utils.formatId(order.id).contains(orderID)
//            }
//            if(!check) return@filter false
//
//            if(filterStatus != resources.getString(R.string.txt_status)){
//                check = order.orderStatus == filterStatus
//            }
//            if(!check) return@filter false
//
//            if(filterCustomer != resources.getString(R.string.txt_customer)){
//                check = (order.user.firstName + " " + order.user.lastName) == filterCustomer
//                Log.d("filterOrders", order.user.firstName + " " + order.user.lastName + " " + filterCustomer)
//            }
//            if(!check) return@filter false
//
//            if(selectedBool && dateText != resources.getString(R.string.txt_default_date)){
////                Log.d("filterOrders", "da vo 1")
//                val sdf = SimpleDateFormat("hh:MM:ss dd/MM/yyyy", Locale.getDefault())
////                Log.d("filterOrders", startDate.toString() + " " + endDate.toString())
//
////                Log.d("filterOrders", "da vo 2")
//                val startDateParse = sdf.parse("00:00:00 $startDate")
//                val endDatParse = sdf.parse("23:59:59 $endDate")
//
//                check = order.date.after(startDateParse) && order.date.before(endDatParse)
//                if (startDateParse != null && endDatParse != null) {
////                    Log.d("filterOrders", startDateParse.toString() + " " + endDatParse.toString())
//                }
////                Log.d("filterOrders", "date")
//            }
//            return@filter check
//        }.toMutableList()