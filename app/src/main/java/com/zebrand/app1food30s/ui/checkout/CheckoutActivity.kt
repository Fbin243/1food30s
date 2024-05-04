package com.zebrand.app1food30s.ui.checkout


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
//import com.zebrand.app1food30s.data.zalopay.CreateOrder
//import vn.zalopay.sdk.ZaloPayError
//import vn.zalopay.sdk.ZaloPaySDK
//import vn.zalopay.sdk.listeners.PayOrderListener
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.StrictMode
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.here.sdk.core.GeoBox
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.LanguageCode
import com.here.sdk.core.engine.SDKNativeEngine
import com.here.sdk.core.engine.SDKOptions
import com.here.sdk.core.errors.InstantiationErrorException
import com.here.sdk.search.SearchEngine
import com.here.sdk.search.SearchOptions
import com.here.sdk.search.TextQuery
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
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.AddressAdapter
import com.zebrand.app1food30s.adapter.CheckoutItemsAdapter
import com.zebrand.app1food30s.data.entity.CartItem
import com.zebrand.app1food30s.databinding.ActivityCheckoutBinding
import com.zebrand.app1food30s.ui.cart.CartRepository
import com.zebrand.app1food30s.ui.main.MainActivity
import com.zebrand.app1food30s.utils.MySharedPreferences
import com.zebrand.app1food30s.utils.SingletonKey
import com.zebrand.app1food30s.utils.Utils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

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
    private lateinit var apiKey: String
    private lateinit var defaultLatLng: LatLng
    private var totalPrice: Double = 0.0
    private var shippingFee: Double = 0.0
    private lateinit var paymentArr: Array<String>

    private lateinit var queue: RequestQueue
    private var orderId = ""
    private val url = "https://api-m.sandbox.paypal.com/v2/checkout/orders/"
    private val urlToken = "https://api-m.sandbox.paypal.com/v1/oauth2/token"
    var token = "Bearer "
    val clientId = "AXpqoGgnoXww1RmM2N15AKI7LV4es1uEB-kk0qO1X9OwdELkXnS18nTQ50Kdt9ERQQUoVOsGvOolFgWI"
//    val clientId = "AQi0jeUAAGwLBWPY-_J-KqReZQ5udKOfjEH17RwJGuzrRFqn-RKKiBoOtdSF1AOEd6yVw_tzGM1sbvzd"
    val clientSecret = "EO0MxmLuQfbA0Cq8PSNbDA6JHHims1JBMjG4gCLLmjMesgI0tpIdWYyIJMBjsPCuRzltRQdbQaWIAIc4"
//    val clientSecret = "AQi0jeUAAGwLBWPY-_J-KqReZQ5udKOfjEH17RwJGuzrRFqn-RKKiBoOtdSF1AOEd6yVw_tzGM1sbvzd"

    private var searchEngine: SearchEngine? = null
    // Coordinates roughly encompassing Ho Chi Minh City
    private lateinit var southWestCornerHCMC: GeoCoordinates
    private lateinit var northEastCornerHCMC: GeoCoordinates
    private lateinit var hcmcGeoBox: GeoBox
    private lateinit var hcmcArea: TextQuery.Area
    private var marker: Marker? = null

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
        defaultLatLng = LatLng(10.762622, 106.682171)  // University of Science

        preferences = MySharedPreferences.getInstance(this)
        userId = preferences.getString(SingletonKey.KEY_USER_ID) ?: ""

        // TODO: pass from cart fragment?
        cartRepository = CartRepository(FirebaseFirestore.getInstance())
        presenter = CheckoutPresenter(this, cartRepository)

//        21127197
        paymentArr = resources.getStringArray(R.array.payment_method_array)
        // payment method dropdown
        val adapterStatus = ArrayAdapter(this, R.layout.item_drop_down_filter, paymentArr)
        binding.spinnerPayment.setAdapter(adapterStatus)
//    --------------------

        // -----Maps SDK for Android-----
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        // when the map is ready, onMapReady() callback is triggered
        mapFragment.getMapAsync(this)
        // -----Places SDK-----
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }
        // NEW MAP
        initializeHERESDK()

//        Paypal
        queue = Volley.newRequestQueue(this)

        binding.searchButton.setOnClickListener() {
            // Implement HERE SDK search suggestion logic here
            handleAddressInput(binding.tvAddress.text.toString())
        }

        setupRecyclerView()
        handleCloseCheckoutScreen()

        cartId = intent.getStringExtra("user_id") ?: return
        presenter.loadCartData(cartId)

        handlePlaceOrderButton()
    }

    private fun initializeHERESDK() {
        // Set your credentials for the HERE SDK.
        val accessKeyID = "r9rEHx6tUarZRTyRl5_1IA"
        val accessKeySecret = "Mx3jwrKn5HW6EowO5IdLT-LQOSfZpZsn-urW-EkgwjvAuRTe3X99lVJtDtGBlppBhGwbOg3OPoC6s-8FAtyu-g"
        val options = SDKOptions(accessKeyID, accessKeySecret)
        try {
            val context: Context = this
            SDKNativeEngine.makeSharedInstance(context, options)
            initializeSearchEngine()
            setupGeoCoordinates()
        } catch (e: InstantiationErrorException) {
            throw RuntimeException("Initialization of HERE SDK failed: " + e.error.name)
        }
    }

    private fun initializeSearchEngine() {
        try {
            searchEngine = SearchEngine()
        } catch (e: InstantiationErrorException) {
            Log.e("HERE SDK", "Search engine initialization failed.", e)
        }
    }

    private fun setupGeoCoordinates() {
        southWestCornerHCMC = GeoCoordinates(10.693360, 106.568604)
        northEastCornerHCMC = GeoCoordinates(10.880334, 106.826672)
        hcmcGeoBox = GeoBox(southWestCornerHCMC, northEastCornerHCMC)
        hcmcArea = TextQuery.Area(hcmcGeoBox)
    }

    private fun handleAddressInput(userInput: String) {
        val autoSuggestQuery = TextQuery(userInput, hcmcArea)
        val searchOptions = SearchOptions().apply {
            languageCode = LanguageCode.EN_US // TODO
            maxItems = 5
        }

        searchEngine?.suggest(autoSuggestQuery, searchOptions) { error, suggestions ->
            if (error != null) {
                Log.e("HERE SDK", "Autosuggest Error: ${error.name}")
                return@suggest
            }

//            val addresses = suggestions?.map { it.title + " - " + it.place?.address?.addressText }.orEmpty()
            val addresses = suggestions?.mapNotNull { it.place?.address?.addressText }.orEmpty()
            runOnUiThread {
                AlertDialog.Builder(this)
                    .setTitle("Select Address")
                    .setAdapter(AddressAdapter(this, addresses)) { dialog, which ->
                        selectAddressAndGeocode(addresses[which])
                    }
                    .show()
            }
        }
    }

    private fun selectAddressAndGeocode(selectedAddress: String) {
        val addressQuery = TextQuery(selectedAddress, hcmcArea)
        searchEngine?.search(addressQuery, SearchOptions()) { error, results ->
            if (error != null) {
                Log.e("HERE SDK", "Search Error: ${error.name}")
                return@search
            }

            results?.firstOrNull()?.geoCoordinates?.let { coordinates ->
                runOnUiThread {
                    mMap.clear()
                    val latLng = LatLng(coordinates.latitude, coordinates.longitude)
                    val markerOptions = MarkerOptions().position(latLng).title(selectedAddress)
                    mMap.addMarker(markerOptions)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    binding.tvAddress.setText(selectedAddress)

                    calculateDistance(defaultLatLng, latLng)
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val hoChiMinhCity = LatLng(10.776889, 106.700806)

        // Initialize or move the marker
        if (marker == null) {
            val markerOptions = MarkerOptions().position(hoChiMinhCity).title("Marker in Ho Chi Minh City").draggable(true)
            marker = mMap.addMarker(markerOptions)
        } else {
            marker?.position = hoChiMinhCity
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hoChiMinhCity, 10f))

        setupMapListeners()
    }

    private fun setupMapListeners() {
        mMap.setOnCameraMoveStartedListener {
            val scrollView = findViewById<LockableNestedScrollView>(R.id.scrollView)
            scrollView.setScrollingEnabled(false)
        }

        mMap.setOnCameraIdleListener {
            val scrollView = findViewById<LockableNestedScrollView>(R.id.scrollView)
            scrollView.setScrollingEnabled(true)
        }

        mMap.setOnMapClickListener { latLng ->
            // Move the existing marker to the clicked position
            marker?.position = latLng
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))

            // Use reverse geocoding to update the address and calculate distance
            updateAddressAndCalculateDistance(latLng)
        }
    }

    private fun updateAddressAndCalculateDistance(latLng: LatLng) {
        val geocoder = Geocoder(this@CheckoutActivity, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val address = addresses[0]
                    val addressText = (0..address.maxAddressLineIndex).joinToString(separator = " ") { address.getAddressLine(it) }
                    binding.tvAddress.setText(addressText)

                    // Calculate distance from the default location to this new location
                    calculateDistance(defaultLatLng, latLng)
                }
            }
        } catch (e: IOException) {
            Log.e("CheckoutActivity", "Unable to get address from the clicked location", e)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != AUTOCOMPLETE_REQUEST_CODE || data == null) return

        if (resultCode == RESULT_OK) {
            val place = Autocomplete.getPlaceFromIntent(data)
            binding.tvAddress.setText(place.address)
            binding.tvAddress.clearFocus()

            place.latLng?.let {
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(it, 15f)
                mMap.moveCamera(cameraUpdate)
            }
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            val status = Autocomplete.getStatusFromIntent(data)
            // Handle the error
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

    // --------------------PAYPAL--------------------
    private suspend fun getPayPalToken(url: String): String {
        return suspendCoroutine { continuation ->
            val jsonObjectRequest = object : JsonObjectRequest(
                Method.POST, url, null,
                Response.Listener { response ->
                    val jsonResponse = JSONObject(response.toString())
                    val accessToken = jsonResponse.getString("access_token")
//                    Toast.makeText(this@MainActivity, accessToken.toString(), Toast.LENGTH_SHORT).show()

                    continuation.resume(accessToken)
                },
                Response.ErrorListener { error ->
//                    Toast.makeText(this@MainActivity, "Failed", Toast.LENGTH_SHORT).show()
                    continuation.resumeWithException(error)
                }) {

                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Accept"] = "application/json"
                    headers["Accept-Language"] = "en_US"
                    headers["Authorization"] = "Basic " + Base64.encodeToString("$clientId:$clientSecret".toByteArray(), Base64.NO_WRAP)
                    return headers
                }

                override fun getBody(): ByteArray {
                    // Set the request body
                    return ("grant_type=client_credentials").toByteArray()
                }

                override fun getBodyContentType(): String {
                    // Set the request body content type
                    return "application/x-www-form-urlencoded"
                }
            }

            queue.add(jsonObjectRequest)
        }
    }

    private suspend fun createPayPalOrder(url: String, token: String, amountInput: Double): String {
        return suspendCoroutine { continuation ->
            val requestObject = JSONObject()
            requestObject.put("intent", "CAPTURE")
            val purchaseUnits = JSONObject()
            val amount = JSONObject()
            amount.put("currency_code", "USD")
            amount.put("value", "5.00")
//            amount.put("value", (amountInput + shippingFee).toString())
            purchaseUnits.put("amount", amount)
            val purchaseUnitsArray = JSONArray()
            purchaseUnitsArray.put(purchaseUnits)
            requestObject.put("purchase_units", purchaseUnitsArray)

            val jsonObjectRequest = object : JsonObjectRequest(
                Method.POST, url, requestObject,
                Response.Listener { response ->
                    val orderId = response.optString("id")
                    continuation.resume(orderId)
                },
                Response.ErrorListener { error ->
                    continuation.resumeWithException(error)
                }) {
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] = token
                    headers["Content-Type"] = "application/json"
                    return headers
                }
            }

            queue.add(jsonObjectRequest)
        }
    }

//    @OptIn(DelicateCoroutinesApi::class)
    private fun launchPayPalCheckout(orderId: String){
        val coreConfig = CoreConfig(
            clientId,
            environment = Environment.SANDBOX
        )

        val payPalNativeClient = PayPalNativeCheckoutClient(
            application = application,
            coreConfig = coreConfig,
            returnUrl = "com.zebrand.app1food30s://paypalpay"
        )

        payPalNativeClient.listener = object : PayPalNativeCheckoutListener {
            override fun onPayPalCheckoutStart() {
                // the PayPal paysheet is about to show up
//                Toast.makeText(this@CheckoutActivity, "STARTING", Toast.LENGTH_SHORT).show()
            }

            override fun onPayPalCheckoutSuccess(result: PayPalNativeCheckoutResult) {
                GlobalScope.launch {
                    try {
                        val tmp = capturePayPalOrder(token, orderId)
//                        Log.d("PayPalCheckoutPayPalCheckout", "Order captured successfully 2!")
                        Log.d("PayPalCheckoutPayPalCheckout", "Order captured successfully!")
                        val userDocRef = FirebaseFirestore.getInstance().document("accounts/$userId")
                        val note = binding.tvNote.text.toString()
                        val paymentMethod = binding.spinnerPayment.text.toString()
                        presenter.onPlaceOrderClicked(cartId, userDocRef, address, note, shippingFee, paymentMethod)
                    } catch (e: Exception) {
                        // Handle exceptions
                        e.printStackTrace()
                    }
                }
            }

            override fun onPayPalCheckoutFailure(error: PayPalSDKError) {
            }

            override fun onPayPalCheckoutCanceled() {
            }
        }

        payPalNativeClient.shippingListener = object : PayPalNativeShippingListener {
            override fun onPayPalNativeShippingAddressChange(
                actions: PayPalNativePaysheetActions,
                shippingAddress: PayPalNativeShippingAddress
            ) {
                actions.approve()
            }

            override fun onPayPalNativeShippingMethodChange(
                actions: PayPalNativePaysheetActions,
                shippingMethod: PayPalNativeShippingMethod
            ) {
                try {
                    actions.approve()
                } catch (e: Exception) {
                    actions.reject()
                }
            }
        }

        val request = PayPalNativeCheckoutRequest(orderId)
        payPalNativeClient.startCheckout(request)
    }

    private suspend fun capturePayPalOrder(token: String, orderId: String): String {
        return suspendCoroutine { continuation ->
            val url = url + orderId + "/capture"

            val jsonObjectRequest = object : JsonObjectRequest(
                Method.POST, url, null,
                Response.Listener { response ->
                    Log.d("CaptureResponse", response.toString())
//                    Toast.makeText(this, "Order captured successfully!", Toast.LENGTH_SHORT).show()
                    continuation.resume("SUCCESSFULL")
                },
                Response.ErrorListener { error ->
                    Log.e("CaptureError", error.toString())
//                    Toast.makeText(this, "Error capturing order: ${error.message}", Toast.LENGTH_SHORT).show()
                    continuation.resumeWithException(error)
                }) {
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] = token
                    headers["Content-Type"] = "application/json"
                    return headers
                }
            }

            queue.add(jsonObjectRequest)
        }
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
//            Toast.makeText(this@CheckoutActivity, "TEST", Toast.LENGTH_LONG).show()

            address = binding.tvAddress.text.toString()
            val note = binding.tvNote.text.toString()
            if (address.isEmpty()) {
                // Show error message
                binding.addressContainer.isErrorEnabled = true
                binding.addressContainer.error = getString(R.string.error_address_required)
                return@setOnClickListener
            } else {
                // Clear error
                binding.addressContainer.isErrorEnabled = false
                binding.addressContainer.error = null
            }

            val paymentMethod = binding.spinnerPayment.text.toString()
            if(paymentMethod == resources.getString(R.string.txt_cash_on_delivery)){
                val userDocRef = FirebaseFirestore.getInstance().document("accounts/$userId")
                presenter.onPlaceOrderClicked(cartId, userDocRef, address, note, shippingFee, paymentMethod)
            } else {
                GlobalScope.launch {
                    try {
                        token += getPayPalToken(urlToken)
                        orderId = createPayPalOrder(url, token, checkoutItemsAdapter.getPrice())
                        launchPayPalCheckout(orderId)
                    } catch (e: Exception) {
                        // Handle exceptions
                        e.printStackTrace()
                    }
                }
            }

        }
    }

    private fun Context.getConnectivityManager() = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

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
            binding.textViewAmount.text = Utils.formatPrice(0.0, this)
        }
    }

    private fun handleCloseCheckoutScreen() {
        binding.ivBack.root.setOnClickListener {
            finish()
        }
    }
}

