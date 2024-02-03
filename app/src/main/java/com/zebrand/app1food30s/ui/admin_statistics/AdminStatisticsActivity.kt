package com.zebrand.app1food30s.ui.admin_statistics

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.yourpackage.name.Customer
import com.yourpackage.name.FirestoreUtils
import com.yourpackage.name.FirestoreUtils.addOrderForCustomer
import com.zebrand.app1food30s.R
import com.google.firebase.firestore.Query
import java.util.*

class AdminStatisticsActivity : AppCompatActivity() {
    private lateinit var myGridRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Setting the content view to the layout defined in activity_admin_statistics.xml
        setContentView(R.layout.activity_admin_statistics)

        // Setup for the Grid RecyclerView
        // Find the RecyclerView widget as defined in the XML layout
        myGridRecyclerView = findViewById(R.id.my_grid_recycler_view)
        // Create an adapter for the grid RecyclerView with predefined data

        // Fetch total number of orders
        getTotalNumberOfOrders { totalOrders ->
            val myGridAdapter = MyGridAdapter(
                arrayOf(
                    MyGridAdapter.GridItem("Text 1", totalOrders, R.drawable.image),
                    MyGridAdapter.GridItem("Text 2", 2, R.drawable.image),
                    MyGridAdapter.GridItem("Text 3", 3, R.drawable.image),
                    MyGridAdapter.GridItem("Text 4", 4, R.drawable.image),
                )
            )
            myGridRecyclerView.adapter = myGridAdapter
            val spanCount = 2 // Number of columns
            myGridRecyclerView.layoutManager = GridLayoutManager(this@AdminStatisticsActivity, spanCount)
            val spacing = 60 // Spacing in pixels
            val includeEdge = true
            myGridRecyclerView.addItemDecoration(GridSpacingItemDecoration(spanCount, spacing, includeEdge))
            myGridRecyclerView.setHasFixedSize(true)
            // Ensure the RecyclerView is updated
        }

//        // Example of dynamically updating the TextViews
//        val textViewValue1: TextView = findViewById(R.id.textViewValue1)
//        val textViewValue2: TextView = findViewById(R.id.textViewValue2)
//
//        // Set the text based on your data
//        textViewValue1.text = "10" // Example value
//        textViewValue2.text = "20" // Example value

//        val newCustomer = Customer(name = "Jane Doe", email = "jane.doe@example.com")
//        FirestoreUtils.addNewCustomer(newCustomer) { customerId ->
//            // This block will be executed once the customer is successfully added.
//            // The customerId parameter contains the auto-generated ID of the new customer.
//            println("New customer added with ID: $customerId")
//
//            // Here, you can proceed to add an order for the new customer using the customerId.
//            // For example:
//            val orderData = mapOf(
//                "date" to "2023-01-01",
//                "status" to "Completed",
//                "totalAmount" to 150
//            )
//            val items = listOf(
//                mapOf("productId" to "prod123", "quantity" to 2, "price" to 50),
//                mapOf("productId" to "prod456", "quantity" to 1, "price" to 50)
//            )
//
//            // Assuming you have defined a function to add an order for a customer
//            FirestoreUtils.addOrderForCustomer(customerId, orderData, items)
//        }

        val editTextNumberOfDays = findViewById<EditText>(R.id.editTextNumberOfDays)
        val textViewValue1 = findViewById<TextView>(R.id.textViewValue1)

        // Example button to trigger the calculation
        val calculateButton = findViewById<Button>(R.id.calculateButton)
        calculateButton.setOnClickListener {
            val days = editTextNumberOfDays.text.toString().toIntOrNull() ?: 0
            calculateTotalRevenueLastXDays(days) { totalRevenue ->
                // Update the TextView with the calculated total revenue
                textViewValue1.text = totalRevenue.toString()
            }
        }
    }

    private fun getTotalNumberOfOrders(callback: (Int) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("orders").get()
            .addOnSuccessListener { documents ->
                val totalOrders = documents.size()
                callback(totalOrders)
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: ", exception)
                callback(0) // Handle error case as needed
            }
    }

    fun calculateTotalRevenueLastXDays(days: Int, onResult: (Double) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val customersRef = db.collection("customers")

        // Calculate the start date for the query
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        val startDate = calendar.time

        var totalRevenue = 0.0
        customersRef.get().addOnSuccessListener { customerSnapshots ->
            val customerCount = customerSnapshots.size()
            var processedCustomers = 0

            if (customerCount == 0) {
                onResult(0.0) // No customers found
                return@addOnSuccessListener
            }

            customerSnapshots.documents.forEach { customerDoc ->
                val ordersRef = customerDoc.reference.collection("orders")
                ordersRef.whereGreaterThan("date", startDate).get()
                    .addOnSuccessListener { orderSnapshots ->
                        orderSnapshots.forEach { orderDoc ->
                            val totalAmount = orderDoc.getDouble("totalAmount") ?: 0.0
                            totalRevenue += totalAmount
                        }
                        processedCustomers++
                        if (processedCustomers == customerCount) {
                            onResult(totalRevenue) // Final total after all customers processed
                        }
                    }
                    .addOnFailureListener { e ->
                        println("Error fetching orders: $e")
                        processedCustomers++
                        if (processedCustomers == customerCount) {
                            onResult(totalRevenue) // Handle partial failure scenario
                        }
                    }
            }
        }
            .addOnFailureListener { e ->
                println("Error fetching customers: $e")
                onResult(0.0) // Error fetching customers
            }
    }
}
