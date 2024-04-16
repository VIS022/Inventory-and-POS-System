package com.example.posandinventorysystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Home extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button btnAddProduct = findViewById(R.id.product);
        Button btnAddCategory = findViewById(R.id.category);
        Button btnViewInventory = findViewById(R.id.viewinventory);
        Button btnPos = findViewById(R.id.pos);
        Button btnEarnings = findViewById(R.id.earning);
        Button btnExit = findViewById(R.id.exit);

        btnAddProduct.setOnClickListener(this);
        btnAddCategory.setOnClickListener(this);
        btnViewInventory.setOnClickListener(this);
        btnPos.setOnClickListener(this);
        btnEarnings.setOnClickListener(this);
        btnExit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        if (v.getId() == R.id.product) {
            intent = new Intent(this, AddProduct.class);
        } else if (v.getId() == R.id.category) {
            intent = new Intent(this, CategoryList.class);
        } else if (v.getId() == R.id.viewinventory) {
            intent = new Intent(this, Inventory.class);
        } else if (v.getId() == R.id.pos) {
            intent = new Intent(this, POS.class);
        } else if (v.getId() == R.id.earning) {
            intent = new Intent(this, Earnings.class);
        } else if (v.getId() == R.id.exit) {
            finish(); // Close the current activity
            return; // Exit the method
        }

        if (intent != null) {
            startActivity(intent);
        }
    }
}
