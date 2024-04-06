package com.zebrand.app1food30s.ui.offer_detail

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.squareup.picasso.Picasso
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.databinding.ActivityOfferDetailBinding
import com.zebrand.app1food30s.ui.list_product.ListProductFragment
import com.zebrand.app1food30s.ui.menu.MenuFragment
import com.zebrand.app1food30s.utils.Utils
import kotlinx.coroutines.launch

class OfferDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOfferDetailBinding
    private lateinit var db: AppDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOfferDetailBinding.inflate(layoutInflater)
        db = AppDatabase.getInstance(this)
        setContentView(binding.root)
        Utils.replaceFragment(ListProductFragment(hasLoading = true), supportFragmentManager, R.id.fragment_container)

        supportFragmentManager.registerFragmentLifecycleCallbacks(
            object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentViewCreated(
                    fm: FragmentManager,
                    f: Fragment,
                    v: View,
                    savedInstanceState: Bundle?
                ) {
                    val offerName = intent.getStringExtra("offerName")!!
                    val offerId = intent.getStringExtra("offerId")!!
                    val offerImg = intent.getStringExtra("offerImg")!!
                    Picasso.get().load(offerImg).placeholder(Utils.getShimmerDrawable()).into(binding.offerImg)
                    if (f is ListProductFragment) {
                        f.setInfo(offerName, "offer", offerId)
                        f.initOffer()
                    }
                }
            }, false
        )

        binding.backBtn.root.setOnClickListener {
            finish()
        }
    }
}