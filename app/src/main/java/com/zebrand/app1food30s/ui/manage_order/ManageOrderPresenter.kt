package com.zebrand.app1food30s.ui.manage_order

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.DocumentChange
import com.zebrand.app1food30s.adapter.ManageOrderAdapter
import com.zebrand.app1food30s.adapter.MyOrderAdapter
import com.zebrand.app1food30s.data.entity.Order
import com.zebrand.app1food30s.data.entity.User
import com.zebrand.app1food30s.utils.FireStoreUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ManageOrderPresenter(private val context: Context) {
    suspend fun getManageOrders(): MutableList<Order> = withContext(Dispatchers.IO) {
        val orders = mutableListOf<Order>()
        val query = FireStoreUtils.mDBOrderRef
        try {
            val querySnapshot = query.get().await()
            for (document in querySnapshot.documents) {
                val order = document.toObject(Order::class.java)
                order?.id = document.id
                order?.idAccount = document.getDocumentReference("idAccount")
                if (order != null) {
                    val idAccountDocument = order.idAccount?.get()?.await()
                    if (idAccountDocument != null && idAccountDocument.exists()) {
                        val userData = idAccountDocument.toObject(User::class.java)
                        order.user = userData ?: User() // Assigning user data to order
                    }
                    orders.add(order)
                }
            }
        } catch (e: Exception) {
            Log.e("getManageOrders", "Error getting orders: ", e)
        }
        return@withContext orders
    }
}