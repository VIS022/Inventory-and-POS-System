package com.example.posandinventorysystem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

public class AddProduct extends AppCompatActivity {

    EditText prodName, prodPrice, prodQuantity, prodCapital, prodBar, editCateName;
    Spinner prodCate;
    Button btnAddSave, addCateButton;
    ArrayAdapter<String> adapter;
    DBHelper DB;
    private static final int REQUEST_CODE_ADD_CATEGORY = 1;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;

    ImageView insertImgProd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        prodName = findViewById(R.id.EditTextName);
        prodPrice = findViewById(R.id.EditTextPrice);
        prodQuantity = findViewById(R.id.EditTextQuantity);
        prodCapital = findViewById(R.id.EditTextCapital);
        prodCate = findViewById(R.id.addtxtSpinner);
        prodBar = findViewById(R.id.EditTextBar);

        btnAddSave = findViewById(R.id.btnAddSave);

        insertImgProd = findViewById(R.id.insertImgProd);
        insertImgProd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        DB = new DBHelper(this);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prodCate.setAdapter(adapter);

        loadCategories();

        btnAddSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String prodNameTXT = prodName.getText().toString().trim();
                String prodCateSpin = prodCate.getSelectedItem() != null ? prodCate.getSelectedItem().toString() : "";
                String prodPriceTXT = prodPrice.getText().toString().trim();
                String prodQuantityTXT = prodQuantity.getText().toString().trim();
                String prodBarTXT = prodBar.getText().toString().trim();
                String prodCapitalTXT = prodCapital.getText().toString().trim();

                String imageUriString = selectedImageUri != null ? selectedImageUri.toString() : null;

                if (prodNameTXT.isEmpty() || prodPriceTXT.isEmpty() || prodQuantityTXT.isEmpty() || prodBarTXT.isEmpty() || prodCapitalTXT.isEmpty()) {
                    Toast.makeText(AddProduct.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (prodCateSpin.isEmpty()) {
                    Toast.makeText(AddProduct.this, "Please select a category", Toast.LENGTH_SHORT).show();
                    return;
                }

                double prodPriceDouble = 0;
                try {
                    prodPriceDouble = Double.parseDouble(prodPriceTXT);
                } catch (NumberFormatException e) {
                    Toast.makeText(AddProduct.this, "Please enter a valid price", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (prodQuantityTXT.equals("0")) {
                    Toast.makeText(AddProduct.this, "Please input valid quantity", Toast.LENGTH_SHORT).show();
                    return;
                }

                int prodQuantityInt = 0;
                try {
                    prodQuantityInt = Integer.parseInt(prodQuantityTXT);
                } catch (NumberFormatException e) {
                    Toast.makeText(AddProduct.this, "Please input valid quantity", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (prodCapitalTXT.equals("0")) {
                    Toast.makeText(AddProduct.this, "Please input valid capital", Toast.LENGTH_SHORT).show();
                    return;
                }

                double prodCapitalDouble = 0;
                try {
                    prodCapitalDouble = Double.parseDouble(prodCapitalTXT);
                } catch (NumberFormatException e) {
                    Toast.makeText(AddProduct.this, "Please input valid capital", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean checkInsertData = DB.insertProdDetails(prodNameTXT, prodCateSpin, prodPriceDouble, prodQuantityInt, prodBarTXT, prodCapitalDouble, imageUriString);
                if (checkInsertData) {
                    Toast.makeText(AddProduct.this, "Item added successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AddProduct.this, Inventory.class);
                    intent.putExtra("itemAdded", true);
                    // Pass the image URI string to the Inventory activity
                    intent.putExtra("imageUri", imageUriString);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(AddProduct.this, "Failed to add item", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadCategories() {
        ArrayList<String> categories = DB.getAllCategories();
        categories.remove("All Categories");
        adapter.clear();
        adapter.addAll(categories);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            insertImgProd.setImageURI(selectedImageUri);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
}
