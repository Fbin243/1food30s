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
        val productName = nameEditText.text.toString().trim()
        val productPrice = priceEditText.text.toString().toDoubleOrNull() ?: 0.0
        val productStock = stockEditText.text.toString().toIntOrNull() ?: 0
        val productDescription = descriptionEditText.text.toString().trim()
        val db = Firebase.firestore

        // Get the selected names from the spinners
        val selectedCategoryName = categorySpinner.selectedItem.toString()
        val selectedOfferName = offerSpinner.selectedItem.toString()

        // First, find the category reference
        db.collection("categories").whereEqualTo("name", selectedCategoryName).limit(1).get()
            .addOnSuccessListener { categoryDocuments ->
                if (categoryDocuments.documents.isNotEmpty()) {
                    val categoryDocumentRef = categoryDocuments.documents.first().reference

                    // Then, find the offer reference
                    db.collection("offers").whereEqualTo("name", selectedOfferName).limit(1).get()
                        .addOnSuccessListener { offerDocuments ->
                            if (offerDocuments.documents.isNotEmpty()) {
                                val offerDocumentRef = offerDocuments.documents.first().reference

                                // Create a new document reference with a unique ID in the "products" collection
                                val newProductRef = db.collection("products").document()

                                // Create the product with the new document's ID
                                val newProduct = Product(
                                    id = newProductRef.id,
                                    name = productName,
                                    idCategory = categoryDocumentRef,
                                    idOffer = offerDocumentRef,
                                    price = productPrice,
                                    description = productDescription,
                                    stock = productStock,
                                    image = "", // Update this after image upload if necessary
                                    date = Date() // Current date
                                )

                                // Set the new document with the new product
                                newProductRef.set(newProduct)
                                    .addOnSuccessListener {
                                        // Handle success, e.g., show a success message to the user
                                    }
                                    .addOnFailureListener { e ->
                                        // Handle failure, e.g., show an error message to the user
                                    }
                            } else {
                                // Handle the case where no matching offer is found
                            }
                        }
                        .addOnFailureListener { exception ->
                            // Handle any errors in finding the offer
                        }
                } else {
                    // Handle the case where no matching category is found
                }
            }
            .addOnFailureListener { exception ->
                // Handle any errors in finding the category
            }
    }


    // Dummy functions, replace these with your actual logic for retrieving document IDs based on the spinner selections



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
