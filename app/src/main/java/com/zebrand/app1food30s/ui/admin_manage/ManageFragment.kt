package com.zebrand.app1food30s.ui.admin_manage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.zebrand.app1food30s.databinding.FragmentManageBinding
import com.zebrand.app1food30s.ui.my_order.adapter.MyOrderViewPagerAdapter
import com.zebrand.app1food30s.ui.admin_manage.ManageViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator

class ManageFragment : Fragment() {
    private var _binding: FragmentManageBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewPager2Adapter: ManageViewPagerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentManageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setupEvents()
    }

    private fun init() {
        viewPager2Adapter = ManageViewPagerAdapter(childFragmentManager, lifecycle)
        binding.viewPager2.adapter = viewPager2Adapter
        binding.viewPager2.isUserInputEnabled = false
        binding.viewPager2.offscreenPageLimit = 3
        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            when (position) {
                0 -> tab.text = "Product"
                1 -> tab.text = "Category"
                2 -> tab.text = "Offer"
            }
        }.attach()
    }

    private fun setupEvents() {
//        binding.backIcon.root.setOnClickListener {
//            requireActivity().onBackPressed()
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
