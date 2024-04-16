package com.example.posandinventorysystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class Inventory extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        Button buttonFilter = findViewById(R.id.btnFilter);

        buttonFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Inventory.this, FilterProduct.class);
                startActivity(intent);
            }
        });

        ListView listView = findViewById(R.id.firstListView);
        String[] names = {
                "Product Name:\n" +
                        "Category:\n" +
                        "Price:\n" +
                        "Quantity:\n" +
                        "Capital\n" +
                        "Total Price\n" +
                        "Barcode Number:\n" +
                        "Item Left:\n" +
                        "Item Purchased:"
        };
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, names
        );
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Open EditProduct class here
                Intent intent = new Intent(Inventory.this, EditProduct.class);
                startActivity(intent);
            }
        });

    }

}