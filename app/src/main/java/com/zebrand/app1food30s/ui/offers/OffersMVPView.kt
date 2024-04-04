package com.zebrand.app1food30s.ui.offers

import com.zebrand.app1food30s.data.entity.Offer

interface OffersMVPView {

    fun showShimmerEffectForOffers()
    fun hideShimmerEffectForOffers()
    fun showOffers(offers: List<Offer>)
}