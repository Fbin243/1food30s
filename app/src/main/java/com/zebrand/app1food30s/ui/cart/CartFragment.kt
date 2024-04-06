package com.zebrand.app1food30s.ui.cart

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
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
    private var debounceJob: Job? = null
    private lateinit var userId: String

    // TODO: not display cart screen before moving to log in
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Initialize preferences here as context is available and safe to use
        preferences = MySharedPreferences.getInstance(context)

        userId = preferences.getString(SingletonKey.KEY_USER_ID) ?: ""
        val defaultId = defaultStringValue

        // Check if the user is logged in before proceeding
        if (userId == defaultId) {
            // User is not logged in, navigate to LoginActivity
            val loginIntent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(loginIntent)
            return // Stop further execution of this function
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = MySharedPreferences.getInstance(requireContext())
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

        // TODO
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

    private fun updateQuantityWithDebounce(productRef: DocumentReference, newQuantity: Int) {
        // Cancel any existing job to ensure only the last update within the debounce period is processed
        debounceJob?.cancel()
        debounceJob = CoroutineScope(Dispatchers.Main).launch {
            delay(500) // Adjust the delay as needed
            presenter.updateCartItemQuantity(productRef, newQuantity)
        }
    }

    // presenter: load cart
    override fun loadCart(cartItems: List<CartItem>) {
        _binding?.let {
            adapter.loadItems(cartItems)
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
                // Now we pass the userId instead of a static cartId
                putExtra("user_id", userId)
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
