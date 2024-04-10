package com.zebrand.app1food30s.ui.list_product

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.textfield.TextInputEditText
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.CategoryAdapter
import com.zebrand.app1food30s.adapter.ProductAdapter
import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.data.entity.Category
import com.zebrand.app1food30s.data.entity.Offer
import com.zebrand.app1food30s.data.entity.Product
import com.zebrand.app1food30s.databinding.FragmentListProductBinding
import com.zebrand.app1food30s.ui.product_detail.ProductDetailActivity
import com.zebrand.app1food30s.utils.Utils
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class ListProductFragment(
    private val hasBackBtn: Boolean = false,
    private val hasTitle: Boolean = false,
    private val hasLoading: Boolean = false
) : Fragment(), ListProductMVPView, SwipeRefreshLayout.OnRefreshListener {
    private lateinit var binding: FragmentListProductBinding
    private lateinit var listProductPresenter: ListProductPresenter
    private lateinit var db: AppDatabase
    private var isGrid: Boolean = false
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var gridLayoutManager: GridLayoutManager
    private lateinit var title: String
    private var idFilter: String? = null
    private lateinit var filterBy: String
    private lateinit var searchInput: TextInputEditText
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var categories: List<Category>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListProductBinding.inflate(inflater)
        db = AppDatabase.getInstance(requireContext())
        linearLayoutManager = LinearLayoutManager(requireContext())
        gridLayoutManager = GridLayoutManager(requireContext(), 2)
        listProductPresenter = ListProductPresenter(this, db)
        if(hasLoading) Utils.initSwipeRefreshLayout(binding.swipeRefreshLayout, this, resources)
        else binding.swipeRefreshLayout.isEnabled = false

        return binding.root
    }

    // Hàm nhận dữ liệu từ các activity, fragment khác
    fun setInfo(title: String, filterBy: String, idFilter: String? = null) {
        this.title = title
        this.filterBy = filterBy
        this.idFilter = idFilter
        Log.i("TAG123", "setInfo: $title $idFilter")
        handleDisplayTitle()
    }

    fun setSearchInput(searchInput: TextInputEditText) {
        this.searchInput = searchInput
    }

    fun initProductViewAll() {
        lifecycleScope.launch {
            listProductPresenter.getDataAndDisplay()
        }
    }

    fun initOffer() {
        lifecycleScope.launch {
            listProductPresenter.getDataAndDisplay()
            listProductPresenter.filterProductsByOffer(
                idFilter!!,
                binding.productRcv.adapter as ProductAdapter
            )
        }
    }

    fun initCategory(initialPosition: Int) {
        Log.i("TAG123", "initCategory: vi tri ban dau $initialPosition")
        lifecycleScope.launch {
            listProductPresenter.getDataAndDisplay()
            listProductPresenter.filterProductsByCategory(
                categories[initialPosition].id,
                binding.productRcv.adapter as ProductAdapter
            )
        }
        binding.textView.text = categories[initialPosition].name
    }

    fun refreshDataAndFilterByCategory() {
        refreshData()
        listProductPresenter.filterProductsByCategory(
            categories[0].id,
            binding.productRcv.adapter as ProductAdapter
        )
        binding.textView.text = categories[0].name
    }

    private fun refreshData() {
        listProductPresenter.refreshData(binding.productRcv.adapter as ProductAdapter)
    }

    fun initSearch() {
        lifecycleScope.launch {
            listProductPresenter.getDataAndDisplay()
            searchProductsByName("")
            Utils.hideShimmerEffect(binding.shimmerLayout, binding.textView)
            handleSearchInput()
        }
    }

    fun setCategoryAdapterAndCategories(
        categoryAdapter: CategoryAdapter,
        categories: List<Category>
    ) {
        categoryAdapter.onItemClick = { holder ->
            categoryAdapter.lastItemClicked?.disableUnderline()
            holder.enableUnderline()
            binding.textView.text = categories[holder.adapterPosition].name
            categoryAdapter.updateCurrentPosition(holder.adapterPosition)
            // Update UI by category
            listProductPresenter.filterProductsByCategory(
                categories[holder.adapterPosition].id,
                binding.productRcv.adapter as ProductAdapter
            )
        }
        this.categoryAdapter = categoryAdapter
        this.categories = categories
    }

    private fun handleSearchInput() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchProductsByName(s.toString())
            }
        })
    }

    private fun searchProductsByName(pattern: String) {
        val searchResultNumber = listProductPresenter.searchProductsByName(
            pattern,
            binding.productRcv.adapter as ProductAdapter
        )
        "$searchResultNumber Items Available".also { binding.textView.text = it }
    }

    private fun handleDisplayTitle() {
        if (hasBackBtn) {
            binding.backBtn.root.visibility = View.VISIBLE
            handleBackBtn()
        }

        if (hasTitle) {
            Utils.showShimmerEffect(binding.shimmerLayout, binding.textTitleView)
            binding.textTitleView.text = title
            Utils.hideShimmerEffect(binding.shimmerLayout, binding.textTitleView)
        } else {
            Utils.showShimmerEffect(binding.shimmerLayout, binding.textView)
            binding.textView.text = title
            if (title != "search") Utils.hideShimmerEffect(binding.shimmerLayout, binding.textView)
        }
    }

    private fun handleBackBtn() {
        binding.backBtn.root.setOnClickListener {
            requireActivity().finish()
        }
    }

    override fun handleChangeLayout(products: List<Product>) {
        setTypeDisplay(products)
        binding.gridBtn.setOnClickListener {
            isGrid = true
            binding.gridBtn.setImageResource(R.drawable.ic_active_grid)
            binding.linearBtn.setImageResource(R.drawable.ic_linear)
            binding.productRcv.layoutManager = gridLayoutManager
            changeLayout(products)
        }

        binding.linearBtn.setOnClickListener {
            isGrid = false
            binding.linearBtn.setImageResource(R.drawable.ic_active_linear)
            binding.gridBtn.setImageResource(R.drawable.ic_grid)
            binding.productRcv.layoutManager = linearLayoutManager
            changeLayout(products)
        }
    }


    private fun changeLayout(products: List<Product>) {
        setTypeDisplay(products)
        binding.productRcv.adapter?.notifyDataSetChanged()
    }

    private fun setTypeDisplay(products: List<Product>) {
        for (product in products) {
            product.isGrid = isGrid
        }
    }

    override fun showProducts(products: List<Product>, offers: List<Offer>) {
        Log.i("TAG123", "showProducts: TAO PRODUCT")
        binding.productRcv.layoutManager = if (isGrid) gridLayoutManager else linearLayoutManager
        val adapter = ProductAdapter(products, offers, mutableSetOf())
        adapter.onItemClick = { product ->
            openDetailProduct(product)
        }
        binding.productRcv.adapter = adapter
    }

    private fun openDetailProduct(product: Product) {
        val intent = Intent(requireContext(), ProductDetailActivity::class.java)
        intent.putExtra("idProduct", product.id)
        startActivity(intent)
    }

    override fun showShimmerEffectForProducts() {
        Utils.showShimmerEffect(binding.productShimmer, binding.productRcv)
    }

    override fun hideShimmerEffectForProducts() {
        Utils.hideShimmerEffect(binding.productShimmer, binding.productRcv)
    }

    override fun onRefresh() {
        refreshData()
        binding.swipeRefreshLayout.isRefreshing = false
    }
}