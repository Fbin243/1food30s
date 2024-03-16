package com.zebrand.app1food30s.utils

object Utils {
    fun formatPrice(price: Double): String {
        return String.format("%.2f", price).replace(",", ".")
    }
}