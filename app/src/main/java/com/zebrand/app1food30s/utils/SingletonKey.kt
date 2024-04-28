package com.zebrand.app1food30s.utils


object SingletonKey {
    const val KEY_LOGGED = "KEY_LOGGED" //boolean
    const val IS_ADMIN = "IS_ADMIN" // boolean
    const val KEY_REMEMBER_ME = "KEY_REMEMBER_ME" //boolean
    const val KEY_USER_ID = "KEY_USER_ID" //string
    const val KEY_EMAIL = "KEY_EMAIL" //string
    const val KEY_PASSWORD = "KEY_PASSWORD" //string
    const val KEY_LANGUAGE_CODE = "KEY_LANGUAGE_CODE" //string

    // Pending - Order accepted - On delivery - Delivered // Cancelled
    const val PENDING = "Pending"
    const val ORDER_ACCEPTED = "Order accepted"
    const val ON_DELIVERY = "On delivery"
    const val DELIVERED = "Delivered"
    const val CANCELLED = "Cancelled"

    //Paid - Unpaid
    const val PAID = "Paid"
    const val UNPAID = "Unpaid"
}
