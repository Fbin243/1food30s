package com.zebrand.app1food30s.ui.manage_order.manage_order_details

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.MyOrderDetailsAdapter
import com.zebrand.app1food30s.data.entity.Order
import com.zebrand.app1food30s.data.entity.OrderItem
import com.zebrand.app1food30s.databinding.ActivityManageOrderDetailsBinding
import com.zebrand.app1food30s.utils.Utils

class ManageOrderDetailsActivity : AppCompatActivity(), ManageOrderDetailsMVPView {
    lateinit var binding: ActivityManageOrderDetailsBinding
    private lateinit var presenter: ManageOrderDetailsPresenter
    private lateinit var paymentArr: Array<String>
    private lateinit var idOrder: String
    //    val deliveryArr = resources.getStringArray(R.array.delivery_array)
    private lateinit var deliveryArr: Array<String>
    var orderDetails: Order = Order()
    private lateinit var itemOrderDetailsAdapter: MyOrderDetailsAdapter
    private var manageOrderDetailsList: MutableList<OrderItem> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageOrderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        events()
        handleDropDown()

        getManageOrderDetails()
    }

    private fun init() {
        presenter = ManageOrderDetailsPresenter(this, this)
        paymentArr = resources.getStringArray(R.array.payment_array)
        deliveryArr = resources.getStringArray(R.array.delivery_array)

        idOrder = intent.getStringExtra("idOrder").toString()
    }

    private fun events() {
        binding.backIcon.root.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.rejectBtn.setOnClickListener {
            showCustomRejectDialogBox()
        }
    }

    private fun handleDropDown() {
        // status dropdown
        val adapterStatus = ArrayAdapter(this, R.layout.item_drop_down_filter, paymentArr)
        binding.spinnerPayment.setAdapter(adapterStatus)
        binding.spinnerPayment.setOnItemClickListener { _, _, position, _ ->
            val selectedText = adapterStatus.getItem(position)
            presenter.changePaymentStatus(idOrder, selectedText.toString())
//            Toast.makeText(this, selectedText, Toast.LENGTH_LONG).show()
        }

        // customer dropdown
        val adapterCus = ArrayAdapter(this, R.layout.item_drop_down_filter, deliveryArr)
        binding.spinnerDelivery.setAdapter(adapterCus)
        binding.spinnerDelivery.setOnItemClickListener { _, _, position, _ ->
            val selectedText = adapterCus.getItem(position)
            presenter.changeOrderStatus(idOrder, selectedText.toString())
//            Toast.makeText(this, selectedText, Toast.LENGTH_LONG).show()
        }
    }

    private fun showCustomRejectDialogBox() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_reason_order)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val acceptBtn: Button = dialog.findViewById(R.id.cancelBtn)
        val cancel: Button = dialog.findViewById(R.id.saveBtn)

        acceptBtn.setOnClickListener {
            dialog.dismiss()
        }

        cancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun getManageOrderDetails() {
        Log.i("TAG123", "getMyOrderDetailsList: ${orderDetails.orderStatus}")
        itemOrderDetailsAdapter = MyOrderDetailsAdapter(this, manageOrderDetailsList)
        itemOrderDetailsAdapter.onItemClick = {
//            Chuyển qua product details
//            GlobalUtils.myStartActivityWithString(this, MyOrderDetailsActivity::class.java, "idOrder", it.id)
        }

        // Set layout manager và adapter cho RecyclerView
        binding.rcvManageOrderDetails.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.rcvManageOrderDetails.adapter = itemOrderDetailsAdapter

        //getData
        presenter.getManageOrderDetails(idOrder, orderDetails ,itemOrderDetailsAdapter)
    }

    @SuppressLint("SetTextI18n")
    override fun setManageOrderDetailsUI() {
        binding.tvOrderId.text = Utils.formatId(orderDetails.id)
        binding.tvCustomerName.text = orderDetails.user.firstName + " " + orderDetails.user.lastName
        binding.tvOrderDate.text = Utils.formatDate(orderDetails.date)
        binding.tvAddress.text = orderDetails.shippingAddress
//        binding.tvPaymentStatus.text = orderDetails.paymentStatus.uppercase()
        binding.tvSubTotal.text = "$" + Utils.formatPrice(itemOrderDetailsAdapter.getSubTotal())
        binding.tvDiscount.text = "$" + Utils.formatPrice(itemOrderDetailsAdapter.getDiscount())
        binding.tvTotalAmount.text = "$" + Utils.formatPrice(orderDetails.totalAmount)

        binding.spinnerPayment.setText(orderDetails.paymentStatus, false)
        binding.spinnerDelivery.setText(orderDetails.orderStatus, false)
        
    }

}