package com.zebrand.app1food30s.ui.offers

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.zebrand.app1food30s.adapter.OfferAdapter
import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.data.entity.Offer
import com.zebrand.app1food30s.databinding.FragmentOffersBinding
import com.zebrand.app1food30s.ui.offer_detail.OfferDetailActivity
import com.zebrand.app1food30s.utils.Utils
import kotlinx.coroutines.launch

class OffersFragment : Fragment(), OffersMVPView {
    private lateinit var binding: FragmentOffersBinding
    private lateinit var db: AppDatabase
    private lateinit var offersPresenter: OffersPresenter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOffersBinding.inflate(inflater)
        db = AppDatabase.getInstance(requireContext())
        offersPresenter = OffersPresenter(this, db)
        lifecycleScope.launch {
            offersPresenter.getDataAndDisplay()
        }

        return binding.root
    }

    override fun showOffers(offers: List<Offer>) {
        binding.offerRcv.layoutManager = LinearLayoutManager(requireContext())
        val adapater = OfferAdapter(offers)
        adapater.onItemClick = {
            openOfferDetailScreen(offers[it.adapterPosition])
        }
        binding.offerRcv.adapter = adapater
    }

    private fun openOfferDetailScreen(offer: Offer) {
        val intent = Intent(requireContext(), OfferDetailActivity::class.java)
        intent.putExtra("offerName", offer.name)
        intent.putExtra("offerId", offer.id)
        intent.putExtra("offerImg", offer.image)
        startActivity(intent)
    }

    override fun showShimmerEffectForOffers() {
        Utils.showShimmerEffect(binding.offerShimmer, binding.offerRcv)
    }

    override fun hideShimmerEffectForOffers() {
        Utils.hideShimmerEffect(binding.offerShimmer, binding.offerRcv)
    }
}