package com.zebrand.app1food30s.ui.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.ui.admin_stats.AdminStatsActivity
import com.zebrand.app1food30s.databinding.FragmentProfileAfterLoginBinding
import com.zebrand.app1food30s.ui.authentication.DeleteAccountActivity
import com.zebrand.app1food30s.ui.authentication.LoginActivity
import com.zebrand.app1food30s.ui.main.MainActivity
import com.zebrand.app1food30s.ultis.FirebaseUtils
import com.zebrand.app1food30s.ultis.GlobalUtils
import com.zebrand.app1food30s.ultis.MySharedPreferences
import com.zebrand.app1food30s.ultis.SingletonKey


class ProfileAfterLoginFragment : Fragment() {
    private lateinit var binding: FragmentProfileAfterLoginBinding
    private var idUser: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Lấy idUser từ Bundle
        idUser = arguments?.getString("USER_ID")
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileAfterLoginBinding.inflate(inflater, container, false)
        events()

        // ========== Code này ở branch Hai3 (đã sửa lại dùng binding) =========
        // Set up the click listener for the layoutMyOrders
        binding.layoutMyOrders.setOnClickListener {
            // Navigate to AdminStatisticsActivity
            val intent = Intent(activity, AdminStatsActivity::class.java)
            startActivity(intent)
        }
        // ================================================================
        binding.layoutEditProfile.setOnClickListener {
            // Navigate to AdminStatisticsActivity
            val intent = Intent(activity, EditProfileActivity::class.java).apply {
                putExtra("USER_ID", idUser)
            }
            startActivity(intent)
        }

        return binding.root
    }

    private fun events() {
        binding.btnLogout.setOnClickListener {
            signOut()
        }

        binding.layoutDeleteAccount.setOnClickListener {
            GlobalUtils.myStartActivity(requireContext(), DeleteAccountActivity::class.java)
        }
    }

    fun signOut(){
        val mAuth = FirebaseUtils.fireAuth
        val mySharedPreferences = MySharedPreferences.getInstance(requireContext())

//            Sign out
        mAuth.signOut()

//            Clear data in local DB
        GlobalUtils.resetMySharedPreferences(mySharedPreferences)

//            Start activity
        GlobalUtils.myStartActivityFinishAffinity(requireContext(), MainActivity::class.java)
    }

}
