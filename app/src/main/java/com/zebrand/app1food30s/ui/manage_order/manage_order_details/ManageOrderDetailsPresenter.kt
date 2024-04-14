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
import com.zebrand.app1food30s.utils.SingletonKey
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
                }

                view.setManageOrderDetailsUI()
                Log.d("Test00", "placeOrder: Starting order placement. $orderDetails")
            }
        }
    }

    fun changeOrderStatus(idOrder: String, status: String) {
        val dOrderRef = FireStoreUtils.mDBOrderRef

        val doc: DocumentReference = dOrderRef.document(idOrder)

        doc.update("orderStatus", status)
        view.setManageOrderDetailsUI()
    }

    fun cancelOrder(idOrder: String, reason: String) {
        val dOrderRef = FireStoreUtils.mDBOrderRef

        val doc: DocumentReference = dOrderRef.document(idOrder)

        doc.update("orderStatus", SingletonKey.CANCELLED)
        doc.update("cancelReason", reason)
        view.setManageOrderDetailsUI()
    }

    fun changePaymentStatus(idOrder: String, status: String) {
        val dOrderRef = FireStoreUtils.mDBOrderRef

        val doc: DocumentReference = dOrderRef.document(idOrder)

        doc.update("paymentStatus", status)
    }
}