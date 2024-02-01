package com.zebrand.app1food30s.ui.admin_statistics

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.zebrand.app1food30s.R

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
        }

        // Define the layout as a GridLayoutManager with 2 columns
        val spanCount = 2 // Number of columns
        myGridRecyclerView.layoutManager = GridLayoutManager(this, spanCount)

        // Add item decoration for spacing
        val spacing = 60 // Spacing in pixels
        val includeEdge = true
        myGridRecyclerView.addItemDecoration(GridSpacingItemDecoration(spanCount, spacing, includeEdge))

        // Improve performance by setting to a fixed size, as changes in content do not change the layout size
        myGridRecyclerView.setHasFixedSize(true)
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
