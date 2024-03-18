package com.zebrand.app1food30s.ui.cart

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.DocumentReference
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.CartItemAdapter
import com.zebrand.app1food30s.data.DetailedCartItem
import com.zebrand.app1food30s.databinding.FragmentCartBinding
import com.google.gson.Gson
import com.zebrand.app1food30s.ui.checkout.CheckoutActivity

class CartFragment : Fragment(), CartView {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CartItemAdapter
    private lateinit var presenter: CartPresenter
    private val cartId = "mdXn8lvirHaAogStOY1K"

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
            items = mutableListOf(),
            onItemDeleted = { cartItem ->
                cartItem.productId?.let { productRef ->
                    presenter.removeFromCart(productRef)
                }
            },
            onQuantityUpdated = { detailedCartItem, newQuantity ->
                detailedCartItem.productId?.let {
                    presenter.updateCartItemQuantity(it, newQuantity)
                }
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
        adapter.removeItemByRef(productRef)
    }

    override fun displayError(error: String) {
        // Display error to user
    }

    private fun handleCheckoutNavigation() {
        binding.btnCheckout.setOnClickListener {
            val cartSummary = presenter.getCartSummary() // Pair<List<String>, Double>
//            val intent = Intent(context, CheckoutActivity::class.java).apply {
//                val itemDescriptionsJson = Gson().toJson(cartSummary.first) // Convert list to JSON string
//                putExtra("item_descriptions", itemDescriptionsJson)
//                putExtra("total_price", cartSummary.second)
//            }
            val intent = Intent(context, CheckoutActivity::class.java).apply {
                putExtra("cart_id", cartId) // Assume cartId is a String representing the cart's ID
            }
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
