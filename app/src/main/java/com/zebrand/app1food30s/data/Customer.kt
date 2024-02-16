package com.zebrand.app1food30s.data // Change this to your actual package name

import com.google.firebase.firestore.FirebaseFirestore

// Data class definition (if not already defined elsewhere)
data class Customer(
    val name: String,
    val email: String
    // Add other fields as needed
)

fun Customer.toMap(): Map<String, Any> = mapOf(
    "name" to name,
    "email" to email
    // Include other properties here as needed
)

object FirestoreUtils {

    fun addNewCustomer(customer: Customer, onCustomerAdded: (String) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val customerMap = customer.toMap() // Assuming you have a method to convert Customer to Map

        db.collection("customers").add(customerMap)
            .addOnSuccessListener { documentReference ->
                println("Customer added with ID: ${documentReference.id}")
                onCustomerAdded(documentReference.id)
            }
            .addOnFailureListener { e ->
                println("Error adding customer: $e")
            }
    }

    fun addOrderForCustomer(customerId: String, orderData: Map<String, Any>, items: List<Map<String, Any>>) {
        val db = FirebaseFirestore.getInstance()
        val ordersRef = db.collection("customers").document(customerId).collection("orders")

        ordersRef.add(orderData).addOnSuccessListener { orderDocRef ->
            println("Order added with ID: ${orderDocRef.id}")
            val itemsRef = orderDocRef.collection("items")
            items.forEach { item ->
                itemsRef.add(item).addOnSuccessListener {
                    println("Item added with ID: ${it.id}")
                }
            }
        }
    }
}