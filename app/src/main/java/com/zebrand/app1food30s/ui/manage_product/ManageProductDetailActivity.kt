package com.zebrand.app1food30s.ui.manage_product

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.zebrand.app1food30s.R
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView


class ManageProductDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_product_detail)

        val prices = resources.getStringArray(R.array.prices_array)
        val offers = resources.getStringArray(R.array.offers_array)

        val priceAdapter = ArrayAdapter(this, R.layout.dropdown_menu_popup_item, prices)
        val offerAdapter = ArrayAdapter(this, R.layout.dropdown_menu_popup_item, offers)

        val priceDropdown: AutoCompleteTextView = findViewById(R.id.price_dropdown)
        val offerDropdown: AutoCompleteTextView = findViewById(R.id.offer_dropdown)

        priceDropdown.setAdapter(priceAdapter)
        offerDropdown.setAdapter(offerAdapter)
    }
}