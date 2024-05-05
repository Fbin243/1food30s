package com.zebrand.app1food30s.utils

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentReference
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.squareup.okhttp.Call
import com.squareup.okhttp.Callback
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.Response
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.data.entity.Cart
import com.zebrand.app1food30s.data.entity.CartItem
import com.zebrand.app1food30s.data.entity.Product
import com.zebrand.app1food30s.data.entity.User
import com.zebrand.app1food30s.data.entity.Wishlist
import com.zebrand.app1food30s.ui.authentication.LoginActivity
import org.json.JSONObject
import java.io.IOException
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.math.log


object Utils {
    const val timeHandler: Long = 800
    val handler = Handler(Looper.getMainLooper())
    var hasExchageRate = false

    private fun fetchExchangeRate(callback: (Int) -> Unit) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://api.exchangerate-api.com/v4/latest/USD")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(request: Request?, e: IOException?) {
                println("Failed to fetch exchange rate: ${e?.message}")
            }

            override fun onResponse(response: Response?) {
                val responseBody = response?.body()?.string()
                val exchangeRate = parseExchangeRate(responseBody)
                callback(exchangeRate)
            }
        })
    }

    private fun parseExchangeRate(responseBody: String?): Int {
        return try {
            val json = responseBody?.trim() ?: ""
            val jsonObject = JSONObject(json)
            val rates = jsonObject.getJSONObject("rates")
            val vndRate = rates.getInt("VND")
            vndRate
        } catch (e: Exception) {
            println("Error parsing exchange rate: ${e.message}")
            0
        }
    }

//    fun showCustomSnackbar(view: View, message: String) {
//        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).apply {
////            setAction("Action") {
////                // Handle action click
////            }
//            // Optionally customize the appearance
//            setTextColor(Color.WHITE)
//            setBackgroundTint(Color.BLACK)
//            show()
//        }
//    }

    fun showCustomToast(context: Context, message: String, type: String) {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.custom_toast, null)
        val toastText = layout.findViewById<TextView>(R.id.toast_text)
        toastText.text = message
        val toastIcon = layout.findViewById<ImageView>(R.id.toast_icon)
        var color = ContextCompat.getColor(context, R.color.black)

        // Set text and background color based on the type
        when (type) {
            "success" -> {
                layout.setBackgroundResource(R.drawable.background_toast_success)
//                color = ContextCompat.getColor(context, R.color.black)

            }
            "error" -> {
                layout.setBackgroundResource(R.drawable.background_toast_error)
//                color = ContextCompat.getColor(context, R.color.black)
            }
            "info" -> {
                layout.setBackgroundResource(R.drawable.background_toast_success)
//                color = ContextCompat.getColor(context, R.color.black)
            }
            else -> {
                layout.setBackgroundResource(R.drawable.background_toast_success)
//                color = ContextCompat.getColor(context, R.color.black)
            }
        }
        toastText.setTextColor(color)
        toastIcon.setColorFilter(color, PorterDuff.Mode.SRC_IN)

        Toast(context).apply {
            duration = Toast.LENGTH_SHORT
            view = layout
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 141)
            show()
        }
    }


    fun formatPrice(price: Double, context: Context): String {
        val mySharedPreferences = MySharedPreferences.getInstance(context)
        val languageCode = mySharedPreferences.getString(SingletonKey.KEY_LANGUAGE_CODE) ?: "en"
        val exchangeRate = mySharedPreferences.getInt(SingletonKey.EXCHANGE_RATE)
        if(!hasExchageRate) {
            hasExchageRate = true
            Log.i("TAG123", "formatPrice: exchange rate is 0")
            fetchExchangeRate { exchangeRate ->
                mySharedPreferences.setInt(SingletonKey.EXCHANGE_RATE, exchangeRate)
            }
        }
        var locale = Locale("en", "US")
        var convertedPrice = price

        if (languageCode == "vi") {
            locale = Locale("vi", "VN")
            convertedPrice *= exchangeRate
        }
        val currencyFormat = NumberFormat.getCurrencyInstance(locale)
        return currencyFormat.format(convertedPrice)
    }

    fun formatRating(price: Double): String {
        return String.format("%.1f", price).replace(",", ".")
    }

    fun getShimmerDrawable(): ShimmerDrawable {
        return ShimmerDrawable().apply {
            setShimmer(
                Shimmer.ColorHighlightBuilder().setBaseColor(Color.parseColor("#f3f3f3"))
                    .setBaseAlpha(1.0f).setHighlightColor(Color.parseColor("#e7e7e7"))
                    .setHighlightAlpha(1.0f).setDropoff(50.0f).build()
            )
        }
    }

    fun showShimmerEffect(shimmer: ShimmerFrameLayout, view: View) {
        shimmer.startShimmer()
        shimmer.visibility = View.VISIBLE
        view.visibility = View.GONE
    }

    fun hideShimmerEffect(shimmer: ShimmerFrameLayout, view: View, wantToShow: Boolean = true) {
        shimmer.stopShimmer()
        shimmer.visibility = View.GONE
        if (wantToShow) view.visibility = View.VISIBLE
    }

    fun initSwipeRefreshLayout(
        swipeRefreshLayout: SwipeRefreshLayout,
        onRefreshListener: SwipeRefreshLayout.OnRefreshListener,
        resources: Resources
    ) {
        swipeRefreshLayout.setOnRefreshListener(onRefreshListener)
        swipeRefreshLayout.setColorSchemeColors(resources.getColor(R.color.primary))
        swipeRefreshLayout.setDistanceToTriggerSync(250)
    }

    fun replaceFragment(
        fragment: Fragment,
        supportFragmentManager: FragmentManager,
        containerId: Int
    ) {
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.replace(containerId, fragment).commit()
    }

    fun formatDate(date: Date): String? {
        var localDateTime: LocalDateTime? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
            // Định dạng theo chuẩn HH:mm:ss yyyy-MM-dd
            val formatter = DateTimeFormatter.ofPattern("HH:mm dd-MM-yyyy")
            return localDateTime.format(formatter)
        }
        return null
    }

    fun formatCalendarView(date: CalendarDay): String {
        return "${date.day}/${date.month}/${date.year}"
    }

    fun formatId(id: String): String {
        return id.substring(0, 7)
    }

    fun setLocale(activity: AppCompatActivity, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val resources = activity.resources
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    fun addProductToCart(
        context: Context,
        productId: String,
        userId: String,
        defaultUserId: String = MySharedPreferences.defaultStringValue,
        addQuantity: Int = 1
    ) {
        if (userId == defaultUserId) {
            val loginIntent = Intent(context, LoginActivity::class.java)
            loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(loginIntent)
            return
        }

        val cartRef = FireStoreUtils.mDBCartRef.document(userId)

        val productRef = FireStoreUtils.mDBProductRef.document(productId)
        productRef.get().addOnSuccessListener product@{ productSnapshot ->
            val product = productSnapshot.toObject(Product::class.java)
            val stock = product?.stock ?: 0

            cartRef.get().addOnSuccessListener cart@{ document ->
                val cart = document.toObject(Cart::class.java)
                cart?.let {
                    val existingItemIndex = it.items.indexOfFirst { item -> item.productId == productRef }
                    if (existingItemIndex >= 0) {
                        // Product exists, update quantity
                        val newQuantity = it.items[existingItemIndex].quantity + addQuantity
                        if (newQuantity <= stock) {
                            it.items[existingItemIndex].quantity = newQuantity
                        } else {
//                            Toast.makeText(context, "Not enough stock available.", Toast.LENGTH_SHORT).show()
                            showCustomToast(context, "Not enough stock available.", "error")
                            return@cart
                        }
                    } else {
                        // New product, add to cart with addQuantity
                        if (stock <= 0) {
                            showCustomToast(context, "Not enough stock available.", "error")
                            return@cart
                        }
                        it.items.add(CartItem(productRef, "", "", 0.0, 0.0, "", 0, addQuantity))
                    }

                    cartRef.set(it).addOnSuccessListener {
//                        Toast.makeText(context, "Added to cart successfully!", Toast.LENGTH_SHORT).show()
                        showCustomToast(context, "Added to cart successfully!", "success")
                    }
                }
            }.addOnFailureListener { exception ->
                Log.e("Test00", "Error updating cart: ", exception)
                Toast.makeText(context, "Failed to add to cart.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Log.e("Test00", "Error getting product: ", exception)
            Toast.makeText(context, "Failed to get product details.", Toast.LENGTH_SHORT).show()
        }
    }

    fun setUserDataInFireStore(user: User, callback: () -> Unit = {}) {
        val userRef = FireStoreUtils.mDBUserRef
        val cartRef = FireStoreUtils.mDBCartRef
        val wishListRef = FireStoreUtils.mDBWishlistRef

        val userDoc: DocumentReference = userRef.document() // Automatically generates a unique document ID
        val userId = userDoc.id
        // TODO: cart id instead of user id
        val cartDoc: DocumentReference = cartRef.document(userId) // Use userId as the document ID
//        val cartDoc: DocumentReference = cartRef.document() // Automatically generates a unique document ID
        val wishListDoc: DocumentReference = wishListRef.document() // Automatically generates a unique document ID

        val cartId = cartDoc.id
        val wishListId = wishListDoc.id

        val cart = Cart(id = cartId, userId = userDoc)
        val wishList = Wishlist(id = wishListId, userId = userDoc)
        user.id = userId
        user.cartRef = cartDoc
        user.wishlistRef = wishListDoc

        Log.d("userInfo", "Sign up " + user.toString())

        userDoc.set(user).addOnSuccessListener {
            callback()
        }
        cartDoc.set(cart)
            .addOnSuccessListener {
                // Xử lý khi tài liệu được thêm thành công
                println("Document added with ID: ${userDoc.id}")
            }
            .addOnFailureListener { e ->
                // Xử lý lỗi nếu có
                println("Error adding document: $e")
            }
        wishListDoc.set(wishList)
    }

}