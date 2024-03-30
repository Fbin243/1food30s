package com.zebrand.app1food30s.ui.admin_stats

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.firestore.FirebaseFirestore
import com.zebrand.app1food30s.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AdminStatsActivity : AppCompatActivity() {
    private lateinit var myGridRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_statistics)

        myGridRecyclerView = findViewById(R.id.my_grid_recycler_view)

        updateGridItems()

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

    private fun updateGridItems() {
        CoroutineScope(Dispatchers.Main).launch {
            val totalOrdersDeferred = async { getTotalNumberOfOrders() }
            val totalPendingDeferred = async { getTotalOrdersWithStatus("Pending") }
            val totalAcceptedDeferred = async { getTotalOrdersWithStatus("Order accepted") }
            val totalDeliveringDeferred = async { getTotalOrdersWithStatus("On delivery") }
            val totalDeliveredDeferred = async { getTotalOrdersWithStatus("Delivered") }

            val totalOrders = totalOrdersDeferred.await()
            val totalPending = totalPendingDeferred.await()
            val totalAccepted = totalAcceptedDeferred.await()
            val totalDelivering = totalDeliveringDeferred.await()
            val totalDelivered = totalDeliveredDeferred.await()

            // Now update the UI with totalOrders and totalPending
            val myGridAdapter = MyGridAdapter(
                arrayOf(
                    MyGridAdapter.GridItem("Total Orders", totalOrders, R.drawable.ic_total_orders),
                    MyGridAdapter.GridItem("Pending", totalPending, R.drawable.ic_box_pending),
                    MyGridAdapter.GridItem("Accepted", totalAccepted, R.drawable.ic_box_accepted),
                    MyGridAdapter.GridItem("On delivery", totalDelivering, R.drawable.ic_box_delivering),
                    MyGridAdapter.GridItem("Delivered", totalDelivered, R.drawable.ic_box_delivered),
                )
            )
            myGridRecyclerView.adapter = myGridAdapter
            val spanCount = 2 // Number of columns
            myGridRecyclerView.layoutManager = GridLayoutManager(this@AdminStatsActivity, spanCount)
            val spacing = 60 // Spacing in pixels
            val includeEdge = false
            myGridRecyclerView.addItemDecoration(GridSpacingItemDecoration(spanCount, spacing, includeEdge))
        }
    }

    private suspend fun getTotalNumberOfOrders(): Int = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        val ordersRef = db.collection("orders")
        try {
            val snapshot = ordersRef.get().await()
            return@withContext snapshot.documents.size
        } catch (e: Exception) {
//            Log.e("Error", "Failed to fetch total number of orders", e)
            return@withContext 0
        }
    }

    private suspend fun getTotalOrdersWithStatus(orderStatus: String): Int = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        val ordersRef = db.collection("orders")
        try {
            val snapshot = ordersRef.whereEqualTo("orderStatus", orderStatus).get().await()
            return@withContext snapshot.documents.size
        } catch (e: Exception) {
//            Log.e("Error", "Failed to fetch orders with status $orderStatus", e)
            return@withContext 0
        }
    }

    private fun calculateRevenueAndDrawChart(
        days: Int,
        lineChart: LineChart,
        textViewValue1: TextView,
        textViewValue2: TextView
    ) {
        val db = FirebaseFirestore.getInstance()
        val ordersRef = db.collection("orders")

        // Calculate the start date for the query
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        val startDate = calendar.time

        // Initialize a map to hold revenue per day
        val revenuePerDay = mutableMapOf<String, Double>()

        // Prepare to format dates
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Query orders that were created within the specified time frame
        ordersRef.whereGreaterThan("date", startDate).get()
            .addOnSuccessListener { orderSnapshots ->
                if (orderSnapshots.isEmpty) {
                    // No orders found, proceed to update the chart with no data
                    updateChartAndTextViews(lineChart, revenuePerDay, days, textViewValue1, textViewValue2)
                    return@addOnSuccessListener
                }

                orderSnapshots.forEach { orderDoc ->
                    val totalAmount = orderDoc.getDouble("totalAmount") ?: 0.0
                    val orderDate = orderDoc.getDate("date")
                    val formattedDate = dateFormat.format(orderDate)
                    revenuePerDay[formattedDate] = revenuePerDay.getOrDefault(formattedDate, 0.0) + totalAmount
                }

                // All orders processed, update the chart and TextViews
                updateChartAndTextViews(lineChart, revenuePerDay, days, textViewValue1, textViewValue2)
            }
            .addOnFailureListener { e ->
                println("Error fetching orders: $e")
                // Error fetching orders, update the chart with no data
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
