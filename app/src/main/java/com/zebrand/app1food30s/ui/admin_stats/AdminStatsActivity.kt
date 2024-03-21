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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AdminStatsActivity : AppCompatActivity() {
    private lateinit var myGridRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_statistics)

        myGridRecyclerView = findViewById(R.id.my_grid_recycler_view)

        getTotalNumberOfOrders { totalOrders ->
            val myGridAdapter = MyGridAdapter(
                arrayOf(
                    MyGridAdapter.GridItem("Text 1", totalOrders, R.drawable.ic_total_orders),
                    MyGridAdapter.GridItem("Text 2", 2, R.drawable.image),
                    MyGridAdapter.GridItem("Text 3", 3, R.drawable.image),
                    MyGridAdapter.GridItem("Text 4", 4, R.drawable.image),
                )
            )
            myGridRecyclerView.adapter = myGridAdapter
            val spanCount = 2 // Number of columns
            myGridRecyclerView.layoutManager = GridLayoutManager(this@AdminStatsActivity, spanCount)
            val spacing = 60 // Spacing in pixels
            val includeEdge = false
            myGridRecyclerView.addItemDecoration(GridSpacingItemDecoration(spanCount, spacing, includeEdge))
        }

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

    private fun getTotalNumberOfOrders(callback: (Int) -> Unit) {
        callback(1)
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
