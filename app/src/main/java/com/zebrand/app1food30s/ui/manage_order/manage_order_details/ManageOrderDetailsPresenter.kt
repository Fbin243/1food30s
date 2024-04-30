package com.zebrand.app1food30s.ui.manage_order.manage_order_details

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.DocumentReference
import com.zebrand.app1food30s.adapter.MyOrderDetailsAdapter
import com.zebrand.app1food30s.data.entity.Order
import com.zebrand.app1food30s.data.entity.Product
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
                view.showShimmerEffectForOrders(newObject.items.size)
                if (orderDetails?.id?.isEmpty() == false && (orderDetails.orderStatus != newObject.orderStatus
                            || orderDetails.paymentStatus != newObject.paymentStatus
                            || orderDetails.cancelReason != newObject.cancelReason) ) {
                    // Trạng thái đã được sửa đổi, cập nhật lại trong adapter
                    orderDetails.apply {
                        orderStatus = newObject.orderStatus
                        paymentStatus = newObject.paymentStatus
                        cancelReason = newObject.cancelReason
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
                        paymentMethod = newObject.paymentMethod
                        originAmount = newObject.originAmount
                        shippingFee = newObject.shippingFee
                        note = newObject.note
                        date = newObject.date
                    }
                    adapter.updateIsDelivered(newObject.orderStatus)
                    for (item in newObject.items) {
                        adapter.insertData(item)
                    }
                }

                view.setManageOrderDetailsUI()
                view.hideShimmerEffectForOrders()
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

    fun acceptOrder(idOrder: String, adapter: MyOrderDetailsAdapter) {
        val dOrderRef = FireStoreUtils.mDBOrderRef
        val dProductRef = FireStoreUtils.mDBProductRef
        val doc: DocumentReference = dOrderRef.document(idOrder)

        val orderDetailsList = adapter.orderItems

        var checked = false
        var counter = 0

        for (orderItem in orderDetailsList) {
            orderItem.productId?.get()?.addOnSuccessListener {
                val product = it.toObject(Product::class.java)
                if (product != null) {
                    if (product.stock < orderItem.quantity) {
                        checked = true
                        Toast.makeText(context, "Product ${product.name} is out of stock", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }
                    counter++
                    if (counter == orderDetailsList.size) {
                        if(!checked){
                            for (orderItem in orderDetailsList) {
                                orderItem.productId?.get()?.addOnSuccessListener {
                                    val product = it.toObject(Product::class.java)
                                    if (product != null) {
                                        Log.d("ManageOrderDetailsPresenter", "acceptOrder: $product")
                                        val newStock = product.stock - orderItem.quantity
                                        dProductRef.document(product.id).update("sold", product.sold + orderItem.quantity)
                                        dProductRef.document(product.id).update("stock", newStock)
                                    }
                                }
                            }

                            doc.update("orderStatus", SingletonKey.ORDER_ACCEPTED)
                            Toast.makeText(context, "Order accepted", Toast.LENGTH_SHORT).show()
                            view.setManageOrderDetailsUI()
                        }
                    }
                }
            }
        }
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