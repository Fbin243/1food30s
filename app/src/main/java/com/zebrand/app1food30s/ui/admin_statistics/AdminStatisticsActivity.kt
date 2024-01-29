package com.zebrand.app1food30s.ui.admin_statistics

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zebrand.app1food30s.R

class AdminStatisticsActivity : AppCompatActivity() {

    // Lateinit properties for RecyclerViews, will be initialized in onCreate
    private lateinit var myGridRecyclerView: RecyclerView
    private lateinit var anotherRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Setting the content view to the layout defined in activity_admin_statistics.xml
        setContentView(R.layout.activity_admin_statistics)

        // Setup for the Grid RecyclerView
        // Find the RecyclerView widget as defined in the XML layout
        myGridRecyclerView = findViewById(R.id.my_grid_recycler_view)
        // Create an adapter for the grid RecyclerView with predefined data
        // Modified to contain 6 elements for a 3x2 grid
        val myGridAdapter = MyGridAdapter(arrayOf("Data 1", "Data 2", "Data 3", "Data 4", "Data 5", "Data 6"))
        // Setting the adapter to the RecyclerView
        myGridRecyclerView.adapter = myGridAdapter
        // Define the layout as a GridLayoutManager with 2 columns
        myGridRecyclerView.layoutManager = GridLayoutManager(this, 2)
        // Improve performance by setting to a fixed size, as changes in content do not change the layout size
        myGridRecyclerView.setHasFixedSize(true)


//        // Setup for another RecyclerView with a linear layout
//        // Find the second RecyclerView widget as defined in the XML layout
//        anotherRecyclerView = findViewById(R.id.another_recycler_view) // Ensure this ID matches the one in your XML layout
//        // Create an adapter for this RecyclerView with a different set of data
//        val anotherAdapter = MyAdapter(arrayOf("Item 1", "Item 2", "Item 3", "Item 4"))
//        // Setting the adapter to the RecyclerView
//        anotherRecyclerView.adapter = anotherAdapter
//        // Define the layout as a LinearLayoutManager
//        anotherRecyclerView.layoutManager = LinearLayoutManager(this)
//        // Improve performance by setting to a fixed size, as changes in content do not change the layout size
//        anotherRecyclerView.setHasFixedSize(true)
    }
}
