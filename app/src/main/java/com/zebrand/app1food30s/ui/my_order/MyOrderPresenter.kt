package com.zebrand.app1food30s.ui.my_order

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.annotations.Nullable
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.zebrand.app1food30s.adapter.MyOrderAdapter
import com.zebrand.app1food30s.data.entity.Order
import com.zebrand.app1food30s.data.entity.Product
import com.zebrand.app1food30s.utils.FireStoreUtils


class MyOrderPresenter(
    private val context: Context, private val view: MyOrderMVPView
) {
    fun getMyOrderList(idAccount: String, adapter: MyOrderAdapter, list: MutableList<Order>) {

        val dOrderRef = FireStoreUtils.mDBOrderRef
        val userDoc: DocumentReference = FireStoreUtils.mDBUserRef.document(idAccount)
        Log.d("Test00", "getMyOrderList: ${userDoc.path}")
        val query: Query = dOrderRef
            .whereEqualTo("idAccount", userDoc)
            .orderBy("date", Query.Direction.DESCENDING)
        Log.d("Test00", "getMyOrderList: ${userDoc.path}")
        query.get()
            .addOnSuccessListener { snapshot ->
                for (documentSnapshot in snapshot) {
                    val order = documentSnapshot.toObject(Order::class.java)
                    Log.d("Test00", "getMyOrderList: ${order.id}")
                    list.add(order)
                    adapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error: $e", Toast.LENGTH_SHORT).show()
            }
    }

    fun getActiveOrderList(idAccount: String, adapter: MyOrderAdapter, list: MutableList<Order>) {
        val dOrderRef = FireStoreUtils.mDBOrderRef
        val userDoc: DocumentReference = FireStoreUtils.mDBUserRef.document(idAccount)

        val query: Query = dOrderRef
            .whereEqualTo("idAccount", userDoc)
            .orderBy("date", Query.Direction.DESCENDING)
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