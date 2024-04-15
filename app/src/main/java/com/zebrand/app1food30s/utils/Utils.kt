package com.zebrand.app1food30s.utils

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.facebook.shimmer.ShimmerFrameLayout
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.data.entity.Cart
import com.zebrand.app1food30s.data.entity.CartItem
import com.zebrand.app1food30s.data.entity.Product
import com.zebrand.app1food30s.ui.authentication.LoginActivity
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


object Utils {
    fun formatPrice(price: Double, context: Context): String {
        val mySharedPreferences = MySharedPreferences.getInstance(context)
        val languageCode = mySharedPreferences.getString(SingletonKey.KEY_LANGUAGE_CODE) ?: "en"
        if (languageCode == "vi") {
            return String.format("%.0f", price * 25000).replace(",", ".") + "đ"
        }
        return "$" + String.format("%.2f", price).replace(",", ".")
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
//        Handler().postDelayed({
        shimmer.stopShimmer()
        shimmer.visibility = View.GONE
        if (wantToShow) view.visibility = View.VISIBLE
//        }, 1000)
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
        defaultUserId: String = MySharedPreferences.defaultStringValue
    ) {
        if (userId == defaultUserId) {
            val loginIntent = Intent(context, LoginActivity::class.java)
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
//                val cart = if (document.exists()) {
//                    document.toObject(Cart::class.java)
//                }
//                else {
//                    Cart(userId = db.document("accounts/$userId"), items = mutableListOf())
//                }
                cart?.let {
                    val existingItemIndex =
                        it.items.indexOfFirst { item -> item.productId == productRef }
                    if (existingItemIndex >= 0) {
                        // Product exists, update quantity
                        val newQuantity = it.items[existingItemIndex].quantity + 1
                        if (newQuantity <= stock) {
                            it.items[existingItemIndex].quantity = newQuantity
                        } else {
                            Toast.makeText(context, "Product is out of stock.", Toast.LENGTH_SHORT)
                                .show()
                            return@cart
                        }
                    } else {
                        // New product, add to cart
                        it.items.add(CartItem(productRef, "", "", 0.0, 0.0, "", 0, 1))
                    }

                    cartRef.set(it).addOnSuccessListener {
                        Toast.makeText(
                            context,
                            "Added to cart successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
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

//    fun addProductToCart(context: Context, productId: String) {
//        val db = FirebaseFirestore.getInstance()
//        val preferences = MySharedPreferences.getInstance(context)
//        val userId = preferences.getString(SingletonKey.KEY_USER_ID) ?: ""
//        val defaultId = MySharedPreferences.defaultStringValue
//
//        // Check if the user is logged in before proceeding
//        if (userId == defaultId) {
//            // User is not logged in, navigate to LoginActivity
//            val loginIntent = Intent(context, LoginActivity::class.java)
//            context.startActivity(loginIntent)
//            return // Stop further execution of this function
//        }
//
//        val cartRef = FireStoreUtils.mDBCartRef.document(userId)
//
//        val productRef = FireStoreUtils.mDBProductRef.document(productId)
//        productRef.get().addOnSuccessListener { productSnapshot ->
//            val product = productSnapshot.toObject(Product::class.java)
//            val stock = product?.stock ?: 0
//
//            if (stock > 0) {
//                cartRef.get().addOnSuccessListener { document ->
//                    val cart = if (document.exists()) {
//                        document.toObject(Cart::class.java)
//                    } else {
//                        Cart(userId = db.document("accounts/$userId"), items = mutableListOf())
//                    }
//
//                    cart?.let {
//                        val existingItemIndex =
//                            it.items.indexOfFirst { item -> item.productId == productRef }
//                        if (existingItemIndex >= 0) {
//                            // Product exists, update quantity
//                            it.items[existingItemIndex].quantity += 1
//                        } else {
//                            // New product, add to cart
//                            // TODO!
//                            it.items.add(CartItem(productRef, "", "", 0.0, "", 0, 1))
//                        }
//
//                        cartRef.set(it).addOnSuccessListener {
//                            Toast.makeText(
//                                context,
//                                "Added to cart successfully!",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                    }
//                }.addOnFailureListener { exception ->
//                    Log.e("Test00", "Error updating cart: ", exception)
//                    Toast.makeText(context, "Failed to add to cart.", Toast.LENGTH_SHORT).show()
//                }
//            } else {
//                Toast.makeText(context, "Product is out of stock.", Toast.LENGTH_SHORT).show()
//            }
//        }.addOnFailureListener { exception ->
//            Log.e("Test00", "Error getting product: ", exception)
//            Toast.makeText(context, "Failed to get product details.", Toast.LENGTH_SHORT).show()
//        }
//    }

}