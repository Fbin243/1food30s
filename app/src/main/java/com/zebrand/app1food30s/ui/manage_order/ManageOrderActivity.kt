package com.zebrand.app1food30s.ui.manage_order

import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.databinding.ActivityManageOrderBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ManageOrderActivity : AppCompatActivity() {
    lateinit var binding: ActivityManageOrderBinding
    private lateinit var botDialog: BottomSheetDialog
    lateinit var statusArr: Array<String>
    val customerArr = arrayOf("1- Bedroom", "2- Bedroom", "3- Bedroom")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageOrderBinding.inflate(layoutInflater)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
                // Dark mode is enabled, so force light mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
        setContentView(binding.root)

        init()

        events()


    }

    private fun init(){
        statusArr = resources.getStringArray(R.array.delivery_array)
    }

    private fun events() {
        binding.testOrderStatus.root.setOnClickListener {
            val intent = Intent(this, ManageOrderDetailsActivity::class.java)
            startActivity(intent)
        }

        binding.filterBtn.setOnClickListener {
            showBottomSheet()
        }
    }

    private fun showBottomSheet() {
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_manage_order, null)
        botDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        botDialog.setContentView(dialogView)

        // status dropdown
        val adapterStatus = ArrayAdapter(this, R.layout.item_drop_down_filter, statusArr)
        val tv_autoStatus: AutoCompleteTextView = dialogView.findViewById(R.id.spinnerStatus)
        tv_autoStatus.setAdapter(adapterStatus)
        tv_autoStatus.setOnItemClickListener { _, _, position, _ ->
            val selectedText = adapterStatus.getItem(position)
//            Toast.makeText(this, selectedText, Toast.LENGTH_LONG).show()
        }

        // customer dropdown
        val adapterCus = ArrayAdapter(this, R.layout.item_drop_down_filter, customerArr)
        val tv_autoCus: AutoCompleteTextView = dialogView.findViewById(R.id.spinnerCustomer)
        tv_autoCus.setAdapter(adapterCus)
        tv_autoCus.setOnItemClickListener { _, _, position, _ ->
            val selectedText = adapterCus.getItem(position)
//            Toast.makeText(this, selectedText, Toast.LENGTH_LONG).show()
        }

        // date picker
        val datePickerText: TextInputEditText = dialogView.findViewById(R.id.datePicker)
        val myCalendar = Calendar.getInstance()
        val datePicker = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val myFormat = "dd/MM/yyyy"
            val sdf = SimpleDateFormat(myFormat, Locale.UK)
            val formattedDate = sdf.format(myCalendar.time)
//            Log.d("dateABC", formattedDate)
            datePickerText.setText(formattedDate)
        }

        datePickerText.setOnClickListener {
            DatePickerDialog(
                this,
                R.style.MyDatePickerDialogStyle, datePicker, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        botDialog.show()
    }
}