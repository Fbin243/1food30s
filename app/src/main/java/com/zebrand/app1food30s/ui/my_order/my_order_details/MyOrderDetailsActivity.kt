package com.zebrand.app1food30s.ui.my_order.my_order_details

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.databinding.ActivityMyOrderDetailsBinding

class MyOrderDetailsActivity : AppCompatActivity() {
    lateinit var binding:ActivityMyOrderDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyOrderDetailsBinding.inflate(layoutInflater)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
                // Dark mode is enabled, so force light mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
        setContentView(binding.root)

        events()
        lottieAnimation()
    }

    private fun events(){
        binding.backIcon.root.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }

    private fun lottieAnimation(){
        // Start the animation
        binding.lottieSuccess.repeatCount = LottieDrawable.INFINITE

        binding.lottieSuccess.addAnimatorListener(object : Animator.AnimatorListener{
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {
                binding.lottieSuccess.pauseAnimation()

                binding.lottieSuccess.postDelayed({
                    binding.lottieSuccess.resumeAnimation()
                }, 5000)
            }

        })

        binding.lottieSuccess.playAnimation()
    }
}