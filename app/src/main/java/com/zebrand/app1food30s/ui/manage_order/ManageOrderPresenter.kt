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
    suspend fun getManageOrders(adapter: ManageOrderAdapter) = withContext(Dispatchers.IO) {
        val query = FireStoreUtils.mDBOrderRef
        query.addSnapshotListener { snapshot, error ->

            if (error != null) {
                Toast.makeText(context, "Error when getting data", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
            snapshot?.let { querySnapshot ->
                for (dc in querySnapshot.documentChanges) {
                    val newObject: Order = dc.document.toObject(Order::class.java)
                    if (newObject.items.isNotEmpty()) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> {
                                adapter.insertData(newObject)
                            }

                            DocumentChange.Type.MODIFIED -> {
                                adapter.modifyData(newObject)
                            }

                            DocumentChange.Type.REMOVED -> {
                                adapter.removeData(newObject)
                            }
                        }
                    }
                }
            }
        }
    }
}