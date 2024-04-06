package com.zebrand.app1food30s.ui.offers

import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.utils.FirebaseService
import kotlinx.coroutines.coroutineScope

class OffersPresenter(private val view: OffersMVPView, private val db: AppDatabase) {
    suspend fun getDataAndDisplay() {
        coroutineScope {
            view.showShimmerEffectForOffers()
            val offers = FirebaseService.getListOffers(db)
            view.showOffers(offers)
            view.hideShimmerEffectForOffers()
        }
    }

}