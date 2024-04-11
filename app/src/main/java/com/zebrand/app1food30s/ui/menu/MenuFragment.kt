package com.zebrand.app1food30s.ui.menu

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.CategoryAdapter
import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.data.entity.Category
import com.zebrand.app1food30s.data.entity.Product
import com.zebrand.app1food30s.databinding.FragmentMenuBinding
import com.zebrand.app1food30s.ui.product_detail.ProductDetailActivity
import com.zebrand.app1food30s.ui.wishlist.WishlistMVPView
import com.zebrand.app1food30s.ui.wishlist.WishlistPresenter
import com.zebrand.app1food30s.ui.wishlist.WishlistRepository
import com.zebrand.app1food30s.utils.MySharedPreferences
import com.zebrand.app1food30s.utils.SingletonKey
import kotlinx.coroutines.launch
import com.zebrand.app1food30s.ui.list_product.ListProductFragment
import com.zebrand.app1food30s.ui.search.SearchActivity
import com.zebrand.app1food30s.utils.Utils
import kotlinx.coroutines.launch

class MenuFragment(private val calledFromActivity: Boolean = false) : Fragment(), MenuMVPView,
    SwipeRefreshLayout.OnRefreshListener {
    private lateinit var binding: FragmentMenuBinding
    private lateinit var menuPresenter: MenuPresenter
    private lateinit var wishlistPresenter: WishlistPresenter
    private lateinit var db: AppDatabase
    private var categoryId: String? = null
    private var adapterPosition: Int = 0
    private lateinit var fragment: ListProductFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMenuBinding.inflate(inflater)
        db = AppDatabase.getInstance(requireContext())
        menuPresenter = MenuPresenter(this, db)

        // Make function reloading data when swipe down
        Utils.initSwipeRefreshLayout(binding.swipeRefreshLayout, this, resources)
        Utils.replaceFragment(ListProductFragment(), childFragmentManager, R.id.fragment_container)

        childFragmentManager.registerFragmentLifecycleCallbacks(
            object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentViewCreated(
                    fm: FragmentManager,
                    f: Fragment,
                    v: View,
                    savedInstanceState: Bundle?
                ) {
                    if (f is ListProductFragment) {
                        f.setInfo("", "category")
                        lifecycleScope.launch {
                            menuPresenter.getDataAndDisplay()
                            val categories = menuPresenter.getAllCategories()
                            f.setCategoryAdapterAndCategories(
                                binding.cateRcv.adapter as CategoryAdapter,
                                categories
                            )
                            f.initCategory(if (calledFromActivity) adapterPosition else 0)
                            fragment = f
                        }
                    }
                }
            }, false
        )

        handleOpenSearchScreen()

        return binding.root
    }

    override fun showCategories(categories: List<Category>) {
        binding.cateRcv.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        val adapter = CategoryAdapter(categories, true, adapterPosition)
        binding.cateRcv.scrollToPosition(adapterPosition)
        binding.cateRcv.adapter = adapter
    }

    override fun showShimmerEffectForCategories() {
        Utils.showShimmerEffect(binding.cateShimmer, binding.cateRcv)
    }

    override fun hideShimmerEffectForCategories() {
        Utils.hideShimmerEffect(binding.cateShimmer, binding.cateRcv)
    }

    override fun onRefresh() {
        lifecycleScope.launch {
            val categoryAdapter = binding.cateRcv.adapter as CategoryAdapter
            menuPresenter.refreshData(categoryAdapter)
            binding.cateRcv.scrollToPosition(0)
            (binding.cateRcv.adapter as CategoryAdapter).updateCurrentPosition(0)
            fragment.refreshDataAndFilterByCategory()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    fun changeHeaderOfFragment() {
        binding.backFromMenu.visibility = View.VISIBLE
        binding.backFromMenu.setOnClickListener {
            requireActivity().finish()
        }
    }

    fun saveCategoryIdAndAdapterPosition(categoryId: String, adapterPosition: Int) {
        this.categoryId = categoryId
        this.adapterPosition = adapterPosition
    }

    private fun handleOpenSearchScreen() {
        binding.searchButton.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            startActivity(intent)
        }
    }
}