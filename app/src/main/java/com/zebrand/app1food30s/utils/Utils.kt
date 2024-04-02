package com.zebrand.app1food30s.utils

import android.graphics.Color
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable

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
}