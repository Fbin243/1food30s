package com.zebrand.app1food30s.ui.manage_order

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.zebrand.app1food30s.adapter.ManageOrderAdapter
import com.zebrand.app1food30s.adapter.MyOrderAdapter
import com.zebrand.app1food30s.data.entity.Order
import com.zebrand.app1food30s.data.entity.User
import com.zebrand.app1food30s.utils.FireStoreUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ManageOrderPresenter(private val context: Context, private val view: ManageOrderMVPView) {
    fun getManageOrders(adapter: ManageOrderAdapter) {
        val query = FireStoreUtils.mDBOrderRef
        val userDB = FireStoreUtils.mDBUserRef
        query.addSnapshotListener { snapshot, error ->

            if (error != null) {
                Toast.makeText(context, "Error when getting data", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
            snapshot?.let { querySnapshot ->
//                view.showShimmerEffectForOrders(querySnapshot.size())
                Log.d("Test01", "sizePresenter : ${querySnapshot.size()}")
                for (dc in querySnapshot.documentChanges) {
                    val newObject: Order = dc.document.toObject(Order::class.java)
//                    val userRef: DocumentReference = newObject.idAccount
                    newObject.idAccount?.get()?.addOnSuccessListener { documentSnapshot ->
                        val user: User = documentSnapshot.toObject(User::class.java)!!
                        newObject.user.apply {
                            id = user.id
                            email = user.email
                            firstName = user.firstName
                            lastName = user.lastName
                            phone = user.phone
                        }

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
                        Log.d("ManageOrderPresenter", "User: ${newObject.toString()}")
                    }
                }
//                view.setManageOrderUI()
//                view.hideShimmerEffectForOrders()
            }
        }
    }
}