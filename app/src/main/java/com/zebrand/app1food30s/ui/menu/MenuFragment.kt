package com.zebrand.app1food30s.ui.menu

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.CategoryAdapter
import com.zebrand.app1food30s.adapter.ProductAdapter
import com.zebrand.app1food30s.data.Category
import com.zebrand.app1food30s.data.Offer
import com.zebrand.app1food30s.data.Product
import com.zebrand.app1food30s.databinding.FragmentMenuBinding
import kotlinx.coroutines.launch

class MenuFragment : Fragment(), MenuMVPView {
    private lateinit var binding: FragmentMenuBinding
    private lateinit var menuPresenter: MenuPresenter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMenuBinding.inflate(inflater)

        menuPresenter = MenuPresenter(this)
        lifecycleScope.launch { menuPresenter.getDataAndDisplay()}

        return binding.root
    }

    override fun handleChangeLayout(products: List<Product>, offers: List<Offer>) {
        binding.gridBtn.setOnClickListener {
            binding.gridBtn.setImageResource(R.drawable.ic_active_grid)
            binding.linearBtn.setImageResource(R.drawable.ic_linear)
            binding.productRcv.layoutManager = GridLayoutManager(requireContext(), 2)
            binding.productRcv.adapter = ProductAdapter(products, offers, true)
        }

        binding.linearBtn.setOnClickListener {
            binding.linearBtn.setImageResource(R.drawable.ic_active_linear)
            binding.gridBtn.setImageResource(R.drawable.ic_grid)
            binding.productRcv.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            binding.productRcv.adapter = ProductAdapter(products, offers, false)
        }

    }

    override fun showCategories(categories: List<Category>) {
        binding.cateRcv.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        val adapter = CategoryAdapter(categories)
        val primaryColor = Color.parseColor("#7F9839")
        adapter.onItemClick = { holder ->
            adapter.lastItemClicked?.cateTitle?.setTextColor(Color.parseColor("#FF3A3A4F"))
            adapter.lastItemClicked?.cateUnderline?.setBackgroundResource(0)
            holder.cateUnderline.setBackgroundResource(R.drawable.category_underline)
            holder.cateTitle.setTextColor(primaryColor)
        }
        binding.cateRcv.adapter = adapter
    }

    override fun showProducts(products: List<Product>, offers: List<Offer>) {
        binding.productRcv.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        binding.productRcv.adapter = ProductAdapter(products, offers, false)
    }
}