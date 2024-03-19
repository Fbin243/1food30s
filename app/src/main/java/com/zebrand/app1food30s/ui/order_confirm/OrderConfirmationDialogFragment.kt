package com.zebrand.app1food30s.ui.order_confirm

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.zebrand.app1food30s.R

class OrderConfirmationDialogFragment : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate your custom layout that includes the image, text, and buttons
        return inflater.inflate(R.layout.fragment_order_confirmation_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up button listeners and handle actions like 'Go to Details' and 'Pay Now'
        view.findViewById<Button>(R.id.btnGoToDetails).setOnClickListener {
            // Handle 'Go to Details' action
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            val width = (resources.displayMetrics.widthPixels * 0.8).toInt() // 80% of screen width
            // The height will be wrapped based on the content, which will automatically adjust to the new width
            setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            // Since we don't have a direct proportion for height, it will automatically adjust based on the content and width.
        }
    }
}
