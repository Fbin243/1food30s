package com.zebrand.app1food30s.ui.my_order.my_order_details

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.zebrand.app1food30s.adapter.MyOrderDetailsAdapter
import com.zebrand.app1food30s.data.entity.Order
import com.zebrand.app1food30s.utils.FireStoreUtils

class MyOrderDetailsPresenter(private val context: Context, private val view: MyOrderDetailsMVPView) {
    fun getMyOrderDetails(idOrder: String, orderDetails:Order? ,adapter: MyOrderDetailsAdapter) {
        val dOrderRef = FireStoreUtils.mDBOrderRef

        val doc: DocumentReference = dOrderRef.document(idOrder)

        doc.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Toast.makeText(context, "Error when getting data", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
            if (snapshot == null) {
                Toast.makeText(context, "No data found", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            val newObject: Order? = snapshot.toObject(Order::class.java)
            Log.d("Test00", "placeOrder: Starting order placement. ${orderDetails?.id} $newObject")
            if (newObject != null && newObject.items.isNotEmpty()) {
                var check: Boolean = false
                for (newItem in newObject.items) {
                    val oldItem = orderDetails?.items?.find { it.productId == newItem.productId }
                    if (oldItem != null && oldItem.reviewed != newItem.reviewed) {
                        check = true
                        // Replace the old item with the new item
                        val index = orderDetails.items.indexOf(oldItem)
                        orderDetails.items[index] = newItem
                    }
                }

                if(!check){
                    if (orderDetails?.id?.isEmpty() == false && (orderDetails.orderStatus != newObject.orderStatus || orderDetails.paymentStatus != newObject.paymentStatus) ) {
                        // Trạng thái đã được sửa đổi, cập nhật lại trong adapter
                        orderDetails.apply {
                            orderStatus = newObject.orderStatus
                            paymentStatus = newObject.paymentStatus
                        }
                    } else {
                        // Nếu không có sự thay đổi trạng thái, thêm dữ liệu mới vào adapter
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
                        adapter.updateIsDelivered(newObject.orderStatus)
                        for (item in newObject.items) {
                            adapter.insertData(item)
                        }
                        view.handleReviewProduct()
                    }
                }

                view.setOrderDetailsUI()
                Log.d("Test00", "placeOrder: Starting order placement. $orderDetails")
            }
        }
    }

    fun changePaymentStatus(idOrder: String, status: String) {
        val dOrderRef = FireStoreUtils.mDBOrderRef

        val doc: DocumentReference = dOrderRef.document(idOrder)

        doc.update("paymentStatus", status)
            .addOnSuccessListener {
//                Toast.makeText(context, "Payment status changed", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
//                Toast.makeText(context, "Error when changing payment status", Toast.LENGTH_SHORT).show()
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
}