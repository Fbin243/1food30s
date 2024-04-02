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
import com.zebrand.app1food30s.data.entity.DetailedCartItem
import com.zebrand.app1food30s.adapter.CartAdapter
import com.zebrand.app1food30s.data.DetailedCartItem
import com.zebrand.app1food30s.databinding.FragmentCartBinding
import com.zebrand.app1food30s.ui.checkout.CheckoutActivity
import com.zebrand.app1food30s.ultis.MySharedPreferences
import com.zebrand.app1food30s.ultis.SingletonKey

class CartFragment : Fragment(), CartMVPView {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CartAdapter
    private lateinit var presenter: CartPresenter
    private lateinit var preferences: MySharedPreferences

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

        val userId = preferences.getString(SingletonKey.KEY_USER_ID) ?: ""
        if (userId.isNotBlank()) {
            presenter = CartPresenter(this, userId)
            presenter.listenToCartChanges()
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
