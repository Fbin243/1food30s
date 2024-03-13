package com.zebrand.app1food30s.ui.cart_checkout

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.CartItemAdapter
import com.zebrand.app1food30s.data.Product
import com.zebrand.app1food30s.databinding.FragmentCartBinding

class CartFragment : Fragment() {

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeCartItems()
        handleCheckoutNavigation()
        // TODO
//        handleCloseCartScreen()
    }

    private fun setupRecyclerView() {
        val adapter = CartItemAdapter(
            context = requireContext(),
            items = emptyList(),
            onItemDeleted = { cartItem ->
                sharedViewModel.removeFromCart(cartItem)
            },
            onQuantityChanged = { cartItem ->
                sharedViewModel.updateCartItem(cartItem)
            }
        )
        binding.cartItemsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.cartItemsRecyclerView.adapter = adapter
    }

    private fun observeCartItems() {
        sharedViewModel.cartItems.observe(viewLifecycleOwner) { items ->
            (binding.cartItemsRecyclerView.adapter as CartItemAdapter).updateItems(items)
            updateTotalPrice(items)
        }
    }

    private fun handleCheckoutNavigation() {
        binding.btnCheckout.setOnClickListener {
            val intent = Intent(requireActivity(), CheckoutActivity::class.java)
            startActivity(intent)
        }
    }

//    private fun handleCloseCartScreen() {
//        binding.ivBack.root.setOnClickListener {
//            findNavController().navigateUp()
//        }
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateTotalPrice(items: List<CartItemAdapter.CartItem>) {
        val total = items.sumOf { it.product.price * it.quantity }
        binding.textViewAmount.text = getString(R.string.product_price_number, total)
    }
}
