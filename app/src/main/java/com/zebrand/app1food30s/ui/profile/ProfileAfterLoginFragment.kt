package com.zebrand.app1food30s.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.ui.admin_stats.AdminStatsActivity

class ProfileAfterLoginFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile_after_login, container, false)

        // Set up the click listener for the layoutMyOrders
        val layoutMyOrders = view.findViewById<View>(R.id.layoutMyOrders)
        layoutMyOrders.setOnClickListener {
            // Navigate to AdminStatisticsActivity
            val intent = Intent(activity, AdminStatsActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}
