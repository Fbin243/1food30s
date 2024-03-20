package com.zebrand.app1food30s.ui.manage_product

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.zebrand.app1food30s.data.Category
import com.zebrand.app1food30s.data.Product
import com.zebrand.app1food30s.R // Change to your actual package name
import java.util.Date

class ManageProductDetailActivity : AppCompatActivity() {

    private lateinit var nameEditText: TextInputEditText
    private lateinit var priceEditText: TextInputEditText
    private lateinit var stockEditText: TextInputEditText
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var createButton: Button
    private lateinit var categorySpinner: Spinner
    private lateinit var offerSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_product_detail)


        nameEditText = findViewById(R.id.input_name)
        priceEditText = findViewById(R.id.input_price)
        stockEditText = findViewById(R.id.input_stock)
        descriptionEditText = findViewById(R.id.input_description)
        createButton = findViewById(R.id.create_btn)
        categorySpinner = findViewById(R.id.category_spinner)
        offerSpinner = findViewById(R.id.offer_spinner)
        loadCategoriesFromFirebase()
        loadOffersFromFirebase()

        createButton.setOnClickListener {
            saveProductToFirestore()
        }
    }

    private fun saveProductToFirestore() {
        // Collect product data from UI
        val productName = nameEditText.text.toString().trim()
        val productPrice = priceEditText.text.toString().toDoubleOrNull() ?: 0.0
        val productStock = stockEditText.text.toString().toIntOrNull() ?: 0
        val productDescription = descriptionEditText.text.toString().trim()
        val selectedCategoryName = categorySpinner.selectedItem.toString()
        val selectedOfferName = offerSpinner.selectedItem.toString()

        val db = Firebase.firestore

        // Chain of tasks to get category ref, then offer ref, then save product
        db.collection("categories").whereEqualTo("name", selectedCategoryName).limit(1).get()
            .continueWithTask { task ->
                val categoryId = task.result.documents.firstOrNull()?.id
                if (categoryId != null) {
                    db.collection("offers").whereEqualTo("name", selectedOfferName).limit(1).get()
                        .continueWith { offerTask ->
                            val offerId = offerTask.result.documents.firstOrNull()?.id
                            Pair(categoryId, offerId)
                        }
                } else {
                    throw Exception("Category not found")
                }
            }.addOnSuccessListener { (categoryId, offerId) ->
                if (categoryId != null && offerId != null) {
                    val newProduct = Product(
                        name = productName,
                        idCategory = db.document("categories/$categoryId"),
                        idOffer = db.document("offers/$offerId"),
                        price = productPrice,
                        description = productDescription,
                        stock = productStock,
                        date = Date() // Assuming other fields like 'image' are handled elsewhere
                    )
                    db.collection("products").add(newProduct)
                        .addOnSuccessListener {
                            // Successfully saved product
                        }
                        .addOnFailureListener {
                            // Failed to save product
                        }
                } else {
                    // Handle case where category or offer doesn't exist
                }
            }.addOnFailureListener {
                // Handle failure in getting category or offer
            }
    }

    // Dummy functions, replace these with your actual logic for retrieving document IDs based on the spinner selections
    private fun fetchCategoryReference(categoryName: String): DocumentReference {
        // Your logic here to convert categoryName to a Firestore DocumentReference
        // This is just a placeholder; replace it with your actual Firestore reference retrieval logic
        val db = Firebase.firestore
        return db.collection("categories").document() // Replace with actual document ID based on categoryName
    }

    private fun fetchOfferReference(offerName: String): DocumentReference {
        // Your logic here to convert offerName to a Firestore DocumentReference
        // This is just a placeholder; replace it with your actual Firestore reference retrieval logic
        val db = Firebase.firestore
        return db.collection("offers").document() // Replace with actual document ID based on offerName
    }


    private fun loadCategoriesFromFirebase() {
        val db = Firebase.firestore
        val categoriesList = ArrayList<String>()

        db.collection("categories").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
//                    val category = document.toObject(Category::class.java)
                    categoriesList.add(document.getString("name") ?: "")
                }
                // Update the spinner
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoriesList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                categorySpinner.adapter = adapter
            }
            .addOnFailureListener { exception ->
                // Handle any errors here, for example:
                // Log.d("ManageProductDetailActivity", "Error getting documents: ", exception)
            }
    }

    private fun loadOffersFromFirebase() {
        val db = Firebase.firestore
        val offersList = ArrayList<String>()

        db.collection("offers").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
//                    val category = document.toObject(Category::class.java)
                    offersList.add(document.getString("name") ?: "")
                }
                // Update the spinner
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, offersList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                offerSpinner.adapter = adapter
            }
            .addOnFailureListener { exception ->
                // Handle any errors here, for example:
                // Log.d("ManageProductDetailActivity", "Error getting documents: ", exception)
            }
    }
}
