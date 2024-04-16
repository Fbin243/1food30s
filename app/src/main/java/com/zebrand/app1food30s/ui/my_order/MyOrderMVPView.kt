package com.zebrand.app1food30s.ui.my_order

interface MyOrderMVPView {
    fun setMyActiveOrderUI()
    fun setMyPrevOrderUI()

    fun showShimmerEffectForOrders()
    fun hideShimmerEffectForOrders()
}