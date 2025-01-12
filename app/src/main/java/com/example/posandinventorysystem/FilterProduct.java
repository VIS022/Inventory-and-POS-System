package com.example.posandinventorysystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

public class FilterProduct extends AppCompatActivity {

    RadioGroup radioGroupSortOrder;
    RadioButton radioAscending, radioName, radioPrice, radioDescending, radioEarnings; // Add other RadioButtons if needed
    Button buttonGoBack, btnHome;
    Spinner spinnerCategories;
    EditText editTextStock;
    CheckBox cbLowStock;
    DBHelper DB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_product);

        buttonGoBack = findViewById(R.id.btnBacktoInv);
        btnHome = findViewById(R.id.btnBacktoHome);
        radioGroupSortOrder = findViewById(R.id.radioGroupSortOrder);
        radioAscending = findViewById(R.id.radioAscending);
        radioDescending = findViewById(R.id.radioDescending);
        radioName = findViewById(R.id.radioName);
        radioPrice = findViewById(R.id.radioPrice);
        RadioButton radioPurchase = findViewById(R.id.radioPurchase); // Add this line
        spinnerCategories = findViewById(R.id.spCateBy);
        cbLowStock = findViewById(R.id.cbLowStock);
        editTextStock = findViewById(R.id.editTextStock);
        radioEarnings = findViewById(R.id.radioEarnings);

        DB = new DBHelper(this);

        // Populate the Spinner with categories from the database
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, DB.getAllCategories());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategories.setAdapter(adapter);

        // Set the default selection to "All Categories"
        spinnerCategories.setSelection(0); // Assuming "All Categories" is at index 0

        // Listener for the "Go Back" button
        buttonGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the selected category from the spinner
                String selectedCategory = spinnerCategories.getSelectedItem().toString();
                boolean ascending = radioAscending.isChecked();
                String sortBy = "";

                // Determine the sortBy criteria
                if (radioName.isChecked()) {
                    sortBy = "UPPER(prodName)";
                } else if (radioPrice.isChecked()) {
                    sortBy = "prodPrice";
                } else if (radioPurchase.isChecked()) {
                    sortBy = "prodPurchased";
                } else if (radioEarnings.isChecked()) {
                    sortBy = "(prodPurchased * prodPrice) - (prodPurchased * prodCapital)";
                }

                // Get the stock value from the editTextStock
                int stockValue = 0;
                if (cbLowStock.isChecked()) {
                    String stockText = editTextStock.getText().toString();
                    if (!stockText.isEmpty()) {
                        stockValue = Integer.parseInt(stockText);
                    }
                }

                Intent intent = new Intent(FilterProduct.this, Inventory.class);
                intent.putExtra("ascending", ascending);
                intent.putExtra("sortBy", sortBy);
                intent.putExtra("selectedCategory", selectedCategory.equals("All Categories") ? "" : selectedCategory);
                intent.putExtra("lowStockOnly", cbLowStock.isChecked());
                intent.putExtra("stockValue", stockValue);
                startActivity(intent);
            }
        });


        // Listener for the "Home" button
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FilterProduct.this, Home.class);
                startActivity(intent);
            }
        });
    }

}


