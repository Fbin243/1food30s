package com.zebrand.app1food30s.ui.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileAfterLoginBinding.inflate(inflater, container, false)
        events()

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

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
//
//        btnLogin.setOnClickListener {
//            // Intent to start SignUpActivity
//            val intent = Intent(activity, LoginActivity::class.java)
//            startActivity(intent)
//        }
//    }
}