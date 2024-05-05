package com.zebrand.app1food30s.ui.my_order

import android.content.Context
import android.view.View
import android.widget.Toast
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.zebrand.app1food30s.adapter.MyOrderAdapter
import com.zebrand.app1food30s.data.entity.Order
import com.zebrand.app1food30s.utils.FireStoreUtils
import com.zebrand.app1food30s.utils.Utils


class MyOrderPresenter(private val context: Context, private val view: MyOrderMVPView) {
    fun getActiveOrderList(idAccount: String, adapter: MyOrderAdapter) {
        val dOrderRef = FireStoreUtils.mDBOrderRef
        val userDoc: DocumentReference = FireStoreUtils.mDBUserRef.document(idAccount)

        val query: Query = dOrderRef
            .whereEqualTo("idAccount", userDoc)
            .whereNotIn("orderStatus", listOf("Delivered", "Cancelled"))
            .orderBy("date", Query.Direction.DESCENDING)
        query.addSnapshotListener { snapshot, error ->
            if (error != null) {
//                Toast.makeText(context, "Error when getting data", Toast.LENGTH_SHORT).show()
                Utils.showCustomToast(context, "Error when getting data", "error")
                return@addSnapshotListener
            }
            snapshot?.let { querySnapshot ->
                view.showShimmerEffectForOrders(querySnapshot.size())
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
                view.setMyActiveOrderUI()
                view.hideShimmerEffectForOrders()
            }
        }
    }

    fun getPrevOrderList(idAccount: String, adapter: MyOrderAdapter) {
        val dOrderRef = FireStoreUtils.mDBOrderRef
        val userDoc: DocumentReference = FireStoreUtils.mDBUserRef.document(idAccount)

        val query: Query = dOrderRef
            .whereEqualTo("idAccount", userDoc)
            .whereIn("orderStatus", listOf("Delivered", "Cancelled"))
            .orderBy("date", Query.Direction.DESCENDING)
        query.addSnapshotListener { snapshot, error ->
            if (error != null) {
//                Toast.makeText(context, "Error when getting data", Toast.LENGTH_SHORT).show()
                Utils.showCustomToast(context, "Error when getting data", "error")
                return@addSnapshotListener
            }
            snapshot?.let { querySnapshot ->
                view.showShimmerEffectForOrders(querySnapshot.size())
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
                view.setMyPrevOrderUI()
                view.hideShimmerEffectForOrders()
            }
        }
    }
}