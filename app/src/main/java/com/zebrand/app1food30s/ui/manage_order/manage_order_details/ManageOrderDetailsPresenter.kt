package com.zebrand.app1food30s.ui.manage_order.manage_order_details

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.DocumentReference
import com.zebrand.app1food30s.adapter.MyOrderDetailsAdapter
import com.zebrand.app1food30s.data.entity.Order
import com.zebrand.app1food30s.data.entity.User
import com.zebrand.app1food30s.ui.my_order.my_order_details.MyOrderDetailsMVPView
import com.zebrand.app1food30s.utils.FireStoreUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ManageOrderDetailsPresenter(
    private val context: Context,
    private val view: ManageOrderDetailsMVPView
) {
    fun getManageOrderDetails(
        idOrder: String,
        orderDetails:Order?,
        adapter: MyOrderDetailsAdapter
    ) {
        val dOrderRef = FireStoreUtils.mDBOrderRef
//        var mutableOrderDetails = orderDetails

        val doc: DocumentReference = dOrderRef.document(idOrder)
        Log.d("Test00", doc.toString())
        doc.get().addOnSuccessListener { snapshot ->
            if (snapshot != null && snapshot.exists()) {
                val newObject = snapshot.toObject(Order::class.java)

                if (newObject != null && newObject.items.isNotEmpty()) {
                    orderDetails?.apply {
                        id = newObject.id
                        idAccount = newObject.idAccount
                        items.clear()
                        items.addAll(newObject.items)
                        totalAmount = newObject.totalAmount
                        orderStatus = newObject.orderStatus
                        cancelReason = newObject.cancelReason
                        shippingAddress = newObject.shippingAddress
                        paymentStatus = newObject.paymentStatus
                        note = newObject.note
                        date = newObject.date
                    }

                    for (newItem in newObject.items) {
                        adapter.insertData(newItem)
                    }
                }
                view.setManageOrderDetailsUI()
            } else {
                Toast.makeText(context, "No data found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { error ->
            Toast.makeText(context, "Error when getting data: ${error.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun changeOrderStatus(idOrder: String, status: String) {
        val dOrderRef = FireStoreUtils.mDBOrderRef

        val doc: DocumentReference = dOrderRef.document(idOrder)

        doc.update("orderStatus", status)
            .addOnSuccessListener {
//                Toast.makeText(context, "Order canceled", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
//                Toast.makeText(context, "Error when canceling order", Toast.LENGTH_SHORT).show()
            }
    }

    fun changePaymentStatus(idOrder: String, status: String) {
        val dOrderRef = FireStoreUtils.mDBOrderRef

        val doc: DocumentReference = dOrderRef.document(idOrder)

        doc.update("paymentStatus", status)
            .addOnSuccessListener {
//                Toast.makeText(context, "Order canceled", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
//                Toast.makeText(context, "Error when canceling order", Toast.LENGTH_SHORT).show()
            }
    }
}