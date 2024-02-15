package com.zebrand.app1food30s.ui.my_order

import android.animation.Animator
import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.databinding.ActivityMyOrderDetailsBinding

@SuppressLint("StaticFieldLeak")
lateinit var binding:ActivityMyOrderDetailsBinding
class MyOrderDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyOrderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lottieAnimation()
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