package com.zebrand.app1food30s.ui.checkout

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
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
import vn.zalopay.sdk.ZaloPayError
import vn.zalopay.sdk.ZaloPaySDK
import vn.zalopay.sdk.listeners.PayOrderListener
import java.net.URLEncoder
import java.util.logging.Formatter


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        // ZaloPay SDK Init
//        ZaloPaySDK.init(553, Environment.SANDBOX);

//        paypalConfig()

        preferences = MySharedPreferences.getInstance(this)
        userId = preferences.getString(SingletonKey.KEY_USER_ID) ?: ""

        // TODO: pass from cart fragment?
        cartRepository = CartRepository(FirebaseFirestore.getInstance())
        presenter = CheckoutPresenter(this, cartRepository)


        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyBQFpHSKFfBqU9XM8ZFGOEHPGyMkh6iCZk")
        }

        binding.tvAddress.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                // Launch the Autocomplete intent when the user focuses on the input
                val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
                val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this)
                // TODO
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
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

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE && resultCode == RESULT_OK) {
            val place = Autocomplete.getPlaceFromIntent(data!!)
            binding.tvAddress.setText(place.address) // Set the address in the TextInputEditText
            // Optionally, clear focus from the address input to prevent reopening autocomplete
            binding.tvAddress.clearFocus()

            // Use the place's lat and lng for further processing if needed
            val latLng = place.latLng
            // You might want to update the map's location here
            latLng?.let { CameraUpdateFactory.newLatLngZoom(it, 15f) }?.let { mMap.moveCamera(it) }
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            // Handle the error
            val status = Autocomplete.getStatusFromIntent(data!!)
            Log.i("CheckoutActivity", status.statusMessage ?: "Autocomplete error")
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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        ZaloPaySDK.getInstance().onResult(intent)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney").draggable(true))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 10f))

        // Marker drag listener
        mMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {}

            override fun onMarkerDrag(marker: Marker) {}

            override fun onMarkerDragEnd(marker: Marker) {
                // Here you can get the new position of the marker
                val newPos = marker.position
            }
        })
    }

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
            address = binding.tvAddress.text.toString()
            val note = binding.tvNote.text.toString()
            if (address.isEmpty()) {
                // Show error message
                binding.addressContainer.error = getString(R.string.error_address_required)
                return@setOnClickListener
            } else {
                // Clear error
                binding.addressContainer.error = null
            }

            // Proceed with the place order logic if address is not empty
            // TODO: add to utils
//            requestZalo()
//            requestVNPay()
//            val userDocRef = FirebaseFirestore.getInstance().document("accounts/$userId")
//            presenter.onPlaceOrderClicked(cartId, userDocRef, address, note)
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
//            binding.tvCartTotalAmount.text = getString(R.string.product_price_number, totalPrice)
            binding.textViewAmount.text = getString(R.string.product_price_number, totalPrice)
        }
    }

    override fun displayError(error: String) {
        runOnUiThread {
            // Handle error or empty state
//            binding.tvCartTotalAmount.text = getString(R.string.product_price_number, 0.0)
            binding.textViewAmount.text = getString(R.string.product_price_number, 0.0)
        }
    }

    private fun handleCloseCheckoutScreen() {
        binding.ivBack.root.setOnClickListener {
            finish()
        }
    }
}

