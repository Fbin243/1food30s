package com.zebrand.app1food30s.ui.checkout

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.BuildConfig
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.firestore.FirebaseFirestore
//import com.paypal.checkout.PayPalCheckout
//import com.paypal.checkout.approve.OnApprove
//import com.paypal.checkout.config.CheckoutConfig
//import com.paypal.checkout.config.Environment
//import com.paypal.checkout.config.SettingsConfig
//import com.paypal.checkout.createorder.CreateOrder
//import com.paypal.checkout.createorder.CurrencyCode
//import com.paypal.checkout.createorder.OrderIntent
//import com.paypal.checkout.createorder.UserAction
//import com.paypal.checkout.order.Amount
//import com.paypal.checkout.order.AppContext
//import com.paypal.checkout.order.OrderRequest
//import com.paypal.checkout.order.PurchaseUnit
import com.vnpay.authentication.VNP_AuthenticationActivity
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.CheckoutItemsAdapter
import com.zebrand.app1food30s.data.entity.CartItem
//import com.zebrand.app1food30s.data.zalopay.CreateOrder
import com.zebrand.app1food30s.databinding.ActivityCheckoutBinding
import com.zebrand.app1food30s.ui.cart.CartRepository
import com.zebrand.app1food30s.ui.main.MainActivity
import com.zebrand.app1food30s.utils.MySharedPreferences
import com.zebrand.app1food30s.utils.SingletonKey
//import vn.zalopay.sdk.ZaloPayError
//import vn.zalopay.sdk.ZaloPaySDK
//import vn.zalopay.sdk.listeners.PayOrderListener
import java.net.URLEncoder
import java.util.logging.Formatter

import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.Locale
import android.location.Location
import com.google.gson.Gson
import com.paypal.android.corepayments.CoreConfig
import com.paypal.android.corepayments.Environment
import com.paypal.android.corepayments.PayPalSDKError
import com.paypal.android.paypalnativepayments.PayPalNativeCheckoutClient
import com.paypal.android.paypalnativepayments.PayPalNativeCheckoutListener
import com.paypal.android.paypalnativepayments.PayPalNativeCheckoutRequest
import com.paypal.android.paypalnativepayments.PayPalNativeCheckoutResult
import com.paypal.android.paypalnativepayments.PayPalNativePaysheetActions
import com.paypal.android.paypalnativepayments.PayPalNativeShippingAddress
import com.paypal.android.paypalnativepayments.PayPalNativeShippingListener
import com.paypal.android.paypalnativepayments.PayPalNativeShippingMethod
import com.zebrand.app1food30s.utils.Utils
import okhttp3.*
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.config.Configuration
import java.io.File

class CheckoutActivity : AppCompatActivity(), CheckoutMVPView, OnMapReadyCallback {

    private lateinit var binding: ActivityCheckoutBinding
    private val checkoutItemsAdapter = CheckoutItemsAdapter()
    private lateinit var cartRepository: CartRepository
    private lateinit var presenter: CheckoutPresenter
    private lateinit var cartId: String
    private lateinit var preferences: MySharedPreferences
    private lateinit var userId: String
    private lateinit var address: String
    private lateinit var mMap: GoogleMap
    private val AUTOCOMPLETE_REQUEST_CODE = 100 // Class constant
    private val YOUR_CLIENT_ID = "AXpqoGgnoXww1RmM2N15AKI7LV4es1uEB-kk0qO1X9OwdELkXnS18nTQ50Kdt9ERQQUoVOsGvOolFgWI"
    private lateinit var apiKey: String
    private lateinit var defaultLatLng: LatLng
    private var totalPrice: Double = 0.0
    private var shippingFee: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        // ZaloPay SDK Init
//        ZaloPaySDK.init(553, Environment.SANDBOX);

//        paypalConfig()
        apiKey = "AIzaSyBQFpHSKFfBqU9XM8ZFGOEHPGyMkh6iCZk"
        defaultLatLng = LatLng(10.780889, 106.699306) // Central Post Office coordinates

        preferences = MySharedPreferences.getInstance(this)
        userId = preferences.getString(SingletonKey.KEY_USER_ID) ?: ""

        // TODO: pass from cart fragment?
        cartRepository = CartRepository(FirebaseFirestore.getInstance())
        presenter = CheckoutPresenter(this, cartRepository)

        // -----Maps SDK for Android-----
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        // when the map is ready, onMapReady() callback is triggered
        mapFragment.getMapAsync(this)

        // -----Places SDK-----
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }

        binding.tvAddress.setOnFocusChangeListener { v, hasFocus ->
//            Log.d("FocusChange", "Focus change detected. Has focus: $hasFocus")
            if (hasFocus) {
                // Log the beginning of the Autocomplete intent action
//                Log.d("FocusChange", "Launching Autocomplete intent.")

                // Define the fields to be retrieved
                val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
                // Full screen search
                val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this)

                // Launch the Autocomplete intent when the user focuses on the input
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
//                Log.d("FocusChange", "Intent for Autocomplete started.")
            } else {
                // Optionally log when the user moves focus away
//                Log.d("FocusChange", "Focus lost from tvAddress.")
            }
        }

        setupRecyclerView()
        handleCloseCheckoutScreen()

        cartId = intent.getStringExtra("user_id") ?: return
        presenter.loadCartData(cartId)

        handlePlaceOrderButton()
    }

    private fun paypalConfig(){
//        val config = CheckoutConfig(
//            application = application,
//            clientId = YOUR_CLIENT_ID,
//            environment = Environment.SANDBOX,
//            returnUrl = "com.zebrand.app1food30s://paypalpay",
//            currencyCode = CurrencyCode.USD,
//            userAction = UserAction.PAY_NOW,
//            settingsConfig = SettingsConfig(
//                loggingEnabled = true,
//                showWebCheckout = false
//            )
//        )
//        PayPalCheckout.setConfig(config)
//
//        binding.paymentButtonContainer.setup(
//            createOrder =
//            CreateOrder { createOrderActions ->
//                val order =
//                    OrderRequest(
//                        intent = OrderIntent.CAPTURE,
//                        appContext = AppContext(userAction = UserAction.PAY_NOW),
//                        purchaseUnitList =
//                        listOf(
//                            PurchaseUnit(
//                                amount =
//                                Amount(currencyCode = CurrencyCode.USD, value = "10.00")
//                            )
//                        )
//                    )
//                createOrderActions.create(order)
//            },
//            onApprove =
//            OnApprove { approval ->
//                Log.i("paypal", "OrderId: ${approval.data.orderId}")
//            }
//        )
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val hoChiMinhCity = LatLng(10.776889, 106.700806)
        val marker = mMap.addMarker(MarkerOptions().position(hoChiMinhCity).title("Marker in Ho Chi Minh City").draggable(true))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hoChiMinhCity, 10f))

        mMap.setOnCameraMoveStartedListener {
            val scrollView = findViewById<LockableNestedScrollView>(R.id.scrollView)
            scrollView.setScrollingEnabled(false)
        }

        mMap.setOnCameraIdleListener {
            val scrollView = findViewById<LockableNestedScrollView>(R.id.scrollView)
            scrollView.setScrollingEnabled(true)
        }

        // Add a click listener for the map
        mMap.setOnMapClickListener { latLng ->
            // Move the marker to the clicked position
            if (marker != null) {
                marker.position = latLng
                calculateDistance(defaultLatLng, latLng)
            }

            // Optionally, animate the camera to the new position
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))

            // Use reverse geocoding to update the address
            val geocoder = Geocoder(this@CheckoutActivity, Locale.getDefault())
            val addresses: List<Address>?
            try {
                addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address: Address = addresses[0]
                    // Fetch the address lines and join them
                    val addressText = (0..address.maxAddressLineIndex).joinToString(separator = " ") {
                        address.getAddressLine(it)
                    }
                    binding.tvAddress.setText(addressText)
                }
            } catch (e: IOException) {
                Log.e("CheckoutActivity", "Unable to get address from the clicked location", e)
            }
        }
    }



    private fun requestZalo(){
//        val orderApi = CreateOrder()
//
//        try {
//            val data = orderApi.createOrder("20000")
//            val code = data.getString("returncode")
//            if (code == "1") {
//                val token = data.getString("zptranstoken")
//                ZaloPaySDK.getInstance().payOrder(this, token, "demozpdk://app", object : PayOrderListener {
//                    override fun onPaymentSucceeded(p0: String?, p1: String?, p2: String?) {
//                        Toast.makeText(this@CheckoutActivity, "Payment success", Toast.LENGTH_SHORT).show()
//                    }
//
//                    override fun onPaymentCanceled(p0: String?, p1: String?) {
//                        Toast.makeText(this@CheckoutActivity, "Payment canceled", Toast.LENGTH_SHORT).show()
//                    }
//
//                    override fun onPaymentError(p0: ZaloPayError?, p1: String?, p2: String?) {
//                        Toast.makeText(this@CheckoutActivity, "Payment error", Toast.LENGTH_SHORT).show()
//                    }
//                })
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
    }

//    override fun onNewIntent(intent: Intent?) {
//        super.onNewIntent(intent)
////        ZaloPaySDK.getInstance().onResult(intent)
//    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        Log.d("ActivityResult", "Request Code: $requestCode, Result Code: $resultCode")

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                val place = Autocomplete.getPlaceFromIntent(data)
//                Log.d("ActivityResult", "Place found: ${place.name}")

                // Get the address
                binding.tvAddress.setText(place.address)
                binding.tvAddress.clearFocus()
//                Log.d("ActivityResult", "Address set: ${place.address}")

                val latLng = place.latLng
                // Use latitude and longitude to update the map's location
                latLng?.let {
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(it, 15f)
                    mMap.moveCamera(cameraUpdate)
//                    Log.d("ActivityResult", "Map camera moved to: ${latLng.latitude}, ${latLng.longitude}")
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR && data != null) {
                // Handle the error
                val status = Autocomplete.getStatusFromIntent(data)
//                Log.e("ActivityResult", "Autocomplete error: ${status.statusMessage}")
            } else {
//                Log.w("ActivityResult", "Result not OK or data is null")
            }
        } else {
//            Log.d("ActivityResult", "Unhandled request code")
        }
    }

    private fun calculateDistance(from: LatLng, to: LatLng) {
        val results = FloatArray(1)
        Location.distanceBetween(from.latitude, from.longitude, to.latitude, to.longitude, results)
        val distance = String.format("%.1f", results[0]/1000.0).toDouble() // distance in km

        runOnUiThread {
            shippingFee = distance * 0.2
            val formattedShipFee = Utils.formatPrice(shippingFee, this@CheckoutActivity)  // Assume 0.2 as your rate
            binding.tvShipInfo.text = "Ship: $formattedShipFee - Distance: ${distance} km"
            updateTotalPrice()
        }
    }

    data class DirectionsResult(val routes: List<Route>)
    data class Route(val legs: List<Leg>)
    data class Leg(val distance: Distance, val duration: Duration)
    data class Distance(val text: String, val value: Int)
    data class Duration(val text: String, val value: Int)

    // TODO: fix snackbar top to bottom of container
    override fun navigateToOrderConfirmation(showOrderConfirmation: Boolean, orderId: String) {
        if (showOrderConfirmation) {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("showOrderConfirmation", true)
                putExtra("address", address)
                putExtra("orderId", orderId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        } else {
            // Handle the failure case appropriately, maybe show an error message
        }
    }

    // presenter.onPlaceOrderClicked(cartId)
    private fun handlePlaceOrderButton() {
        binding.btnPlaceOrder.setOnClickListener {
            Toast.makeText(this@CheckoutActivity, "TEST", Toast.LENGTH_LONG).show()

            // Thêm client id vào
            val coreConfig = CoreConfig("", environment = Environment.SANDBOX)

            val payPalNativeClient = PayPalNativeCheckoutClient(
                application = this@CheckoutActivity.application,
                coreConfig = coreConfig,
                returnUrl = "com.zebrand.app1food30s://paypalpay"
            )

            payPalNativeClient.listener = object : PayPalNativeCheckoutListener {
                override fun onPayPalCheckoutStart() {
                    // the PayPal paysheet is about to show up
                }
                override fun onPayPalCheckoutSuccess(result: PayPalNativeCheckoutResult) {
                    // order was approved and is ready to be captured/authorized
                }
                override fun onPayPalCheckoutFailure(error: PayPalSDKError) {
                    // handle the error
                }
                override fun onPayPalCheckoutCanceled() {
                    // the user canceled the flow
                }
            }

            payPalNativeClient.shippingListener = object : PayPalNativeShippingListener {
                override fun onPayPalNativeShippingAddressChange(
                    actions: PayPalNativePaysheetActions,
                    shippingAddress: PayPalNativeShippingAddress
                ) {
                    // called when the user updates their chosen shipping address
                    // you must call actions.approve() or actions.reject() in this callback
                    actions.approve()
                    // OPTIONAL: you can optionally patch your order. Once complete, call actions.approve() if successful or actions.reject() if not.
                }
                override fun onPayPalNativeShippingMethodChange(
                    actions: PayPalNativePaysheetActions,
                    shippingMethod: PayPalNativeShippingMethod
                ) {
                    // called when the user updates their chosen shipping method
                    // patch your order server-side with the updated shipping amount.
                    // Once complete, call `actions.approve()` or `actions.reject()`
                    try {
                        // TODO: patch order on server side, notify SDK of success by calling actions.approve()
                        actions.approve()
                    } catch (e: Exception) {
                        // catch any errors from patching the order e.g. network unavailable
                        // and notify SDK that the update was unsuccessful
                        actions.reject()
                    }
                }
            }

            //00W40240UH618401H
            val request = PayPalNativeCheckoutRequest("00W40240UH618401H")
            payPalNativeClient.startCheckout(request)


//            address = binding.tvAddress.text.toString()
//            val note = binding.tvNote.text.toString()
//            if (address.isEmpty()) {
//                // Show error message
//                binding.addressContainer.isErrorEnabled = true
//                binding.addressContainer.error = getString(R.string.error_address_required)
//                return@setOnClickListener
//            } else {
//                // Clear error
//                binding.addressContainer.isErrorEnabled = false
//                binding.addressContainer.error = null
//            }
//
//            // Proceed with the place order logic if address is not empty
//            // TODO: add to utils
////            requestZalo()
////            requestVNPay()
////            val userDocRef = FirebaseFirestore.getInstance().document("accounts/$userId")
////            presenter.onPlaceOrderClicked(cartId, userDocRef, address, note)
//            val userDocRef = FirebaseFirestore.getInstance().document("accounts/$userId")
//            presenter.onPlaceOrderClicked(cartId, userDocRef, address, note, shippingFee)
        }
    }

    fun Context.getConnectivityManager() = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun getIpAddress(context: Context) = with(context.getConnectivityManager()) {
        getLinkProperties(activeNetwork)!!.linkAddresses[1].address.hostAddress!!
    }


    private fun setupRecyclerView() {
        binding.checkoutItemsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.checkoutItemsRecyclerView.adapter = checkoutItemsAdapter
    }

    override fun displayCartItems(cartItems: List<CartItem>, totalPrice: Double) {
        runOnUiThread {
            checkoutItemsAdapter.setItems(cartItems)
            this.totalPrice = totalPrice // Update the internal total price
            updateTotalPrice() // Update UI
        }
    }

    private fun updateTotalPrice() {
        val finalTotal = totalPrice + shippingFee
        binding.textViewAmount.text = Utils.formatPrice(finalTotal, this)
    }

    override fun displayError(error: String) {
        runOnUiThread {
            // Handle error or empty state
//            binding.tvCartTotalAmount.text = getString(R.string.product_price_number, 0.0)
            binding.textViewAmount.text = Utils.formatPrice(0.0, this)
        }
    }

    private fun handleCloseCheckoutScreen() {
        binding.ivBack.root.setOnClickListener {
            finish()
        }
    }
}

