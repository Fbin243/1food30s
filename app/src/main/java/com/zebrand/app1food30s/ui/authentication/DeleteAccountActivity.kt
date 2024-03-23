package com.zebrand.app1food30s.ui.authentication

import android.app.Dialog
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Button
import androidx.appcompat.app.AppCompatDelegate
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.databinding.ActivityDeleteAccountBinding
import com.zebrand.app1food30s.databinding.ActivityManageOrderBinding
import com.zebrand.app1food30s.databinding.DialogDeleteAccountBinding

class DeleteAccountActivity : AppCompatActivity() {
    lateinit var binding: ActivityDeleteAccountBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteAccountBinding.inflate(layoutInflater)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
                // Dark mode is enabled, so force light mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
        setContentView(binding.root)

        events()

    }

    private fun events(){
        binding.confirmBtn.setOnClickListener {
            showCustomConfirmDialogBox()
        }
    }

    private fun showCustomConfirmDialogBox() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        val bindingSub: DialogDeleteAccountBinding = DialogDeleteAccountBinding.inflate(layoutInflater)
        dialog.setContentView(bindingSub.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val acceptBtn: Button = dialog.findViewById(R.id.cancelBtn)
        val cancel: Button = dialog.findViewById(R.id.saveBtn)

        bindingSub.content = resources.getString(R.string.txt_confirm_delete_account)

        acceptBtn.setOnClickListener {
            dialog.dismiss()
        }

        cancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}