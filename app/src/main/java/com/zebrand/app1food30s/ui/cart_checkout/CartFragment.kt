package com.zebrand.app1food30s.ui.cart_checkout

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.CartItemAdapter
import com.zebrand.app1food30s.data.model.Cart
import com.zebrand.app1food30s.data.model.CartItem
import com.zebrand.app1food30s.data.model.DetailedCartItem
import com.zebrand.app1food30s.databinding.FragmentCartBinding

class CartFragment : Fragment(), CartView {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CartItemAdapter
    private val db = FirebaseFirestore.getInstance()
    private val cartId = "mdXn8lvirHaAogStOY1K"
    private lateinit var presenter: CartPresenter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        presenter = CartPresenter(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        presenter.listenToCartChanges()
        handleCheckoutNavigation()
        // TODO
//        handleCloseCartScreen()
    }

    private fun setupRecyclerView() {
        adapter = CartItemAdapter(
            context = requireContext(),
            items = emptyList(),
            onItemDeleted = { cartItem ->
                cartItem.productId?.let { productRef ->
                    presenter.removeFromCart(productRef)
                }
            },
            onQuantityChanged = { cartItem ->
//                updateCartItem(cartItem)
            },
            onUpdateTotalPrice = { totalPrice ->
                binding.textViewAmount.text = getString(R.string.product_price_number, totalPrice)
            }
        )
        binding.cartItemsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.cartItemsRecyclerView.adapter = adapter
    }

    override fun displayCartItems(detailedCartItems: List<DetailedCartItem>) {
        _binding?.let {
            adapter.updateItems(detailedCartItems)
        }
    }

    override fun refreshCart(productRef: DocumentReference) {
        // Directly remove the item from the adapter
        adapter.removeItemByRef(productRef)
    }

    override fun displayError(error: String) {
        // Display error to user
    }

//    private fun removeFromCart(productRef: DocumentReference) {
//        val cartRef = db.collection("carts").document(cartId)
//
//        db.runTransaction { transaction ->
//            val snapshot = transaction.get(cartRef)
//            val cart = snapshot.toObject(Cart::class.java)
//            val items = cart?.items?.toMutableList() ?: mutableListOf()
//
//            // Remove the item with the specified product DocumentReference
//            items.removeAll { it.productId == productRef }
//
//            // Update the cart with the new list of items
//            cart?.items = items
//            transaction.set(cartRef, cart ?: return@runTransaction)
//
//            null // Indicate success but no result
//        }.addOnSuccessListener {
//            // Handle success, e.g., by showing a toast or updating the UI
//        }.addOnFailureListener { e ->
//            // Handle error
//            Log.e("CartFragment", "Error removing item from cart", e)
//        }
//    }

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
}
