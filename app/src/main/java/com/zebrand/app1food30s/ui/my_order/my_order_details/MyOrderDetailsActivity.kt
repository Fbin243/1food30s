package com.zebrand.app1food30s.ui.my_order.my_order_details

import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieDrawable
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.MyOrderDetailsAdapter
import com.zebrand.app1food30s.data.entity.Order
import com.zebrand.app1food30s.data.entity.OrderItem
import com.zebrand.app1food30s.databinding.ActivityMyOrderDetailsBinding
import com.zebrand.app1food30s.databinding.DialogDeleteAccountBinding
import com.zebrand.app1food30s.databinding.TimelineBinding
import com.zebrand.app1food30s.utils.SingletonKey
import com.zebrand.app1food30s.utils.Utils

class MyOrderDetailsActivity : AppCompatActivity(), MyOrderDetailsMVPView {
    lateinit var binding: ActivityMyOrderDetailsBinding
    private lateinit var presenter: MyOrderDetailsPresenter
    private lateinit var idOrder: String
    private var orderDetails: Order = Order()
    private lateinit var itemOrderDetailsAdapter: MyOrderDetailsAdapter
    private var myOrderDetailsList: MutableList<OrderItem> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyOrderDetailsBinding.inflate(layoutInflater)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
                // Dark mode is enabled, so force light mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
        setContentView(binding.root)

        init()

        events()

        lottieAnimation()

        getMyOrderDetailsList()
    }

    private fun init() {
        presenter = MyOrderDetailsPresenter(this, this)

        idOrder = intent.getStringExtra("idOrder").toString()
    }

    private fun events() {
        binding.backIcon.root.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }

    private fun lottieAnimation() {
        // Start the animation
        binding.lottieSuccess.repeatCount = LottieDrawable.INFINITE

        binding.lottieSuccess.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {
                binding.lottieSuccess.pauseAnimation()

                binding.lottieSuccess.postDelayed({
                    binding.lottieSuccess.resumeAnimation()
                }, 5000)
            }

        })

        binding.lottieSuccess.playAnimation()
    }

    override fun getMyOrderDetailsList() {
        itemOrderDetailsAdapter = MyOrderDetailsAdapter(this, myOrderDetailsList)
        itemOrderDetailsAdapter.onItemClick = {
//            Chuyển qua product details
//            GlobalUtils.myStartActivityWithString(this, MyOrderDetailsActivity::class.java, "idOrder", it.id)
        }

        // Set layout manager và adapter cho RecyclerView
        binding.rcvMyOrderDetails.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.rcvMyOrderDetails.adapter = itemOrderDetailsAdapter

        //getData
        presenter.getMyOrderDetails(idOrder, orderDetails, itemOrderDetailsAdapter)
    }

    @SuppressLint("SetTextI18n")
    override fun setOrderDetailsUI() {
        binding.tvOrderId.text = Utils.formatId(orderDetails.id)
        binding.tvOrderDate.text = Utils.formatDate(orderDetails.date)
        binding.tvAddress.text = orderDetails.shippingAddress
        binding.tvPaymentStatus.text = orderDetails.paymentStatus.uppercase()
        binding.tvSubTotal.text = "$" + Utils.formatPrice(itemOrderDetailsAdapter.getSubTotal())
        binding.tvDiscount.text = "$" + Utils.formatPrice(itemOrderDetailsAdapter.getDiscount())
        binding.tvTotalAmount.text = "$" + Utils.formatPrice(orderDetails.totalAmount)

//        -----------------Control button-----------------
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        if (orderDetails.orderStatus != SingletonKey.PENDING) {
            binding.linearControl.visibility = View.GONE
            layoutParams.bottomMargin = 130
        } else {
            layoutParams.bottomMargin = 270
        }

        if (orderDetails.orderStatus == SingletonKey.CANCELLED) {
            binding.linearTracking.visibility = View.GONE
            binding.tvHasBeenCancelled.visibility = View.VISIBLE
        } else {
            val bindingTimeLine = DataBindingUtil.bind<TimelineBinding>(binding.timeLineView.root)
            if (bindingTimeLine != null) {
                bindingTimeLine.timelineList = getTimeLineList()
            }
        }

        binding.linearOrderItems.layoutParams = layoutParams
//        --------------------------------------------

        binding.cancelOrderBtn.setOnClickListener {
            showCustomConfirmDialogBox(
                R.string.txt_cancel_payment,
                R.string.txt_cancel_payment_content
            )
        }
    }

    private fun getTimeLineList(): List<Int> {
        val enumList: List<String> = resources.getStringArray(R.array.delivery_array).toList()
        val intArray: MutableList<Int> = mutableListOf()
        var init = 1
        for (i in enumList.indices) {
            if (enumList[i] != orderDetails.orderStatus) {
                intArray.add(init)

            }else{
                intArray.add(2)
                init = 3
            }
        }
        Log.d("Test00", "enumList[i]: $intArray")
        return intArray
    }

    private fun showCustomConfirmDialogBox(title: Int, content: Int) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        val bindingSub: DialogDeleteAccountBinding =
            DialogDeleteAccountBinding.inflate(layoutInflater)
        dialog.setContentView(bindingSub.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val acceptBtn: Button = dialog.findViewById(R.id.saveBtn)
        val cancel: Button = dialog.findViewById(R.id.cancelBtn)

        bindingSub.title = resources.getString(title)
        bindingSub.content = resources.getString(content)

        acceptBtn.setOnClickListener {
            presenter.changeOrderStatus(idOrder, SingletonKey.CANCELLED)
            dialog.dismiss()
            finish()
        }

        cancel.setOnClickListener {
            dialog.dismiss()
            finish()
        }

        dialog.show()
    }
}