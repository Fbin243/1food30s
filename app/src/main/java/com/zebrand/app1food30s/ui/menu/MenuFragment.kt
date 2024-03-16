package com.zebrand.app1food30s.ui.menu

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.CategoryAdapter
import com.zebrand.app1food30s.adapter.ProductAdapter
import com.zebrand.app1food30s.data.Category
import com.zebrand.app1food30s.data.Product
import com.zebrand.app1food30s.databinding.FragmentMenuBinding

class MenuFragment : Fragment() {
    private lateinit var binding: FragmentMenuBinding
    private lateinit var rcv: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMenuBinding.inflate(inflater)

        handleCategoryMenu()
        handleChangeLayout()
        handleDisplayProductList()

        return binding.root
    }

    private fun handleDisplayProductList() {
        rcv = binding.productRcv
        rcv.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
//        val adapter = ProductAdapter(getListProducts(), false)
//        rcv.adapter = adapter
    }

    private fun handleChangeLayout() {
        binding.gridBtn.setOnClickListener {
            binding.gridBtn.setImageResource(R.drawable.ic_active_grid)
            binding.linearBtn.setImageResource(R.drawable.ic_linear)
            rcv.layoutManager = GridLayoutManager(requireContext(), 2)
//            rcv.adapter = ProductAdapter(getListProducts())
        }

        binding.linearBtn.setOnClickListener {
            binding.linearBtn.setImageResource(R.drawable.ic_active_linear)
            binding.gridBtn.setImageResource(R.drawable.ic_grid)
            rcv.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
//            rcv.adapter = ProductAdapter(getListProducts(), false)
        }

    }

    private fun handleCategoryMenu() {
        rcv = binding.cateRcv
        rcv.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        val adapter = CategoryAdapter(getListCategories())

        val primaryColor = Color.parseColor("#7F9839")
        adapter.onItemClick = { holder ->
            adapter.lastItemClicked?.cateTitle?.setTextColor(Color.parseColor("#FF3A3A4F"))
            adapter.lastItemClicked?.cateUnderline?.setBackgroundResource(0)
            holder.cateUnderline.setBackgroundResource(R.drawable.category_underline)
            holder.cateTitle.setTextColor(primaryColor)
        }
        rcv.adapter = adapter
    }

    private fun getListCategories(): List<Category> {
        var list = listOf<Category>()
//        list = list + Category(R.drawable.cate1, "Appetizers")
//        list = list + Category(R.drawable.cate1, "Burgers")
//        list = list + Category(R.drawable.cate1, "Appetizers")
//        list = list + Category(R.drawable.cate1, "Appetizers")
//        list = list + Category(R.drawable.cate1, "Appetizers")
//        list = list + Category(R.drawable.cate1, "Appetizers")
//        list = list + Category(R.drawable.cate1, "Appetizers")
//        list = list + Category(R.drawable.cate1, "Appetizers")
        return list
    }

    private fun getListProducts(): List<Product> {
        var list = listOf<Product>()
//        list = list + Product(
//            R.drawable.product1,
//            "Sweet & Sour Chicken",
//            "Sweet and sour chicken with crispy chicken, pineapple and delicious chilly sauce.",
//            4.5
//        )
        return list
    }
}