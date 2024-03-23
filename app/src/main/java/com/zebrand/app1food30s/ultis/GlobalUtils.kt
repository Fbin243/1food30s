package com.zebrand.app1food30s.ultis

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
}