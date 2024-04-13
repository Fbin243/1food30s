package com.zebrand.app1food30s.ui.admin_manage

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.zebrand.app1food30s.ui.manage_category.ManageCategoryFragment
import com.zebrand.app1food30s.ui.manage_product.ManageProductFragment
import com.zebrand.app1food30s.ui.my_order.MyActiveOrderFragment
import com.zebrand.app1food30s.ui.my_order.MyPrevOrderFragment
import com.zebrand.app1food30s.ui.offers.ManageOfferFragment

class ManageViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ManageProductFragment()
            1 -> ManageCategoryFragment()
            2 -> ManageOfferFragment()
            else -> ManageProductFragment()
        }
    }
}

