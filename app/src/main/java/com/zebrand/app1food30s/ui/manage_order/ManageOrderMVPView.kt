package com.zebrand.app1food30s.ui.manage_order

interface ManageOrderMVPView {
    fun getManageOrders()
    fun setManageOrderUI()

    fun showShimmerEffectForOrders(size: Int)
    fun hideShimmerEffectForOrders()
}