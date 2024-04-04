package com.zebrand.app1food30s.utils

import android.graphics.Color
import android.os.Handler
import android.view.View
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.facebook.shimmer.ShimmerFrameLayout

object Utils {
    fun formatPrice(price: Double): String {
        return String.format("%.2f", price).replace(",", ".")
    }

    fun getShimmerDrawable(): ShimmerDrawable {
        return ShimmerDrawable().apply {
            setShimmer(
                Shimmer.ColorHighlightBuilder().setBaseColor(Color.parseColor("#f3f3f3"))
                    .setBaseAlpha(1.0f).setHighlightColor(Color.parseColor("#e7e7e7"))
                    .setHighlightAlpha(1.0f).setDropoff(50.0f).build()
            )
        }
    }

    fun showShimmerEffect(shimmer: ShimmerFrameLayout, view: View) {
        shimmer.startShimmer()
        shimmer.visibility = View.VISIBLE
        view.visibility = View.GONE
    }

    fun hideShimmerEffect(shimmer: ShimmerFrameLayout, view: View) {
        Handler().postDelayed({
            shimmer.stopShimmer()
            shimmer.visibility = View.GONE
            view.visibility = View.VISIBLE
        }, 1000)
    }
}