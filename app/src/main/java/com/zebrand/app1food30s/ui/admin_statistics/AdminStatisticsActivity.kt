package com.zebrand.app1food30s.ui.admin_statistics

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.zebrand.app1food30s.data.FirestoreUtils.addOrderForCustomer
import com.zebrand.app1food30s.R
import com.google.firebase.firestore.Query
import java.util.*
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.text.SimpleDateFormat
import com.github.mikephil.charting.data.Entry

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
        val textViewValue2 = findViewById<TextView>(R.id.textViewValue2)
        val lineChart: LineChart = findViewById(R.id.lineChart)

        // Example button to trigger the calculation
        val calculateButton = findViewById<Button>(R.id.calculateButton)
        calculateButton.setOnClickListener {
            val days = editTextNumberOfDays.text.toString().toIntOrNull() ?: 0
            calculateRevenueAndDrawChart(days, lineChart, textViewValue1, textViewValue2)
        }
    }

    // store global count is better
    private fun getTotalNumberOfOrders(callback: (Int) -> Unit) {
        callback(1)
//        val db = FirebaseFirestore.getInstance()
//        val customersRef = db.collection("customers")
//
//        var totalOrders = 0
//        customersRef.get()
//            .addOnSuccessListener { customerSnapshots ->
//                // Check if there are no customers
//                if (customerSnapshots.isEmpty) {
//                    callback(0)
//                    return@addOnSuccessListener
//                }
//
//                // Track the number of processed customers to know when all have been processed
//                var processedCustomers = 0
//                val numberOfCustomers = customerSnapshots.size()
//
//                // Iterate over each customer and fetch their orders
//                for (customerSnapshot in customerSnapshots) {
//                    val ordersRef = customerSnapshot.reference.collection("orders")
//                    ordersRef.get()
//                        .addOnSuccessListener { orderSnapshots ->
//                            totalOrders += orderSnapshots.size()
//                            processedCustomers++
//                            // If all customers have been processed, return the total
//                            if (processedCustomers == numberOfCustomers) {
//                                callback(totalOrders)
//                            }
//                        }
//                        .addOnFailureListener { exception ->
//                            Log.w("Firestore", "Error getting orders for customer: ", exception)
//                            processedCustomers++
//                            // Still proceed with the count in case of failure, to ensure completion
//                            if (processedCustomers == numberOfCustomers) {
//                                callback(totalOrders)
//                            }
//                        }
//                }
//            }
//            .addOnFailureListener { exception ->
//                Log.w("Firestore", "Error getting customers: ", exception)
//                callback(0)
//            }
    }

    fun calculateRevenueAndDrawChart(days: Int, lineChart: LineChart, textViewValue1: TextView, textViewValue2: TextView) {
        val db = FirebaseFirestore.getInstance()
        val customersRef = db.collection("customers")

        // Calculate the start date for the query
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        val startDate = calendar.time

        // Initialize a map to hold revenue per day
        val revenuePerDay = mutableMapOf<String, Double>()

        // Prepare to format dates
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        customersRef.get().addOnSuccessListener { customerSnapshots ->
            val customerCount = customerSnapshots.size()
            var processedCustomers = 0

            if (customerCount == 0) {
                // No customers found, proceed to update the chart with no data
                updateChartAndTextViews(lineChart, revenuePerDay, days, textViewValue1, textViewValue2)
                return@addOnSuccessListener
            }

            customerSnapshots.documents.forEach { customerDoc ->
                val ordersRef = customerDoc.reference.collection("orders")
                ordersRef.whereGreaterThan("date", startDate).get()
                    .addOnSuccessListener { orderSnapshots ->
                        orderSnapshots.forEach { orderDoc ->
                            val totalAmount = orderDoc.getDouble("totalAmount") ?: 0.0
                            val orderDate = orderDoc.getDate("date")
                            val formattedDate = dateFormat.format(orderDate)
                            revenuePerDay[formattedDate] = revenuePerDay.getOrDefault(formattedDate, 0.0) + totalAmount
                        }
                        processedCustomers++
                        if (processedCustomers == customerCount) {
                            // All customers processed, update the chart and TextViews
                            updateChartAndTextViews(lineChart, revenuePerDay, days, textViewValue1, textViewValue2)
                        }
                    }
                    .addOnFailureListener { e ->
                        println("Error fetching orders: $e")
                        processedCustomers++
                        if (processedCustomers == customerCount) {
                            // Handle partial failure scenario
                            updateChartAndTextViews(lineChart, revenuePerDay, days, textViewValue1, textViewValue2)
                        }
                    }
            }
        }
            .addOnFailureListener { e ->
                println("Error fetching customers: $e")
                // Error fetching customers, update the chart with no data
                updateChartAndTextViews(lineChart, revenuePerDay, days, textViewValue1, textViewValue2)
            }
    }

    private fun updateChartAndTextViews(lineChart: LineChart, revenuePerDay: Map<String, Double>, days: Int, textViewValue1: TextView, textViewValue2: TextView) {
        val entries = ArrayList<Entry>()

        // Sort the map by date to ensure the data is plotted in chronological order
        val sortedRevenuePerDay = revenuePerDay.toSortedMap()

        // Convert the revenue per day into chart entries
        sortedRevenuePerDay.keys.forEachIndexed { index, date ->
            entries.add(Entry(index.toFloat(), sortedRevenuePerDay[date]!!.toFloat()))
        }

        val dataSet = LineDataSet(entries, "Daily Revenue")
        val lineData = LineData(dataSet)
        lineChart.data = lineData
        lineChart.invalidate() // Refresh the chart

        // Calculate total and average revenue
        val totalRevenue = sortedRevenuePerDay.values.sum()
        val averageRevenuePerDay = if (days > 0) totalRevenue / days else 0.0

        // Update the TextViews on the UI thread
        textViewValue1.post {
            textViewValue1.text = "Total Revenue: ${"%.2f".format(totalRevenue)}"
            textViewValue2.text = "Average Revenue/Day: ${"%.2f".format(averageRevenuePerDay)}"
        }
    }
}
