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
import com.zebrand.app1food30s.data.Offer
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

        // Initialize UI components
        nameEditText = findViewById(R.id.edit_name)
        priceEditText = findViewById(R.id.price)
        stockEditText = findViewById(R.id.stock)
        descriptionEditText = findViewById(R.id.edit_description)
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
        val productName = nameEditText.text.toString().trim()
        val productPrice = priceEditText.text.toString().toDoubleOrNull() ?: 0.0
        val productStock = stockEditText.text.toString().toIntOrNull() ?: 0
        val productDescription = descriptionEditText.text.toString().trim()

        // Assuming you replace these methods with ones that map names to Firestore document references
        val categoryRef = fetchCategoryReference(categorySpinner.selectedItem.toString())
        val offerRef = fetchOfferReference(offerSpinner.selectedItem.toString())

        val newProduct = Product(
            name = productName,
            idCategory = categoryRef,
            idOffer = offerRef,
            price = productPrice,
            description = productDescription,
            stock = productStock,
            image = "", // This should be replaced with the actual image path after uploading the image to Firestore
            sold = 0,
            reviews = emptyList(),
            date = Date()
        )

        val db = Firebase.firestore
        db.collection("products").add(newProduct)
            .addOnSuccessListener { documentReference ->
                // Handle success, e.g., display a success message or navigate to another screen
            }
            .addOnFailureListener { e ->
                // Handle failure, e.g., display an error message
            }
    }

    private fun fetchCategoryReference(categoryName: String): DocumentReference {
        val db = Firebase.firestore
        // Implement the logic to convert categoryName to Firestore DocumentReference
        // This is a placeholder. Replace it with actual logic.
        return db.collection("categories").document() // Replace with the correct document reference
    }

    private fun fetchOfferReference(offerName: String): DocumentReference {
        val db = Firebase.firestore
        // Implement the logic to convert offerName to Firestore DocumentReference
        // This is a placeholder. Replace it with actual logic.
        return db.collection("offers").document() // Replace with the correct document reference
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
