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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.CartItemAdapter
import com.zebrand.app1food30s.data.Product

class CartFragment : Fragment() {

    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        Log.d("SharedViewModel", "SharedViewModel instance hash code in Fragment: ${sharedViewModel.hashCode()}")

        val recyclerView: RecyclerView = view.findViewById(R.id.cartItemsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize adapter once
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

        recyclerView.adapter = adapter

        sharedViewModel.cartItems.observe(viewLifecycleOwner) { items ->
//             Log.d("SharedViewModel", "Observing cart items: $items")
            adapter.updateItems(items)
            // TODO
            // updateTotalPrice(items)
        }

        // Move to CheckoutActivity
        val btnCheckout = view.findViewById<Button>(R.id.btnCheckout)
        btnCheckout.setOnClickListener {
            val intent = Intent(requireActivity(), CheckoutActivity::class.java)
            startActivity(intent)
        }
    }

//    private fun updateTotalPrice(items: List<Product>) {
//        val total = items.sumOf { it.price * it.quantity /* Make sure to include quantity in your Product model if you haven't already */ }
//        view?.findViewById<TextView>(R.id.textView_amount)?.text = getString(R.string.product_price_number, total)
//    }
}
