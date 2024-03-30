package com.zebrand.app1food30s.utils

import android.app.Activity
import android.content.Context
import android.content.Intent

object GlobalUtils {
    fun myStartActivity(context: Context, cls: Class<*>) {
        val intent = Intent(context, cls)
        context.startActivity(intent)
    }
    fun myStartActivityFinish(context: Context, cls: Class<*>) {
        val intent = Intent(context, cls)
        context.startActivity(intent)
        if (context is Activity) {
            context.finish()
        }
    }
    fun myStartActivityFinishAffinity(context: Context, cls: Class<*>) {
        val intent = Intent(context, cls)
        context.startActivity(intent)
        if (context is Activity) {
            context.finishAffinity()
        }
    }

    fun resetMySharedPreferences(mySharedPreferences: MySharedPreferences){
        mySharedPreferences.setBoolean(SingletonKey.KEY_LOGGED, false)
        mySharedPreferences.setBoolean(SingletonKey.IS_ADMIN, false)
        mySharedPreferences.setBoolean(SingletonKey.KEY_REMEMBER_ME, false)
        mySharedPreferences.setString(
            SingletonKey.KEY_USER_ID,
            MySharedPreferences.defaultStringValue
        )
        mySharedPreferences.setString(
            SingletonKey.KEY_EMAIL,
            MySharedPreferences.defaultStringValue
        )
        mySharedPreferences.setString(
            SingletonKey.KEY_PASSWORD,
            MySharedPreferences.defaultStringValue
        )
    }
}