package com.zebrand.app1food30s.ui.my_order

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.zebrand.app1food30s.adapter.MyOrderAdapter
import com.zebrand.app1food30s.data.entity.Order
import com.zebrand.app1food30s.utils.FireStoreUtils

class MyOrderPresenter(
    private val context: Context, private val view: MyOrderMVPView
) {
    fun getMyOrderList(idAccount: String, adapter: MyOrderAdapter, list: List<Order>) {

        val dOrderRef = FireStoreUtils.mDBOrderRef
        val userDoc: DocumentReference = FireStoreUtils.mDBUserRef.document(idAccount)
        val query: Query = dOrderRef
            .whereEqualTo("idAccount", userDoc)
            .orderBy("date", Query.Direction.DESCENDING)
        Log.d("Test00", "getMyOrderList: ${userDoc.path}")
        query.get()
            .addOnSuccessListener { snapshot ->
                for (documentSnapshot in snapshot) {
                    val order = documentSnapshot.toObject(Order::class.java)
                    Log.d("Test00", "getMyOrderList: ${order.id}")
//                    val dOrderRef = mDBOrderRef.document(documentSnapshot.id)
//                    val mOrderProductRef = dOrderRef.collection("product")
//
//                    mOrderProductRef.get()
//                        .addOnSuccessListener { snapshotChild ->
//                            val orderProductList = mutableListOf<OrderProduct>()
//                            for (documentSnapChild in snapshotChild) {
//                                val orderProduct = documentSnapChild.toObject(OrderProduct::class.java)
//                                orderProduct._id = documentSnapChild.id
//                                orderProductList.add(orderProduct)
//                            }
//                            val myOrderItem = MyOrderItem(MyOrderDetailsAdapter.TYPE_EVALUATE,
//                                orderProductList, order.createdAt)
//                            adapter.addOneData(list, myOrderItem)
//                        }
//                        .addOnFailureListener { e ->
//                            // Handle failure if needed
//                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error: $e", Toast.LENGTH_SHORT).show()
            }
    }
}