// Inventory.java
package com.example.posandinventorysystem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import android.text.Editable;
import android.text.TextWatcher;

import java.util.Calendar;

public class Inventory extends AppCompatActivity {

    private static final int EDIT_PRODUCT_REQUEST_CODE = 1;
    DBHelper DB;
    ListView listView;
    CustomAdapter adapter;

    Button filter;
    TextView txtNumberOfItems;
    TextView txtSumOfItems;
    TextView txtTotalEarnings;
    TextView txtItemsPurchased;

    // Inventory.java
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);
        Log.d("Inventory", "Inventory");

        filter = findViewById(R.id.btnFilter);
        txtNumberOfItems = findViewById(R.id.txtNumberOfItems);
        txtSumOfItems = findViewById(R.id.txtSumOfItems);
        txtTotalEarnings = findViewById(R.id.totalEarning); // Initialize totalEarnings TextView
        txtItemsPurchased = findViewById(R.id.itemPurchased); // Initialize itemsPurchased TextView

        // Retrieve filter options from intent extras
        boolean ascending = getIntent().getBooleanExtra("ascending", false);
        String sortBy = getIntent().getStringExtra("sortBy");
        String selectedCategory = getIntent().getStringExtra("selectedCategory");
        if (selectedCategory == null) {
            // If selectedCategory is null, initialize it with an empty string or handle it accordingly
            selectedCategory = "";
        }
        // Initialize DBHelper
        DB = new DBHelper(this);
        listView = findViewById(R.id.firstListView);


        // Retrieve data from the database based on filter options and selected category
        // Inventory.java (Inside onCreate or onResume method)
        Cursor cursor = DB.getAllProductDetails(selectedCategory, "", sortBy, ascending, false, 0);
        if (cursor != null) {
            String[] columnNames = cursor.getColumnNames();
            for (String columnName : columnNames) {
                Log.d("Cursor Columns", columnName);
            }
        }
        adapter = new CustomAdapter(this, cursor, txtTotalEarnings, txtItemsPurchased);
        listView.setAdapter(adapter);


        EditText searchEditText = findViewById(R.id.searchEditText);

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
                searchProducts(searchText);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // This method is called after the text is changed
            }
        });

        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Inventory.this, FilterProduct.class);
                startActivity(intent);
            }
        });

        // Set item click listener for the ListView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the clicked item data
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);

                String productName = cursor.getString(cursor.getColumnIndex("prodName"));
                String productCategory = cursor.getString(cursor.getColumnIndex("prodCate"));
                String productBarcode = cursor.getString(cursor.getColumnIndex("prodBarcode"));
                String imageUriString = cursor.getString(cursor.getColumnIndex("prodImageUri"));

                // Pass the selected category and other data to EditProduct activity
                Intent intent = new Intent(Inventory.this, EditProduct.class);
                intent.putExtra("productName", productName);
                intent.putExtra("productCategory", productCategory);
                intent.putExtra("productBarcode", productBarcode);
                intent.putStringArrayListExtra("categories", DB.getCategories());
                intent.putExtra("position", position);
                intent.putExtra("imageUriString", imageUriString);
                startActivityForResult(intent, EDIT_PRODUCT_REQUEST_CODE);
                Log.d("Inventory", "Selected product category: " + productCategory);

            }
        });

        // Calculate and display total number of items and total sum of prices multiplied by quantities
        calculateTotals();
        resetTotalsIfNewMonth();

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Retrieve filter options from intent extras
        boolean ascending = getIntent().getBooleanExtra("ascending", false);
        String sortBy = getIntent().getStringExtra("sortBy");
        String selectedCategory = getIntent().getStringExtra("selectedCategory");
        if (selectedCategory == null) {
            selectedCategory = "";
        }
        boolean lowStockOnly = getIntent().getBooleanExtra("lowStockOnly", false);
        int stockValue = getIntent().getIntExtra("stockValue", 0);

        // Retrieve data from the database based on filter options, selected category, and stock value
        Cursor cursor = DB.getAllProductDetails(selectedCategory, "", sortBy, ascending, lowStockOnly, stockValue);
        adapter.changeCursor(cursor);
        adapter.notifyDataSetChanged();

        // Recalculate and display total number of items and total sum of prices multiplied by quantities
        calculateTotals();
        resetTotalsIfNewMonth();
    }


    private void searchProducts(String searchText) {
        // Retrieve filter options from intent extras
        boolean ascending = getIntent().getBooleanExtra("ascending", false);
        String sortBy = getIntent().getStringExtra("sortBy");

        Cursor cursor = DB.getAllProductDetails("", searchText, sortBy, ascending, false, 0);
        adapter.changeCursor(cursor);
        adapter.notifyDataSetChanged();
    }

    // Method to calculate and display total number of items and total sum of prices multiplied by quantities
    // Method to calculate and display total number of items and total sum of prices multiplied by quantities
    private void calculateTotals() {
        int totalItems = adapter.getCount();
        double totalSum = 0;

        Cursor cursor = adapter.getCursor();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                double price = cursor.getDouble(cursor.getColumnIndex("prodPrice"));
                int quantity = cursor.getInt(cursor.getColumnIndex("prodQuantity"));
                totalSum += price * quantity;
            } while (cursor.moveToNext());
        }

        txtNumberOfItems.setText(String.valueOf(totalItems));
        txtSumOfItems.setText(String.format("%.2f", totalSum));
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_PRODUCT_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            int position = data.getIntExtra("position", -1);
            if (position != -1) {
                // Retrieve filter options from intent extras
                boolean ascending = getIntent().getBooleanExtra("ascending", false);
                String sortBy = getIntent().getStringExtra("sortBy");

                // Reload the data in the ListView adapter to reflect the changes
                Cursor cursor = DB.getAllProductDetails("", "", sortBy, ascending, false, 0);
                adapter.changeCursor(cursor);
                adapter.notifyDataSetChanged();

                // Recalculate and display total number of items and total sum of prices multiplied by quantities
                calculateTotals();
            }
        }
    }

    private void updateInventory(String productName, int purchaseQuantity) {
        // Assuming you have a way to get the itemQuantity and itemPurchased TextViews
        View inventoryItemView = getInventoryItemViewByName(productName);
        if (inventoryItemView != null) {
            TextView itemQuantityView = inventoryItemView.findViewById(R.id.itemQuantity);
            TextView itemPurchasedView = inventoryItemView.findViewById(R.id.itemPurchased);

            int currentQuantity = Integer.parseInt(itemQuantityView.getText().toString());
            int currentPurchased = Integer.parseInt(itemPurchasedView.getText().toString());

            int newQuantity = currentQuantity - purchaseQuantity;
            int newPurchased = currentPurchased + purchaseQuantity;

            itemQuantityView.setText(String.valueOf(newQuantity));
            itemPurchasedView.setText(String.valueOf(newPurchased));
        }
    }


    private View getInventoryItemViewByName(String productName) {
        // Implement a method to find the corresponding inventory item view by product name
        // This could be done by iterating through the list items in the inventory and matching the product name
        for (int i = 0; i < listView.getChildCount(); i++) {
            View child = listView.getChildAt(i);
            TextView itemNameView = child.findViewById(R.id.itemName);
            if (itemNameView.getText().toString().equals(productName)) {
                return child;
            }
        }
        return null;
    }

    private void resetTotalsIfNewMonth() {
        // Get the current date
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);

        // Get the last reset date from SharedPreferences
        SharedPreferences preferences = getSharedPreferences("InventoryPrefs", Context.MODE_PRIVATE);
        int lastResetMonth = preferences.getInt("lastResetMonth", -1);
        int lastResetYear = preferences.getInt("lastResetYear", -1);

        // Check if it's a new month
        if (currentMonth != lastResetMonth || currentYear != lastResetYear) {
            // Reset the total earnings and items purchased in the database
            DB.resetTotalEarningsAndItemsPurchased();

            // Update the last reset date in SharedPreferences
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("lastResetMonth", currentMonth);
            editor.putInt("lastResetYear", currentYear);
            editor.apply();
        }
    }
}