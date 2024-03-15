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
import com.google.firebase.firestore.FirebaseFirestore
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.CartItemAdapter
import com.zebrand.app1food30s.data.model.Cart
import com.zebrand.app1food30s.data.model.CartItem
import com.zebrand.app1food30s.data.service.ProductService
import com.zebrand.app1food30s.databinding.FragmentCartBinding

class CartFragment : Fragment(), CartView {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CartItemAdapter
    private val db = FirebaseFirestore.getInstance()
    private val cartId = "mdXn8lvirHaAogStOY1K"
    private val productService = ProductService()
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
            getProductById = { productId, callback ->
                productService.getProductById(productId, callback)
            },
            onItemDeleted = { cartItem ->
                removeFromCart(cartItem.productId)
            },
            onQuantityChanged = { cartItem ->
                updateCartItem(cartItem)
            },
            onUpdateTotalPrice = { totalPrice ->
                binding.textViewAmount.text = getString(R.string.product_price_number, totalPrice)
            }
        )
        binding.cartItemsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.cartItemsRecyclerView.adapter = adapter
    }

    override fun displayCartItems(cartItems: List<CartItem>) {
        adapter.updateItems(cartItems)
    }

    override fun displayTotalPrice(totalPrice: Double) {
        val formattedPrice = String.format("$%.2f", totalPrice)
        view?.findViewById<TextView>(R.id.textView_amount)?.text = formattedPrice
    }

    override fun displayError(error: String) {
        // Display error to user
    }

//    private fun listenToCartChanges() {
//        // Log.d("Test00", "listenToCartChanges: Runs")
//        db.collection("carts").document(cartId)
//            .addSnapshotListener { snapshot, e ->
//                if (e != null) {
//                    // Handle error
//                    // Log.d("Test00", "listenToCartChanges: Error")
//                    return@addSnapshotListener
//                }
//                if (snapshot != null && snapshot.exists()) {
//                    // Log.d("Test00", "listenToCartChanges: No error")
//                    val cartItems = snapshot.toObject(Cart::class.java)?.items ?: listOf()
////                    Log.d("Test00", "snapshot: $snapshot")
//                    // Log.d("Test00", "CartFragment - cartItems: $cartItems")
//                    adapter.updateItems(cartItems)
//                    // updateTotalPrice(cartItems)
//                }
//            }
//    }

    private fun removeFromCart(productId: String) {
        val db = FirebaseFirestore.getInstance()
        val cartRef = db.collection("carts").document(cartId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(cartRef)
            val cart = snapshot.toObject(Cart::class.java)
            val items = cart?.items?.toMutableList() ?: mutableListOf()

            // Remove the item with the specified productId
            items.removeAll { it.productId == productId }

            // Update the cart with the new list of items
            cart?.items = items
            transaction.set(cartRef, cart ?: return@runTransaction)

            null // Indicate success but no result
        }.addOnSuccessListener {
            // Handle success, e.g., by showing a toast or updating the UI
        }.addOnFailureListener { e ->
            // Handle error
            Log.e("CartFragment", "Error removing item from cart", e)
        }
    }

    private fun updateCartItem(cartItem: CartItem) {
        // Firestore operation to update the item quantity in the cart
        val db = FirebaseFirestore.getInstance()
        val cartRef = db.collection("carts").document(cartId)

        // Begin a transaction to ensure atomic updates
        db.runTransaction { transaction ->
            val snapshot = transaction.get(cartRef)
            val cart = snapshot.toObject(Cart::class.java)
            val items = cart?.items?.toMutableList() ?: mutableListOf()

            val index = items.indexOfFirst { it.productId == cartItem.productId }
            if (index != -1) {
                items[index] = cartItem // Update the cart item
            }

            // Prepare updated cart data
            cart?.items = items
            cart?.let { transaction.set(cartRef, it) }

            null // Return null as this transaction does not return any result
        }.addOnSuccessListener {
            // Handle success
        }.addOnFailureListener { e ->
            // Handle failure
        }
    }

//    private fun observeTotalPrice() {
//        // Observe the totalPrice LiveData from ViewModel to get updates
//        productViewModel.totalPrice.observe(viewLifecycleOwner) { totalPrice ->
//            // Update the total price UI
//            binding.textViewAmount.text = getString(R.string.product_price_number, totalPrice)
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
