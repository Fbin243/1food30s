package com.zebrand.app1food30s.ui.my_order.my_order_details

import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
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
import android.widget.RatingBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieDrawable
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.MyOrderDetailsAdapter
import com.zebrand.app1food30s.data.entity.Order
import com.zebrand.app1food30s.data.entity.OrderItem
import com.zebrand.app1food30s.data.entity.Product
import com.zebrand.app1food30s.databinding.ActivityMyOrderDetailsBinding
import com.zebrand.app1food30s.databinding.DialogDeleteAccountBinding
import com.zebrand.app1food30s.databinding.TimelineBinding
import com.zebrand.app1food30s.ui.product_detail.ProductDetailActivity
import com.zebrand.app1food30s.utils.FireStoreUtils
import com.zebrand.app1food30s.utils.MySharedPreferences
import com.zebrand.app1food30s.utils.SingletonKey
import com.zebrand.app1food30s.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date


@Suppress("DEPRECATION")
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

    private fun getMyOrderDetailsList() {
        Log.i("TAG123", "getMyOrderDetailsList: ${orderDetails.orderStatus}")
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
        binding.tvPhone.text = orderDetails.user.phone
        binding.tvNote.text = orderDetails.note
        binding.tvReason.text = orderDetails.cancelReason
        binding.tvPaymentStatus.text = orderDetails.paymentStatus.uppercase()
        binding.tvPaymentMethod.text = orderDetails.paymentMethod
        binding.tvSubTotal.text = Utils.formatPrice(itemOrderDetailsAdapter.getSubTotal(), this)
        binding.tvDiscount.text = Utils.formatPrice(itemOrderDetailsAdapter.getDiscount(), this)
        binding.tvTotalAmount.text = Utils.formatPrice(orderDetails.totalAmount,this)

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

        if(orderDetails.note.trim().isEmpty()) {
            binding.linearNote.visibility = View.GONE
        }

        if (orderDetails.orderStatus == SingletonKey.CANCELLED) {
            binding.linearTracking.visibility = View.GONE
            binding.tvHasBeenCancelled.visibility = View.VISIBLE

            if(orderDetails.cancelReason != null && orderDetails.cancelReason != "") {
                binding.linearReason.visibility = View.VISIBLE
            }

            binding.tvHasBeenCancelled.text = resources.getString(R.string.txt_has_been_cancelled)
            binding.tvHasBeenCancelled.setTextColor(ContextCompat.getColor(this, R.color.orange))
        } else if(orderDetails.orderStatus == SingletonKey.DELIVERED) {
            binding.linearTracking.visibility = View.GONE
            binding.tvHasBeenCancelled.visibility = View.VISIBLE

            binding.tvHasBeenCancelled.text = resources.getString(R.string.txt_your_order_has_been_successful)
            binding.tvHasBeenCancelled.setTextColor(ContextCompat.getColor(this, R.color.primary))
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

            } else {
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
            binding.cardControl.visibility = View.GONE
            dialog.dismiss()
        }

        cancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun showShimmerEffectForOrders(size: Int) {
        Log.d("Test00", "itemCount" + size)
        for (i in 0 until size) {
            val shimmerLayout = layoutInflater.inflate(R.layout.item_order_details_shimmer, binding.linearShimmer, false)
            // Add the inflated layout to the parent LinearLayout
            binding.linearShimmer.addView(shimmerLayout)
        }

        Utils.showShimmerEffect(binding.orderDetailsShimmer, binding.orderItemList)
    }

    override fun hideShimmerEffectForOrders() {
        Utils.hideShimmerEffect(binding.orderDetailsShimmer, binding.orderItemList)
    }

    override fun handleReviewProduct() {
        itemOrderDetailsAdapter.onReviewBtnClick = { orderItem, holder ->
            // Initialize the dialog
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.dialog_review)

            // Initialize the views
            val ratingBar = dialog.findViewById<RatingBar>(R.id.rating_bar)
            val etReview =
                dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.tvFeedback)
            val btnSubmit = dialog.findViewById<Button>(R.id.btn_submit)

            // Set up the click listener for the submit button
            btnSubmit.setOnClickListener {
                // Retrieve the rating and the review text
                val rating = ratingBar.rating
                val review = etReview.text.toString()

                val mySharedPreferences = MySharedPreferences.getInstance(this)
                val userId =
                    mySharedPreferences.getString(SingletonKey.KEY_USER_ID) ?: "Default Value"
                Log.d(
                    "TAG123",
                    "Rating: $rating, Review: $review, User ID: $userId, Product ID: ${orderItem.productId?.path}"
                )

                FireStoreUtils.mDBReviewRef.add(
                    hashMapOf(
                        "rating" to rating,
                        "content" to review,
                        "idAccount" to FireStoreUtils.mDBUserRef.document(userId),
                        "idProduct" to orderItem.productId,
                        "date" to Date()
                    )
                )

                // Mark isReviewed as true
                val orderRef = FireStoreUtils.mDBOrderRef.document(idOrder)
                orderRef.update("items", orderDetails.items.map {
                    if (it.productId == orderItem.productId) {
                        it.copy(reviewed = true)
                    } else {
                        it
                    }
                })
                CoroutineScope(Dispatchers.IO).launch {
                    val product = orderItem.productId!!.get().await().toObject(Product::class.java)
                    val avgRating = product?.avgRating ?: 0.0
                    val numReview = product?.numReview ?: 0
                    val newRating = avgRating * numReview
                    orderItem.productId.update("avgRating", (newRating + rating) / (numReview + 1))
                    orderItem.productId.update("numReview", numReview + 1)
                    withContext(Dispatchers.Main) {
                        holder.reviewBtn.text = resources.getString(R.string.txt_reviewed)
                        holder.reviewBtn.setTextColor(resources.getColor(R.color.secondary))
                        holder.reviewBtn.setOnClickListener {
                            val intent = Intent(this@MyOrderDetailsActivity, ProductDetailActivity::class.java)
                            intent.putExtra("idProduct", orderItem.productId.id)
                            Log.i("TAG123", "handleReviewProduct: ${orderItem.productId.id}")
                            startActivity(intent)
                        }
                    }
                }
                // Dismiss the dialog
                dialog.dismiss()
            }

            // Show the dialog
            dialog.show()
        }
        itemOrderDetailsAdapter.onReviewBtnClickAfterReview = { orderItem ->
            val intent = Intent(this, ProductDetailActivity::class.java)
            intent.putExtra("idProduct", orderItem.productId?.id)
            Log.i("TAG123", "handleReviewProduct: ${orderItem.productId?.id}")
            startActivity(intent)
        }
    }
}