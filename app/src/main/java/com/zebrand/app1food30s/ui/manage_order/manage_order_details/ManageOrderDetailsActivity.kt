package com.zebrand.app1food30s.ui.manage_order.manage_order_details

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.databinding.ActivityManageOrderDetailsBinding
import com.zebrand.app1food30s.ui.my_order.MyOrderPresenter

class ManageOrderDetailsActivity : AppCompatActivity(), ManageOrderDetailsMVPView {
    lateinit var binding: ActivityManageOrderDetailsBinding
    private lateinit var presenter: ManageOrderDetailsPresenter
    lateinit var paymentArr: Array<String>
    private lateinit var idOrder: String
    //    val deliveryArr = resources.getStringArray(R.array.delivery_array)
    lateinit var deliveryArr: Array<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageOrderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        events()
        handleDropDown()
    }

    private fun init() {
        presenter = ManageOrderDetailsPresenter(this)
        paymentArr = resources.getStringArray(R.array.payment_array)
        deliveryArr = resources.getStringArray(R.array.delivery_array)

        idOrder = intent.getStringExtra("idOrder").toString()
    }

    private fun events() {
        binding.backIcon.root.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.rejectBtn.setOnClickListener {
            showCustomRejectDialgoBox()
        }
    }

    private fun handleDropDown() {
        // status dropdown
        val adapterStatus = ArrayAdapter(this, R.layout.item_drop_down_filter, paymentArr)
        binding.spinnerPayment.setAdapter(adapterStatus)
        binding.spinnerPayment.setOnItemClickListener { _, _, position, _ ->
            val selectedText = adapterStatus.getItem(position)
//            Toast.makeText(this, selectedText, Toast.LENGTH_LONG).show()
        }

        // customer dropdown
        val adapterCus = ArrayAdapter(this, R.layout.item_drop_down_filter, deliveryArr)
        binding.spinnerDelivery.setAdapter(adapterCus)
        binding.spinnerDelivery.setOnItemClickListener { _, _, position, _ ->
            val selectedText = adapterCus.getItem(position)
//            Toast.makeText(this, selectedText, Toast.LENGTH_LONG).show()
        }
    }

    private fun showCustomRejectDialgoBox() {
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

}