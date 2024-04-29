package com.zebrand.app1food30s.ui.my_order

import android.content.Context
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zebrand.app1food30s.adapter.MyOrderAdapter
import com.zebrand.app1food30s.data.entity.Order
import com.zebrand.app1food30s.databinding.FragmentMyPrevOrderBinding
import com.zebrand.app1food30s.ui.my_order.my_order_details.MyOrderDetailsActivity
import com.zebrand.app1food30s.utils.GlobalUtils
import com.zebrand.app1food30s.utils.MySharedPreferences
import com.zebrand.app1food30s.utils.SingletonKey
import com.zebrand.app1food30s.utils.Utils
import kotlinx.coroutines.launch

class MyPrevOrderFragment : Fragment(), MyOrderMVPView {
    lateinit var binding: FragmentMyPrevOrderBinding
    //    Chưa login nên không có đi qua local db để lấy data được
    private lateinit var mySharePreference: MySharedPreferences
    private lateinit var presenter: MyOrderPresenter
    private lateinit var myPrevOrderAdapter: MyOrderAdapter
    private var myPrevOrderList: MutableList<Order> = mutableListOf()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mySharePreference = MySharedPreferences.getInstance(context)
        presenter = MyOrderPresenter(context, this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMyPrevOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        events()

        lifecycleScope.launch {
            getPrevMyOrderList()
        }
    }

    private fun init(){

    }

    private fun events() {

    }



//    private fun showBottomSheet() {
//        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_manage_order, null)
//        botDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
//        botDialog.setContentView(dialogView)
//
//        // status dropdown
//        val adapterStatus = ArrayAdapter(requireContext(), R.layout.item_drop_down_filter, statusArr)
//        val tv_autoStatus: AutoCompleteTextView = dialogView.findViewById(R.id.spinnerStatus)
//        tv_autoStatus.setAdapter(adapterStatus)
//        tv_autoStatus.setOnItemClickListener { _, _, position, _ ->
//            val selectedText = adapterStatus.getItem(position)
////            Toast.makeText(this, selectedText, Toast.LENGTH_LONG).show()
//        }
//
//        // customer dropdown
//        val adapterCus = ArrayAdapter(requireContext(), R.layout.item_drop_down_filter, customerArr)
//        val tv_autoCus: AutoCompleteTextView = dialogView.findViewById(R.id.spinnerCustomer)
//        tv_autoCus.setAdapter(adapterCus)
//        tv_autoCus.setOnItemClickListener { _, _, position, _ ->
//            val selectedText = adapterCus.getItem(position)
////            Toast.makeText(this, selectedText, Toast.LENGTH_LONG).show()
//        }
//
//        // date picker
//        val datePickerText: TextInputEditText = dialogView.findViewById(R.id.datePicker)
//        val myCalendar = Calendar.getInstance()
//        val datePicker = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
//            myCalendar.set(Calendar.YEAR, year)
//            myCalendar.set(Calendar.MONTH, month)
//            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
//
//            val myFormat = "dd/MM/yyyy"
//            val sdf = SimpleDateFormat(myFormat, Locale.UK)
//            val formattedDate = sdf.format(myCalendar.time)
////            Log.d("dateABC", formattedDate)
//            datePickerText.setText(formattedDate)
//        }
//
//        datePickerText.setOnClickListener {
//            DatePickerDialog(
//                requireContext(),
//                R.style.MyDatePickerDialogStyle, datePicker, myCalendar.get(Calendar.YEAR), myCalendar.get(
//                    Calendar.MONTH),
//                myCalendar.get(Calendar.DAY_OF_MONTH)
//            ).show()
//        }
//
//        botDialog.show()
//    }

    private fun getPrevMyOrderList() {
        val userId = mySharePreference.getString(SingletonKey.KEY_USER_ID) as String
//        Log.d("Test00", "getMyOrderList: ${userId}")

//        val userId = "8U49yTcDk55UW2UJO69h"
        myPrevOrderAdapter = MyOrderAdapter(myPrevOrderList)
        myPrevOrderAdapter.onItemClick = {
            GlobalUtils.myStartActivityWithString(requireContext(), MyOrderDetailsActivity::class.java, "idOrder", it.id)
        }

        // Set layout manager và adapter cho RecyclerView
        binding.rcvActiveMyOrder.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        binding.rcvActiveMyOrder.adapter = myPrevOrderAdapter

        //getData
        presenter.getPrevOrderList(userId, myPrevOrderAdapter)
    }

//    Miss this function
    override fun setMyActiveOrderUI() {}

    override fun setMyPrevOrderUI() {
        Handler().postDelayed({
            if (myPrevOrderAdapter.itemCount == 0) {
                binding.orderItemList.visibility = View.GONE
                binding.noItemLayout.visibility = View.VISIBLE
            } else {
                binding.orderItemList.visibility = View.VISIBLE
                binding.noItemLayout.visibility = View.GONE
            }
        }, Utils.timeHandler + 200)
    }

    override fun showShimmerEffectForOrders(size: Int) {
        for (i in 0 until size) {
            val shimmerLayout = layoutInflater.inflate(R.layout.product_card_view_linear_shimmer, binding.linearShimmer, false)
            // Add the inflated layout to the parent LinearLayout
            binding.linearShimmer.addView(shimmerLayout)
        }
        Utils.showShimmerEffect(binding.orderShimmer, binding.orderItemList)
    }

    override fun hideShimmerEffectForOrders() {
        Utils.hideShimmerEffect(binding.orderShimmer, binding.orderItemList)
    }
}