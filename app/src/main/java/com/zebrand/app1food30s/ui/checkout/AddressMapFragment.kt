package com.zebrand.app1food30s.ui.checkout

import android.content.Context
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.here.sdk.core.GeoBox
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.LanguageCode
import com.here.sdk.core.engine.SDKNativeEngine
import com.here.sdk.core.engine.SDKOptions
import com.here.sdk.core.errors.InstantiationErrorException
import com.here.sdk.search.SearchEngine
import com.here.sdk.search.SearchOptions
import com.here.sdk.search.TextQuery
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.AddressAdapter
import java.io.IOException
import java.util.Locale

class AddressMapFragment : Fragment(), OnMapReadyCallback {

    private var searchEngine: SearchEngine? = null
    private lateinit var tvAddress: EditText
    private lateinit var searchButton: ImageView

    private lateinit var southWestCornerHCMC: GeoCoordinates
    private lateinit var northEastCornerHCMC: GeoCoordinates
    private lateinit var hcmcGeoBox: GeoBox
    private lateinit var hcmcArea: TextQuery.Area

    private var marker: Marker? = null
    private lateinit var defaultLatLng: LatLng
    private lateinit var mMap: GoogleMap

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvAddress = view.findViewById(R.id.tvAddress)
        searchButton = view.findViewById(R.id.searchButton)
        defaultLatLng = LatLng(10.762622, 106.682171)  // University of Science

        initializeMap()
        initializeHERESDK()
        setupSearchButton()
    }

    fun setAddressText(text: String) {
        tvAddress.setText(text)
    }

    fun getAddressText(): String {
        return tvAddress.text.toString()
    }

    private fun initializeHERESDK() {
        val accessKeyID = "r9rEHx6tUarZRTyRl5_1IA"
        val accessKeySecret = "Mx3jwrKn5HW6EowO5IdLT-LQOSfZpZsn-urW-EkgwjvAuRTe3X99lVJtDtGBlppBhGwbOg3OPoC6s-8FAtyu-g"
        val options = SDKOptions(accessKeyID, accessKeySecret)
        try {
            val context: Context = requireContext()
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

    private fun initializeMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setupMapMarker()
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, 10f))
        setupMapListeners()
    }
    private fun setupMapMarker() {
        if (marker == null) {
            val markerOptions = MarkerOptions().position(defaultLatLng).title("Marker in Ho Chi Minh City").draggable(true)
            marker = mMap.addMarker(markerOptions)
        } else {
            marker?.position = defaultLatLng
        }
    }
    private fun setupMapListeners() {
        mMap.setOnMapClickListener { latLng ->
            updateMarkerAndAddress(latLng)
        }
    }

    private fun setupSearchButton() {
        searchButton.setOnClickListener {
            val userInput = tvAddress.text.toString()
            if (userInput.isNotEmpty()) {
                handleAddressInput(userInput)
            } else {
                Toast.makeText(context, "Please enter an address to search.", Toast.LENGTH_SHORT).show()
            }
        }
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
            updateUI {
                AlertDialog.Builder(requireContext())
                    .setTitle("Select Address")
                    .setAdapter(AddressAdapter(requireContext(), addresses)) { dialog, which ->
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
                val latLng = LatLng(coordinates.latitude, coordinates.longitude)
                updateUI {
                    tvAddress.setText(selectedAddress)
                    updateMarkerAndAddress(latLng, focusCamera = true)
//                    calculateDistance(defaultLatLng, latLng)
                }
            }
        }
    }

    private fun updateUI(action: () -> Unit) {
        if (isAdded) {
            activity?.runOnUiThread {
                action()
            }
        }
    }

    private fun updateMarkerAndAddress(latLng: LatLng, focusCamera: Boolean = true) { // TODO
        if (marker == null) {
            val markerOptions = MarkerOptions().position(latLng).title("Selected Location").draggable(true)
            marker = mMap.addMarker(markerOptions)
        } else {
            marker?.position = latLng
        }

//        if (focusCamera) {
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
//        }
        if (focusCamera) {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        }

        updateAddress(latLng)
    }

    private fun updateAddress(latLng: LatLng) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val address = addresses[0]
                    val addressText = (0..address.maxAddressLineIndex).joinToString(separator = " ") { address.getAddressLine(it) }
                    tvAddress.setText(addressText)

                    // Calculate distance from the default location to this new location
//                    calculateDistance(defaultLatLng, latLng)
                }
            }
        } catch (e: IOException) {
//            Log.e("CheckoutActivity", "Unable to get address from the clicked location", e)
        }
    }
}
