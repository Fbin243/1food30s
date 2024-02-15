package com.zebrand.app1food30s.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zebrand.adapter.CategoryAdapter
import com.zebrand.adapter.ProductApapter
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.databinding.FragmentHomeBinding
import com.zebrand.model.Category
import com.zebrand.model.Product

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var rcv: RecyclerView
    private lateinit var rcv2: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)
        val view = binding.root

        rcv = binding.cateRcv
        rcv.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        rcv.adapter = CategoryAdapter(getListCategories())

        rcv2 = binding.productRcv1
        rcv2.layoutManager = GridLayoutManager(requireContext(), 2)
        rcv2.adapter = ProductApapter(getListProducts())

        return view
    }

    private fun getListCategories(): List<Category> {
        var list = listOf<Category>()
        list = list + Category(R.drawable.cate1, "Appetizers")
        list = list + Category(R.drawable.cate1, "Burgers")
        list = list + Category(R.drawable.cate1, "Appetizers")
        list = list + Category(R.drawable.cate1, "Appetizers")
        list = list + Category(R.drawable.cate1, "Appetizers")
        list = list + Category(R.drawable.cate1, "Appetizers")
        list = list + Category(R.drawable.cate1, "Appetizers")
        list = list + Category(R.drawable.cate1, "Appetizers")
        return list
    }

    private fun getListProducts(): List<Product> {
        var list = listOf<Product>()
        list = list + Product(
            R.drawable.product1,
            "Sweet & Sour Chicken",
            "Sweet and sour chicken with crispy chicken, pineapple and delicious chilly sauce.",
            4.5
        )
        list = list + Product(
            R.drawable.product1,
            "Sweet & Sour Chicken",
            "Sweet and sour chicken with crispy chicken, pineapple and delicious chilly sauce.",
            4.5
        )
        list = list + Product(
            R.drawable.product1,
            "Sweet & Sour Chicken",
            "Sweet and sour chicken with crispy chicken, pineapple and delicious chilly sauce.",
            4.5
        )
        list = list + Product(
            R.drawable.product1,
            "Sweet & Sour Chicken",
            "Sweet and sour chicken with crispy chicken, pineapple and delicious chilly sauce.",
            4.5
        )
        return list
    }

}