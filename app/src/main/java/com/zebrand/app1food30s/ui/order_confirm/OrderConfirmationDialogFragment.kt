package com.zebrand.app1food30s.ui.order_confirm

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.ui.my_order.my_order_details.MyOrderDetailsActivity

class OrderConfirmationDialogFragment : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate your custom layout that includes the image, text, and buttons
        return inflater.inflate(R.layout.fragment_order_confirmation_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val address = arguments?.getString("address")
        val addressTextView = view.findViewById<TextView>(R.id.address_text)
        addressTextView.text = address

        // Set up button listeners and handle actions like 'Go to Details' and 'Pay Now'
        view.findViewById<Button>(R.id.btnGoToDetails).setOnClickListener {
            val intentMyOrderDetailsActivity = Intent(requireContext(), MyOrderDetailsActivity::class.java)
            intentMyOrderDetailsActivity.putExtra("idOrder", activity?.intent?.getStringExtra("orderId"))
            dismiss()
            startActivity(intentMyOrderDetailsActivity)
        }

        // Set up the click listener for the close button
        val closeButton = view.findViewById<ImageView>(R.id.close_button)
        closeButton.setOnClickListener {
            // Dismiss the dialog
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            val width = (resources.displayMetrics.widthPixels * 0.9).toInt() // % of screen width
            // The height will be wrapped based on the content, which will automatically adjust to the new width
            setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            // Since we don't have a direct proportion for height, it will automatically adjust based on the content and width.
        }
    }
}
