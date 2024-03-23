package com.zebrand.app1food30s.ultis

import android.content.Context
import android.content.SharedPreferences

class MySharedPreferences private constructor(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun setString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun getString(key: String): String? {
        return sharedPreferences.getString(key, defaultStringValue)
    }

    fun setBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String): Boolean {
        return sharedPreferences.getBoolean(key, defaultBooleanValue)
    }

    companion object {
        const val PREF_NAME = "MySharedPreferences"
        const val defaultStringValue = "DEFAULT"
        const val defaultBooleanValue = false

        @Volatile
        private var instance: MySharedPreferences? = null

        fun getInstance(context: Context): MySharedPreferences {
            return instance ?: synchronized(this) {
                instance ?: MySharedPreferences(context).also { instance = it }
            }
        }
    }
}
