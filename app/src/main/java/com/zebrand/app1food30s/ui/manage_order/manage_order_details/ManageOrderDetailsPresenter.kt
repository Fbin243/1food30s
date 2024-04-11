package com.zebrand.app1food30s.ui.manage_order.manage_order_details

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.DocumentChange
import com.zebrand.app1food30s.adapter.MyOrderAdapter
import com.zebrand.app1food30s.data.entity.Order
import com.zebrand.app1food30s.data.entity.User
import com.zebrand.app1food30s.utils.FireStoreUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ManageOrderDetailsPresenter (private val context: Context) {
//    suspend fun getManageOrderDetails(idOrder: String, adapter: MyOrderAdapter) = withContext(Dispatchers.IO) {
//        val orders = mutableListOf<Order>()
//        val query = FireStoreUtils.mDBOrderRef.document()
//        query.addSnapshotListener { snapshot, error ->
//
//            if (error != null) {
//                Toast.makeText(context, "Error when getting data", Toast.LENGTH_SHORT).show()
//                return@addSnapshotListener
//            }
//            snapshot?.let { querySnapshot ->
//                for (dc in querySnapshot.documentChanges) {
//                    val newObject: Order = dc.document.toObject(Order::class.java)
//                    if (newObject.items.isNotEmpty()) {
//                        when (dc.type) {
//                            DocumentChange.Type.ADDED -> {
//                                adapter.insertData(newObject)
//                            }
//
//                            DocumentChange.Type.MODIFIED -> {
//                                adapter.modifyData(newObject)
//                            }
//
//                            DocumentChange.Type.REMOVED -> {
//                                adapter.removeData(newObject)
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        try {
//            val querySnapshot = query.get().await()
//            for (document in querySnapshot.documents) {
//                val order = document.toObject(Order::class.java)
//                order?.id = document.id
//                order?.idAccount = document.getDocumentReference("idAccount")
//                if (order != null) {
//                    val idAccountDocument = order.idAccount?.get()?.await()
//                    if (idAccountDocument != null && idAccountDocument.exists()) {
//                        val userData = idAccountDocument.toObject(User::class.java)
//                        order.user = userData ?: User() // Assigning user data to order
//                    }
//                    orders.add(order)
//                }
//            }
//        } catch (e: Exception) {
//            Log.e("getManageOrders", "Error getting orders: ", e)
//        }
//        return@withContext orders
//    }
}