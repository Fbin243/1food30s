package com.zebrand.app1food30s.ui.manage_order

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.zebrand.app1food30s.adapter.ManageOrderAdapter
import com.zebrand.app1food30s.data.entity.Order
import com.zebrand.app1food30s.data.entity.User
import com.zebrand.app1food30s.utils.FireStoreUtils

class ManageOrderPresenter(private val context: Context, private val view: ManageOrderMVPView) {
    fun getManageOrders(adapter: ManageOrderAdapter, manageOrderList: MutableList<Order>, customerArr: MutableSet<String>) {
        val mDBOrder = FireStoreUtils.mDBOrderRef
        customerArr.clear()
        manageOrderList.clear()
        adapter.clear()

        view.showShimmerEffectForOrders(5)
        mDBOrder.get().addOnSuccessListener { documents ->
            for (document in documents) {
                val newObject: Order = document.toObject(Order::class.java)
                val userRef: DocumentReference? = newObject.idAccount
                userRef?.get()?.addOnSuccessListener { documentSnapshot ->
                    val user: User = documentSnapshot.toObject(User::class.java)!!
                    newObject.user.apply {
                        id = user.id
                        email = user.email
                        firstName = user.firstName
                        lastName = user.lastName
                        phone = user.phone
                    }

                    customerArr.add(user.firstName + " " + user.lastName)

                    adapter.insertData(newObject)
                    manageOrderList.add(newObject)
                    Log.d("ManageOrderPresenter", "User: ${customerArr.toString()}")

                    if(manageOrderList.size == documents.size()) {
                        view.setManageOrderUI(documents.size())
                        view.hideShimmerEffectForOrders()
                    }
                }
            }

        }
    }
}