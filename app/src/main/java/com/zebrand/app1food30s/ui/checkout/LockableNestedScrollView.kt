package com.zebrand.app1food30s.ui.checkout

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.widget.NestedScrollView

class LockableNestedScrollView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {

    // Variable to keep track of whether the ScrollView should intercept touch events
    private var scrollable = true

    fun setScrollingEnabled(enabled: Boolean) {
        scrollable = enabled
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // Only intercept touch events if scrollable is true
        return if (scrollable) {
            super.onInterceptTouchEvent(ev)
        } else {
            false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        // Only handle touch events if scrollable is true
        return if (scrollable) {
            super.onTouchEvent(ev)
        } else {
            false
        }
    }
}
