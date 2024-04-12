package com.zebrand.app1food30s.ui.checkout

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.CheckoutItemsAdapter
import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.data.entity.CartItem
import com.zebrand.app1food30s.databinding.ActivityCheckoutBinding
import com.zebrand.app1food30s.ui.cart.CartRepository
import com.zebrand.app1food30s.ui.main.MainActivity
import com.zebrand.app1food30s.utils.MySharedPreferences
import com.zebrand.app1food30s.utils.SingletonKey
import java.io.IOException
import java.util.Locale

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            Places.initialize(applicationContext, "AIzaSyBQFpHSKFfBqU9XM8ZFGOEHPGyMkh6iCZk")
        }

        binding.tvAddress.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                // Launch the Autocomplete intent when the user focuses on the input
                val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
                // Full screen search
                // Return the place ID, name, latitude/longitude, and address
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

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

//        val sydney = LatLng(-34.0, 151.0)
        val hoChiMinhCity = LatLng(10.776889, 106.700806)
        val marker = mMap.addMarker(MarkerOptions().position(hoChiMinhCity).title("Marker in Ho Chi Minh City").draggable(true))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hoChiMinhCity, 10f))

        // Add a click listener for the map
        mMap.setOnMapClickListener { latLng ->
            // Move the marker to the clicked position
            if (marker != null) {
                marker.position = latLng
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

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE && resultCode == RESULT_OK) {
            val place = Autocomplete.getPlaceFromIntent(data!!)
            // Get the address
            binding.tvAddress.setText(place.address)
            binding.tvAddress.clearFocus()

            val latLng = place.latLng
            // Use latitude and longitude to update the map's location
            latLng?.let { CameraUpdateFactory.newLatLngZoom(it, 15f) }?.let { mMap.moveCamera(it) }
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            // Handle the error
            val status = Autocomplete.getStatusFromIntent(data!!)
            Log.i("CheckoutActivity", status.statusMessage ?: "Autocomplete error")
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
            val userDocRef = FirebaseFirestore.getInstance().document("accounts/$userId")
            presenter.onPlaceOrderClicked(cartId, userDocRef, address, note)
        }
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

