// POS.java
package com.example.posandinventorysystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;

public class POS extends AppCompatActivity {

    ListView listView;
    ProductListAdapter adapter;
    EditText searchEditText;
    ImageView addtoCartIcon;
    ArrayList<String> originalProductData;
    ArrayList<String> originalImageUris;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pos);

        addtoCartIcon = findViewById(R.id.ImgAddtoCart);

        addtoCartIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(POS.this, Cart.class);
                startActivity(intent);
            }
        });

        // Initialize ListView
        listView = findViewById(R.id.PosListView);

        // Fetch product data (name, price, and image URI) from Inventory using DBHelper
        ArrayList<String> productData = fetchProductData();

        // Initialize search EditText
        searchEditText = findViewById(R.id.PosSEARCH);

        // Add a text change listener to the search EditText
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // This method is called before the text is changed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // This method is called when the text is changed
                String searchText = s.toString();
                filterProductData(searchText);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // This method is called after the text is changed
            }
        });
    }

    // Method to fetch product data (name, price, and image URI) from Inventory using DBHelper
    private ArrayList<String> fetchProductData() {
        ArrayList<String> productData = new ArrayList<>();
        ArrayList<String> imageUris = new ArrayList<>();

        // Retrieve product data from the DBHelper
        DBHelper dbHelper = new DBHelper(this);
        Cursor cursor = dbHelper.getAllProductDetails("", "", "", true, false, 0); // Fetch all products

        // Inside the fetchProductData() method in the POS class
        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Retrieve product name, price, and image URI from the cursor
                String productName = cursor.getString(cursor.getColumnIndex("prodName"));
                double productPrice = cursor.getDouble(cursor.getColumnIndex("prodPrice"));
                String imageUri = cursor.getString(cursor.getColumnIndex("prodImageUri"));

                // Format the product data as "Product Name - Price"
                String productInfo = productName + " - ₱" + String.format("%.2f", productPrice);
                productData.add(productInfo); // Add product data to the ArrayList
                imageUris.add(imageUri); // Add image URI to the ArrayList
            } while (cursor.moveToNext());
            cursor.close(); // Close cursor after use
        }

        // Store the original product data and image URIs
        originalProductData = new ArrayList<>(productData);
        originalImageUris = new ArrayList<>(imageUris);

        // Create and set adapter for ListView
        adapter = new ProductListAdapter(this, R.layout.custom_product_item, productData, imageUris, false);
        listView.setAdapter(adapter);

        return productData; // Return the ArrayList containing product data
    }

    // Method to filter product data based on search query
    private void filterProductData(String query) {
        if (query.isEmpty()) {
            // If search query is empty, show all original product data
            adapter.updateData(originalProductData, originalImageUris);
        } else {
            ArrayList<String> filteredData = new ArrayList<>();
            ArrayList<String> filteredImageUris = new ArrayList<>();

            // Iterate through all original product data
            for (int i = 0; i < originalProductData.size(); i++) {
                String data = originalProductData.get(i);
                // Check if product name contains the search query
                if (data.toLowerCase().contains(query.toLowerCase())) {
                    filteredData.add(data); // Add matching product data to filteredData
                    filteredImageUris.add(originalImageUris.get(i)); // Add corresponding image URI to filteredImageUris
                }
            }

            // Update ListView adapter with filtered data and image URIs
            adapter.updateData(filteredData, filteredImageUris);
        }
    }
}
