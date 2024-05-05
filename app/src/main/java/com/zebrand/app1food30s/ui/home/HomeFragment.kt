package com.zebrand.app1food30s.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.ui.chat.ChatActivity
import com.zebrand.app1food30s.adapter.CategoryAdapter
import com.zebrand.app1food30s.adapter.OfferAdapter
import com.zebrand.app1food30s.adapter.ProductAdapter
import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.data.entity.Category
import com.zebrand.app1food30s.data.entity.Chat
import com.zebrand.app1food30s.data.entity.Offer
import com.zebrand.app1food30s.data.entity.Product
import com.zebrand.app1food30s.databinding.FragmentHomeBinding
import com.zebrand.app1food30s.ui.authentication.LoginActivity
import com.zebrand.app1food30s.ui.menu.MenuActivity
import com.zebrand.app1food30s.ui.offer_detail.OfferDetailActivity
import com.zebrand.app1food30s.ui.product_detail.ProductDetailActivity
import com.zebrand.app1food30s.ui.product_view_all.ProductViewAllActivity
import com.zebrand.app1food30s.ui.search.SearchActivity
import com.zebrand.app1food30s.ui.wishlist.WishlistActivity
import com.zebrand.app1food30s.ui.wishlist.WishlistMVPView
import com.zebrand.app1food30s.ui.wishlist.WishlistPresenter
import com.zebrand.app1food30s.ui.wishlist.WishlistRepository
import com.zebrand.app1food30s.utils.MySharedPreferences
import com.zebrand.app1food30s.utils.SingletonKey
import com.zebrand.app1food30s.utils.Utils
import com.zebrand.app1food30s.utils.Utils.hideShimmerEffect
import com.zebrand.app1food30s.utils.Utils.showShimmerEffect
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class HomeFragment : Fragment(), HomeMVPView, WishlistMVPView,
    SwipeRefreshLayout.OnRefreshListener {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var homePresenter: HomePresenter
    private lateinit var db: AppDatabase
    private lateinit var wishlistPresenter: WishlistPresenter
    private var currentProducts: List<Product> = emptyList()
    private var wishlistedProductIds: MutableSet<String> = mutableSetOf()
    private lateinit var preferences: MySharedPreferences
    private lateinit var userId: String
    private lateinit var defaultUserId: String
    private lateinit var mySharedPreferences: MySharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)
        db = AppDatabase.getInstance(requireContext())
        preferences = context?.let { MySharedPreferences.getInstance(it) }!!
        userId = preferences.getString(SingletonKey.KEY_USER_ID) ?: "Default Value"
        defaultUserId = MySharedPreferences.defaultStringValue
        mySharedPreferences = MySharedPreferences.getInstance(requireContext())
        val isLogin = mySharedPreferences.getBoolean(SingletonKey.KEY_LOGGED)

        homePresenter = HomePresenter(this, db)

        // TODO
//        val userId = SingletonKey.KEY_USER_ID
//        Log.d("Test00", "onCreateView: $userId")

//        val mySharedPreferences = context?.let { MySharedPreferences.getInstance(it) }
//        val userId = mySharedPreferences?.getString(SingletonKey.KEY_USER_ID) ?: "Default Value"
        val wishlistRepository = WishlistRepository(userId)
        wishlistPresenter = WishlistPresenter(this, wishlistRepository)
//        Log.d("Test00", "onCreateView: fetchAndUpdateWishlistState()")

        lifecycleScope.launch {
            homePresenter.getDataAndDisplay()
            handleOpenSearchScreen()
        }

        // Make function reloading data when swipe down
        Utils.initSwipeRefreshLayout(binding.swipeRefreshLayout, this, resources)

        handleOpenSearchScreen()
        handleDisplayWishlistActivity()

        binding.ivChatScreen.setOnClickListener{
            if(isLogin){
                val intent = Intent(requireContext(), ChatActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
            }

        }

        setupChatListener(userId)

        return binding.root
    }

    private fun handleDisplayWishlistActivity() {
        binding.ivWishlistScreen.setOnClickListener {
            val defaultId = MySharedPreferences.defaultStringValue
            if (userId == defaultId) {
                // User is not logged in, navigate to LoginActivity
                val loginIntent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(loginIntent)
                return@setOnClickListener // Stop further execution of this function
            }

            val intent = Intent(requireContext(), ProductViewAllActivity::class.java)
            intent.putExtra("filterBy", "wishlist")
            intent.putExtra("title", "Wishlist")
            startActivity(intent)
        }
    }

    private fun setupChatListener(chatId: String) {
        val chatsCollection = FirebaseFirestore.getInstance().collection("chats")
        val chatQuery = chatsCollection.whereEqualTo("idBuyer", chatId)
        chatQuery.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("ChatActivity", "Listen failed.", e)
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                val document = snapshot.documents.first()
                val updatedChat = document.toObject(Chat::class.java)
                updatedChat?.let {
                    if(!it.seenByBuyer){
                        binding.ivChatScreen.setImageResource(R.drawable.chat_round_unread_svgrepo_com)
                    } else {
                        binding.ivChatScreen.setImageResource(R.drawable.ic_chat)
                    }
                }
            }
        }
    }

    override fun fetchAndUpdateWishlistState(callback: () -> Unit) {
        wishlistPresenter.fetchAndUpdateWishlistState(callback)
        // then: view.refreshWishlistState(wishlistedProductIds)
    }

    override fun refreshWishlistState(wishlistedProductIds: Set<String>) {
        this.wishlistedProductIds = wishlistedProductIds.toMutableSet()
//        val thisWishlistedProductIds = this.wishlistedProductIds
//        Log.d("Test00", "refreshWishlistState: $thisWishlistedProductIds")
        updateAdaptersWithWishlistState() // Update your UI accordingly
    }

    private fun updateAdaptersWithWishlistState() {
        (binding.productRcv1.adapter as? ProductAdapter)?.updateWishlistState(wishlistedProductIds)
        (binding.productRcv2.adapter as? ProductAdapter)?.updateWishlistState(wishlistedProductIds)
    }

    // Implementation of WishlistMVPView methods
    override fun updateWishlistItemStatus(product: Product, isAdded: Boolean) {
        // Update the set of wishlisted product IDs based on the action
        val productId = product.id
//        Log.d("Test00", "updateWishlistItemStatus: $productId")
        if (isAdded) {
//            Log.d("Test00", "updateWishlistItemStatus: added")
            wishlistedProductIds.add(productId)
        } else {
//            Log.d("Test00", "updateWishlistItemStatus: removed")
            wishlistedProductIds.remove(productId)
        }
        // updated correctly
//        Log.d("Test00", "updateWishlistItemStatus: $wishlistedProductIds")
        updateAdaptersWishlistProductIds()

        // Notify all adapters about the update
        updateProductInAllAdapters(productId)
        // updateProductInAllAdapters(product.id, isAdded)
    }

    private fun updateAdaptersWishlistProductIds() {
        (binding.productRcv1.adapter as? ProductAdapter)?.updateWishlistProductIds(
            wishlistedProductIds
        )
        (binding.productRcv2.adapter as? ProductAdapter)?.updateWishlistProductIds(
            wishlistedProductIds
        )
    }

    //    private fun updateProductInAllAdapters(productId: String, isWishlisted: Boolean)
    private fun updateProductInAllAdapters(productId: String) {
        val adapters = listOfNotNull(
            binding.productRcv1.adapter as? ProductAdapter,
            binding.productRcv2.adapter as? ProductAdapter
        )

//        Log.d("Test00", "updateProductInAllAdapters: $adapters")

        adapters.forEach { adapter ->
            val index = adapter.products.indexOfFirst { it.id == productId }
            if (index != -1) {
//                Log.d("Test00", "updateProductInAllAdapters: notified")
                adapter.notifyItemChanged(index)
            }
        }
    }

    override fun onResume() {
        super.onResume()
//        Log.d("Test00", "onResume: ")
        wishlistPresenter.fetchAndUpdateWishlistState {
            // Optional: Do something after wishlist state is updated, if necessary
        }
//        refreshWishlistState(wishlistedProductIds)
        binding.searchInput.clearFocus()
    }

    // ============= DƯỚI NÀY LÀ HÀM CỦA T
    private fun handleOpenSearchScreen() {
        binding.searchInput.setOnFocusChangeListener { _, focus ->
            if (focus) {
                val intent = Intent(requireContext(), SearchActivity::class.java)
                startActivity(intent)
            }
        }
        binding.searchInput.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            startActivity(intent)
        }
    }

    private fun openMenuActivityWithCategory(categoryId: String, adapterPosition: Int) {
        val intent = Intent(requireContext(), MenuActivity::class.java)
        intent.putExtra("categoryId", categoryId)
        intent.putExtra("adapterPosition", adapterPosition)
        startActivity(intent)
    }

    override fun showCategories(categories: List<Category>) {
        binding.cateRcv.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        val adapter = CategoryAdapter(categories)
        adapter.onItemClick = { holder ->
            openMenuActivityWithCategory(
                categories[holder.adapterPosition].id,
                holder.adapterPosition
            )
        }
        binding.cateRcv.adapter = adapter
        binding.btn.root.setOnClickListener {
            openMenuActivityWithCategory(categories[0].id, 0)
        }
    }

    override fun showProductsLatestDishes(
        products: MutableList<Product>,
        offers: MutableList<Offer>
    ) {
        for (product in products) {
            product.isGrid = true
        }
        currentProducts = products
        binding.productRcv1.layoutManager = GridLayoutManager(requireContext(), 2)
        val adapter = ProductAdapter(products.take(4).toMutableList(), offers, wishlistedProductIds)
        addCallBacksForAdapter(adapter)
        binding.productRcv1.adapter = adapter
        handleOpenProductViewAll(binding.btn1.root, true, binding.textView1.text.toString())
    }

    override fun showProductsBestSeller(
        products: MutableList<Product>,
        offers: MutableList<Offer>
    ) {
        currentProducts = products
        binding.productRcv2.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        val adapter = ProductAdapter(products.sortedByDescending { it.sold }.take(4).toMutableList(), offers, wishlistedProductIds)
        addCallBacksForAdapter(adapter)
        binding.productRcv2.adapter = adapter
        handleOpenProductViewAll(binding.btn2.root, false, binding.textView2.text.toString())
    }

    private fun addCallBacksForAdapter(adapter: ProductAdapter) {
        adapter.onItemClick = { product ->
            openDetailProduct(product)
        }
        adapter.onAddButtonClick = { product ->
            Utils.addProductToCart(requireContext(), product.id, userId, defaultUserId)
        }
        adapter.onWishlistProductClick = { product ->
            if (userId == defaultUserId) {
                val loginIntent = Intent(requireContext(), LoginActivity::class.java)
                loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                this.startActivity(loginIntent)
            } else {
                wishlistPresenter.toggleWishlist(product)
            }
        }
    }

    private fun handleOpenProductViewAll(
        view: View,
        isLatestDishes: Boolean = false,
        title: String
    ) {
        view.setOnClickListener {
            val intent = Intent(requireContext(), ProductViewAllActivity::class.java)
            intent.putExtra("filterBy", if(isLatestDishes) "latestDishes" else "bestSellers")
            intent.putExtra("title", title)
            startActivity(intent)
        }
    }

    private fun openDetailProduct(product: Product) {
        val intent = Intent(requireContext(), ProductDetailActivity::class.java)
        intent.putExtra("idProduct", product.id)
        startActivity(intent)
    }

    override fun showOffers(offers: List<Offer>) {
        binding.offerRcv.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        val adapter = OfferAdapter(offers.take(2))
        adapter.onItemClick = { holder ->
            openOfferDetailActivity(offers[holder.adapterPosition])
        }
        binding.offerRcv.adapter = adapter

    }

    private fun openOfferDetailActivity(offer: Offer) {
        val intent = Intent(requireContext(), OfferDetailActivity::class.java)
        intent.putExtra("offerNameWithDiscount", "${offer.name} (${offer.discountRate}%)")
        intent.putExtra("offerId", offer.id)
        intent.putExtra("offerImg", offer.image)
        startActivity(intent)
    }

    override fun showShimmerEffects() {
        showShimmerEffect(binding.cateShimmer, binding.cateRcv)
        showShimmerEffect(binding.product1Shimmer, binding.productRcv1)
        showShimmerEffect(binding.product2Shimmer, binding.productRcv2)
        showShimmerEffect(binding.offerShimmer, binding.offerRcv)
    }

    override fun hideShimmerEffects() {
        hideShimmerEffect(binding.cateShimmer, binding.cateRcv)
        hideShimmerEffect(binding.product1Shimmer, binding.productRcv1)
        hideShimmerEffect(binding.product2Shimmer, binding.productRcv2)
        hideShimmerEffect(binding.offerShimmer, binding.offerRcv)
    }

    override fun onRefresh() {
        lifecycleScope.launch {
            homePresenter.getDataAndDisplay()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }
}