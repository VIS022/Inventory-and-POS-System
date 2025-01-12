package com.example.posandinventorysystem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import android.Manifest;

import com.bumptech.glide.Glide;


public class EditProduct extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    DBHelper DB;
    EditText prodName, prodPrice, prodQuantity, prodCapital, prodBarcode;
    Spinner prodCate;
    Button btnSavePro, btnDeleteProd;
    private Uri selectedImageUri;
    ImageView imgEditProd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        // Initialize DBHelper
        DB = new DBHelper(this);

        // Retrieve the product details and categories from the Intent
        String productName = getIntent().getStringExtra("productName");
        String productCategory = getIntent().getStringExtra("productCategory");
        String productBarcode = getIntent().getStringExtra("productBarcode");
        ArrayList<String> categories = getIntent().getStringArrayListExtra("categories");

        // Initialize UI elements
        imgEditProd = findViewById(R.id.imgEditProd);
        prodName = findViewById(R.id.EditTextName);
        prodPrice = findViewById(R.id.EditTextPrice);
        prodQuantity = findViewById(R.id.EditTextQuantity);
        prodCapital = findViewById(R.id.EditTextCapital);
        prodCate = findViewById(R.id.addtxtEditSpinner);
        btnSavePro = findViewById(R.id.btnSavePro);
        btnDeleteProd = findViewById(R.id.btnDeleteProd);
        prodBarcode = findViewById(R.id.EditTextBar);

        // Retrieve the image URI from the intent extras
        String imageUriString = getIntent().getStringExtra("imageUriString");
        Log.d("EditProduct", "Received product category: " + productCategory);
        if (imageUriString != null) {
            selectedImageUri = Uri.parse(imageUriString);
            // Load the image using Glide
            Glide.with(this)
                    .load(selectedImageUri)
                    .placeholder(R.drawable.image_default)
                    .into(imgEditProd);
        }

        // Set an OnClickListener to handle image selection
        imgEditProd.setOnClickListener(v -> {
            openImagePicker();
        });

        // Populate the UI elements with the retrieved product details
        ProductDetails productDetails = DB.getProductDetails(productName);
        if (productDetails != null) {
            prodName.setText(productDetails.getProdName());
            prodPrice.setText(String.valueOf(productDetails.getProdPrice()));
            prodQuantity.setText(String.valueOf(productDetails.getProdQuantity()));
            prodCapital.setText(String.valueOf(productDetails.getProdCapital()));
        }

        // Populate the Spinner with categories and set the selected category
        if (categories != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            prodCate.setAdapter(adapter);
            if (productCategory != null) {
                int position = adapter.getPosition(productCategory);
                Log.d("EditProduct", "Category position: " + position);

                if (position != -1) {
                    prodCate.setSelection(position);
                    Log.d("EditProduct", "Category selection set");
                }  else {
                    Log.d("EditProduct", "Category not found in adapter");
            }
        }
        }

        // Set the product barcode if available
        if (productBarcode != null) {
            prodBarcode.setText(productBarcode);
        }

        // Handle Save button click
        btnSavePro.setOnClickListener(v -> {
            // Get updated product details
            String updatedName = prodName.getText().toString();
            String updatedQuantityText = prodQuantity.getText().toString();
            String updatedCapitalText = prodCapital.getText().toString();
            String updatedCategory = prodCate.getSelectedItem().toString();
            String updatedBarcode = prodBarcode.getText().toString();
            String updatedImageUri = selectedImageUri != null ? selectedImageUri.toString() : null;

            double updatedPrice;
            try {
                updatedPrice = Double.parseDouble(prodPrice.getText().toString());
            } catch (NumberFormatException e) {
                Toast.makeText(EditProduct.this, "Invalid price format\nPlease put numbers.", Toast.LENGTH_SHORT).show();
                return;
            }

            int updatedQuantity;
            try {
                updatedQuantity = Integer.parseInt(updatedQuantityText);
            } catch (NumberFormatException e) {
                Toast.makeText(EditProduct.this, "Invalid quantity format\nPlease put numbers.", Toast.LENGTH_SHORT).show();
                return;
            }

            double updatedCapital;
            try {
                updatedCapital = Double.parseDouble(prodCapital.getText().toString());
            } catch (NumberFormatException e) {
                Toast.makeText(EditProduct.this, "Invalid capital format\nPlease put numbers.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update the product in the database
            boolean success = DB.updateProduct(productName, updatedName, updatedCategory, updatedPrice, updatedQuantity, updatedCapital, updatedBarcode, updatedImageUri);
            if (success) {
                Toast.makeText(EditProduct.this, "Product Updated Successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.putExtra("position", getIntent().getIntExtra("position", -1));
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Toast.makeText(EditProduct.this, "Product Update Failed", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle Delete button click
        btnDeleteProd.setOnClickListener(v -> {
            String productNameToDelete = prodName.getText().toString();
            AlertDialog.Builder builder = new AlertDialog.Builder(EditProduct.this);
            builder.setTitle("Delete Product");
            builder.setMessage("Are you sure you want to delete this product?");

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    boolean success = DB.deleteProduct(productNameToDelete);
                    if (success) {
                        Toast.makeText(EditProduct.this, "Product Deleted Successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        intent.putExtra("position", getIntent().getIntExtra("position", -1));
                        setResult(RESULT_OK, intent);
                        finish();
                    } else {
                        Toast.makeText(EditProduct.this, "Failed to Delete Product", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ArrayList<String> categories = DB.getAllCategories();
        categories.remove("All Categories");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prodCate.setAdapter(adapter);

        // Preserve the selected category
        String selectedCategory = prodCate.getSelectedItem().toString();
        int position = adapter.getPosition(selectedCategory);
        if (position != -1) {
            prodCate.setSelection(position);
        }
    }


    private void openImagePicker() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_IMAGE_REQUEST);
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            // Load the selected image using Glide
            Glide.with(this)
                    .load(selectedImageUri)
                    .placeholder(R.drawable.image_default)
                    .into(imgEditProd);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PICK_IMAGE_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Permission denied. Unable to open image picker.", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
