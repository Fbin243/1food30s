package com.zebrand.app1food30s.ui.cart

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentReference
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.data.entity.CartItem
import com.zebrand.app1food30s.adapter.CartAdapter
import com.zebrand.app1food30s.databinding.FragmentCartBinding
import com.zebrand.app1food30s.ui.authentication.LoginActivity
import com.zebrand.app1food30s.ui.checkout.CheckoutActivity
import com.zebrand.app1food30s.utils.MySharedPreferences
import com.zebrand.app1food30s.utils.MySharedPreferences.Companion.defaultStringValue
import com.zebrand.app1food30s.utils.SingletonKey
import com.zebrand.app1food30s.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CartFragment : Fragment(), CartMVPView {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CartAdapter
    private lateinit var presenter: CartPresenter
    private lateinit var preferences: MySharedPreferences
    //    private var debounceJob: Job? = null
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = MySharedPreferences.getInstance(requireContext())
        userId = preferences.getString(SingletonKey.KEY_USER_ID) ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        if (userId.isNotBlank()) {
            presenter = CartPresenter(this, userId, requireContext())
//          presenter.listenToCartChanges()
//            Log.d("Test00", "onViewCreated: loadCart()")
//            presenter.loadCart()
        }

        handleCheckoutNavigation(userId)

//        handleCloseCartScreen()
    }

    private fun setupRecyclerView() {
        adapter = CartAdapter(
            context = requireContext(),
            items = mutableListOf(),
            onItemDeleted = { cartItem ->
                cartItem.productId?.let { productRef ->
                    presenter.removeFromCart(productRef)
                }
            },
            onQuantityUpdated = { detailedCartItem, newQuantity ->
                detailedCartItem.productId?.let { productRef ->
//                    updateQuantityWithDebounce(productRef, newQuantity)
                    presenter.updateCartItemQuantity(productRef, newQuantity)
                }
            },
            onUpdateTotalPrice = { totalPrice ->
                binding.textViewAmount.text = getString(R.string.product_price_number, totalPrice)
            }
        )
        binding.cartItemsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.cartItemsRecyclerView.adapter = adapter
    }

    override fun displayEmptyCart() {
        if (isAdded && !isRemoving && !requireActivity().isFinishing) {
            _binding?.let {
                binding.emptyCartTextView.visibility = View.VISIBLE // Show the empty cart message
                binding.cartView.visibility = View.GONE // Hide the RecyclerView
            }
        }
    }

    override fun showShimmerEffectForCart() {
        if(_binding == null) return
        Utils.showShimmerEffect(_binding!!.cartViewShimmer, _binding!!.cartView)
    }

    override fun hideShimmerEffectForCart() {
        if(_binding == null) return
        Utils.hideShimmerEffect(_binding!!.cartViewShimmer, _binding!!.cartView, false)
    }

//    private fun updateQuantityWithDebounce(productRef: DocumentReference, newQuantity: Int) {
//        // Cancel any existing job to ensure only the last update within the debounce period is processed
//        debounceJob?.cancel()
//        debounceJob = CoroutineScope(Dispatchers.Main).launch {
//            delay(500) // Adjust the delay as needed
//            presenter.updateCartItemQuantity(productRef, newQuantity)
//        }
//    }

    // presenter: load cart
    // only called when some items are found
    override fun loadCart(cartItems: List<CartItem>) {
        _binding?.let {
            adapter.loadItems(cartItems)

            binding.emptyCartTextView.visibility = View.GONE
            binding.cartView.visibility = View.VISIBLE
        }
    }

    // presenter: listen to changes
    override fun displayCartItems(cartItems: List<CartItem>) {
        _binding?.let {
            adapter.updateItems(cartItems)
        }
    }

    override fun refreshCart(productRef: DocumentReference) {
        adapter.removeItemByRef(productRef)
    }

    override fun displayError(error: String) {
        context?.let {
            Toast.makeText(it, error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleCheckoutNavigation(userId: String) {
        binding.btnCheckout.setOnClickListener {
            val intent = Intent(context, CheckoutActivity::class.java).apply {
                // TODO
                putExtra("user_id", userId)
            }
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
