package com.zebrand.app1food30s.ui.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.ui.admin_statistics.AdminStatisticsActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Assuming you have a button in your activity_main.xml with the ID 'btn_view_statistics'
        val buttonViewStatistics: Button = findViewById(R.id.btn_view_statistics)
        buttonViewStatistics.setOnClickListener {
            // Intent to start AdminStatisticsActivity
            val intent = Intent(this, AdminStatisticsActivity::class.java)
            startActivity(intent)
        }
    }
}
