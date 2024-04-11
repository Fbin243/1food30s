package com.zebrand.app1food30s.ui.admin_stats

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.firestore.FirebaseFirestore
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.databinding.ActivityAdminStatisticsBinding
import com.zebrand.app1food30s.utils.FireStoreUtils.mDBOrderRef
import com.zebrand.app1food30s.utils.FireStoreUtils.mDBProductRef
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AdminStatsFragment : Fragment() {
    //    private lateinit var myGridRecyclerView: RecyclerView
//    private lateinit var binding: ActivityAdminStatisticsBinding
    private var _binding: ActivityAdminStatisticsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = ActivityAdminStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView and other views here
        updateGridItems()
        setupDaysChooser()
        calculateRevenueAndDrawChart(30, binding.barChart, binding.textViewValue1, binding.textViewValue2)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel all coroutines
    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityAdminStatisticsBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        myGridRecyclerView = findViewById(R.id.my_grid_recycler_view)
//
//        updateGridItems()
//        setupDaysChooser()
//        // Draw the bar chart with the last 30 days of revenue data on startup
//        calculateRevenueAndDrawChart(30, binding.barChart, binding.textViewValue1, binding.textViewValue2)
//    }

    private fun setupDaysChooser() {
        binding.containerDaysChooser.setOnClickListener {
            showDaysInputPopup()
        }
    }

    private fun showDaysInputPopup() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_days_chooser, null)
        val editText = dialogView.findViewById<EditText>(R.id.editTextDialog)

        AlertDialog.Builder(requireContext())
            .setTitle("Choose Number of Days")
            .setView(dialogView)
            .setPositiveButton("Update") { dialog, _ ->
                val days = editText.text.toString().toIntOrNull() ?: 30
                binding.tvDaysChooser.text = "Last $days Days"
                calculateRevenueAndDrawChart(days, binding.barChart, binding.textViewValue1, binding.textViewValue2)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }


    private fun updateGridItems() {
        CoroutineScope(Dispatchers.Main).launch {
            val totalStockDeffered = async { getTotalItemsInStock() }
            val totalOrdersDeferred = async { getTotalNumberOfOrders() }
            val totalPendingDeferred = async { getTotalOrdersWithStatus("Pending") }
            val totalAcceptedDeferred = async { getTotalOrdersWithStatus("Order accepted") }
            val totalDeliveringDeferred = async { getTotalOrdersWithStatus("On delivery") }
            val totalDeliveredDeferred = async { getTotalOrdersWithStatus("Delivered") }

            val totalStock = totalStockDeffered.await()
            val totalOrders = totalOrdersDeferred.await()
            val totalPending = totalPendingDeferred.await()
            val totalAccepted = totalAcceptedDeferred.await()
            val totalDelivering = totalDeliveringDeferred.await()
            val totalDelivered = totalDeliveredDeferred.await()

            // Now update the UI with totalOrders and totalPending
            val myGridAdapter = MyGridAdapter(
                arrayOf(
                    MyGridAdapter.GridItem("In Stock", totalStock, R.drawable.ic_in_stock),
                    MyGridAdapter.GridItem("Total Orders", totalOrders, R.drawable.ic_total_orders),
                    MyGridAdapter.GridItem("Pending", totalPending, R.drawable.ic_box_pending),
                    MyGridAdapter.GridItem("Accepted", totalAccepted, R.drawable.ic_box_accepted),
                    MyGridAdapter.GridItem("On Delivery", totalDelivering, R.drawable.ic_box_delivering),
                    MyGridAdapter.GridItem("Delivered", totalDelivered, R.drawable.ic_box_delivered),
                )
            )
            binding.myGridRecyclerView.adapter = myGridAdapter
            val spanCount = 2 // Number of columns
            binding.myGridRecyclerView.layoutManager = GridLayoutManager(requireContext(), spanCount)
            val spacing = 60 // Spacing in pixels
            val includeEdge = false
            binding.myGridRecyclerView.addItemDecoration(GridSpacingItemDecoration(spanCount, spacing, includeEdge))
        }
    }

    private suspend fun getTotalItemsInStock(): Int = withContext(Dispatchers.IO) {
        val productsRef = mDBProductRef
        try {
            val snapshot = productsRef.get().await()
            val totalStock = snapshot.documents.sumOf { document ->
                document.getLong("stock")?.toInt() ?: 0 // Assuming each document has a "stock" field
            }
            return@withContext totalStock
        } catch (e: Exception) {
            Log.e("AdminStats", "Failed to fetch total items in stock", e)
            return@withContext 0
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
        barChart: BarChart,
        textViewValue1: TextView,
        textViewValue2: TextView
    ) {
        // Calculate the start and end date for the query
        val startCalendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -days + 1) // Adjust to start from the beginning of the 'days' period
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startDate = startCalendar.time
//        Log.d("calculateRevenue", "Start date: $startDate")

        val endCalendar = Calendar.getInstance().apply {
            // Set to the end of the current day, regardless of 'days' since we're including today
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val endDate = endCalendar.time
//        Log.d("calculateRevenue", "End date: $endDate")

        // Initialize a map to hold revenue per day
        val revenuePerDay = mutableMapOf<String, Double>()

        // Prepare to format dates as "dd-MM"
        val dateFormat = SimpleDateFormat("dd-MM", Locale.getDefault())

        // Query orders that were created within the specified time frame
//        val deliveredOrders = ordersRef.whereEqualTo("orderStatus", "Delivered")
//        Log.d("Test00", "calculateRevenueAndDrawChart: $deliveredOrders")
//        deliveredOrders.whereGreaterThanOrEqualTo("date", startDate)
//            .whereLessThanOrEqualTo("date", endDate).get()
//            .addOnSuccessListener { orderSnapshots ->
////                Log.d("calculateRevenue", "Number of orders: ${orderSnapshots.size()}")
//                if (orderSnapshots.isEmpty) {
//                    // No orders found, proceed to update the chart with no data
//                    updateBarChartAndTextViews(barChart, revenuePerDay, days, textViewValue1, textViewValue2)
//                    return@addOnSuccessListener
//                }
//
//                orderSnapshots.forEach { orderDoc ->
//                    val totalAmount = orderDoc.getDouble("totalAmount") ?: 0.0
//                    val orderDate = orderDoc.getDate("date")
////                    val formattedDate = dateFormat.format(orderDate)
////                    revenuePerDay[formattedDate] = revenuePerDay.getOrDefault(formattedDate, 0.0) + totalAmount
//                    val formattedDate = if (orderDate != null) dateFormat.format(orderDate) else "Unknown Date"
//                    revenuePerDay[formattedDate] = revenuePerDay.getOrDefault(formattedDate, 0.0) + totalAmount
//
//                }
//
//                // All orders processed, update the chart and TextViews
//                updateBarChartAndTextViews(barChart, revenuePerDay, days, textViewValue1, textViewValue2)
//            }
//            .addOnFailureListener { e ->
//                Log.e("calculateRevenue", "Error fetching orders", e)
//                // Error fetching orders, update the chart with no data
//                updateBarChartAndTextViews(barChart, revenuePerDay, days, textViewValue1, textViewValue2)
//            }
        mDBOrderRef
            .whereEqualTo("orderStatus", "Delivered")
            .whereGreaterThanOrEqualTo("date", startDate)
            .whereLessThanOrEqualTo("date", endDate)
            .get()
            .addOnSuccessListener { orderSnapshots ->
//                Log.d("calculateRevenue", "Number of orders: ${orderSnapshots.size()}")

                orderSnapshots.forEach { orderDoc ->
                    val totalAmount = orderDoc.getDouble("totalAmount") ?: 0.0
                    val orderDate = orderDoc.getDate("date")
//                    Log.d("calculateRevenue", "Order date from Firestore: $orderDate")

                    val formattedDate = if (orderDate != null) dateFormat.format(orderDate) else "Unknown Date"
//                    Log.d("calculateRevenue", "Formatted date: $formattedDate and total amount: $totalAmount")

                    revenuePerDay[formattedDate] = revenuePerDay.getOrDefault(formattedDate, 0.0) + totalAmount
                }

//                Log.d("calculateRevenue", "Revenue per day: $revenuePerDay")
                updateBarChartAndTextViews(barChart, revenuePerDay, days, textViewValue1, textViewValue2)
            }
            .addOnFailureListener { e ->
                Log.e("calculateRevenue", "Error fetching orders", e)
                updateBarChartAndTextViews(barChart, revenuePerDay, days, textViewValue1, textViewValue2)
            }
    }


    private fun updateBarChartAndTextViews(
        barChart: BarChart,
        revenuePerDay: Map<String, Double>,
        days: Int,
        textViewValue1: TextView,
        textViewValue2: TextView
    ) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        // Sort the map by date to ensure the data is plotted in chronological order
        val sortedRevenuePerDay = revenuePerDay.toSortedMap()

        // Convert the revenue per day into chart entries
        sortedRevenuePerDay.keys.forEachIndexed { index, date ->
            entries.add(BarEntry(index.toFloat(), sortedRevenuePerDay[date]!!.toFloat()))
            labels.add(date)
        }

        val dataSet = BarDataSet(entries, "Daily Total Sales")
        dataSet.color = ContextCompat.getColor(requireContext(), R.color.primary)
        val barData = BarData(dataSet)

        // Set the labels to the XAxis
        barChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            granularity = 1f // Only show integer values
        }

        barChart.data = barData
        barChart.axisLeft.axisMinimum = 0f // Start from zero
        barChart.axisRight.isEnabled = false // Disable right axis
        barChart.description.isEnabled = false // Disable the description
        barChart.legend.isEnabled = false // Enable the legend if desired
//        barChart.legend.apply {
//            // You can use yOffset to move the legend up or down (negative values to move up)
//            yOffset = 10f
//        }
        barChart.invalidate() // Refresh the chart

        // Calculate total and average revenue
        val totalRevenue = sortedRevenuePerDay.values.sum()
        val averageRevenuePerDay = if (days > 0) totalRevenue / days else 0.0

        // Update the TextViews
        textViewValue1.text = getString(R.string.formatted_currency_dollar, totalRevenue)
        textViewValue2.text = getString(R.string.formatted_currency_dollar, averageRevenuePerDay)
    }
}