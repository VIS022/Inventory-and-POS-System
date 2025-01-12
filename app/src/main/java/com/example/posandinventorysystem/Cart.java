package com.example.posandinventorysystem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import androidx.core.app.NotificationCompat;

public class Cart extends AppCompatActivity {

    static ArrayList<String> productInfoList = new ArrayList<>(); // List to store products in the cart
    ProductListAdapter adapter;
    static ArrayList<Integer> quantities = new ArrayList<>();
    ListView listView;
    TextView txtTotCart; // TextView to display total price
    EditText editTextPayAmount;

    private static final String TAG = "Cart";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Initialize views
        listView = findViewById(R.id.ATCListview);
        txtTotCart = findViewById(R.id.txtTotCart); // Initialize TextView
        editTextPayAmount = findViewById(R.id.editTextPayAmount);

        Button btnCartPay = findViewById(R.id.btnCartPay);

        Button btnDeleteAll = findViewById(R.id.btnDeleteAll);
        btnDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteAllConfirmationDialog();
            }
        });

        btnCartPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Cart.productInfoList.isEmpty()) {
                    Toast.makeText(Cart.this, "Empty Cart. Please add products!", Toast.LENGTH_SHORT).show();
                } else {
                    String payAmountString = editTextPayAmount.getText().toString().trim();
                    if (!payAmountString.isEmpty()) {
                        double payAmount = Double.parseDouble(payAmountString);
                        double totalPrice = calculateTotalPrice();

                        if (payAmount >= totalPrice) {
                            // Validate quantities
                            if (!validateQuantities()) {
                                Toast.makeText(Cart.this, "One or more items have quantities exceeding available stock.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            double exchange = payAmount - totalPrice;
                            editTextPayAmount.setText("");
                            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault());
                            String currentDateTime = sdf.format(new Date());
                            String receiptId = currentDateTime.replace(" ", "_");

                            ArrayList<String> paidProductsWithDetails = new ArrayList<>();
                            for (int i = 0; i < Cart.productInfoList.size(); i++) {
                                String productInfo = Cart.productInfoList.get(i);
                                String[] parts = productInfo.split("-");
                                if (parts.length >= 2) {
                                    String productName = parts[0].trim();
                                    String price = parts[1].trim();
                                    int purchaseQuantity = Cart.quantities.get(i);
                                    String paidProductInfo = productName + "|" + price + "|" + purchaseQuantity;
                                    paidProductsWithDetails.add(paidProductInfo);

                                    // Update Inventory quantity and itemPurchased in the database
                                    DBHelper dbHelper = new DBHelper(Cart.this);
                                    dbHelper.updateProductQuantityAndPurchased(productName, purchaseQuantity);
                                }
                            }

                            SharedPreferences sharedPreferences = getSharedPreferences("PurchaseHistory", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            String paidProductsString = TextUtils.join(",", paidProductsWithDetails);
                            editor.putString(receiptId, paidProductsString);
                            editor.putFloat(receiptId + "_paidAmount", (float) payAmount);
                            editor.putFloat(receiptId + "_exchange", (float) exchange);
                            editor.apply();

                            Toast.makeText(Cart.this, "Payment successful!", Toast.LENGTH_SHORT).show();

                            Cart.productInfoList.clear();
                            Cart.quantities.clear();
                            SharedPreferences cartPreferences = getSharedPreferences("CartPreferences", Context.MODE_PRIVATE);
                            SharedPreferences.Editor cartEditor = cartPreferences.edit();
                            cartEditor.clear();
                            cartEditor.apply();

                            adapter.notifyDataSetChanged();
                            updateTotalPrice();
                        } else {
                            Toast.makeText(Cart.this, "Insufficient payment amount!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(Cart.this, "Please enter the pay amount!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // Initialize quantities list with default values
        for (int i = 0; i < productInfoList.size(); i++) {
            quantities.add(1);
        }

        // Update total price initially
        updateTotalPrice();
    }

    private boolean validateQuantities() {
        DBHelper dbHelper = new DBHelper(this);
        boolean isValid = true;
        for (int i = 0; i < productInfoList.size(); i++) {
            String productName = productInfoList.get(i).split("-")[0].trim();
            int requestedQuantity = quantities.get(i);
            int availableQuantity = dbHelper.getProductQuantity(productName);
            if (requestedQuantity > availableQuantity) {
                isValid = false;
            }

            // Check if quantity is less than 10 and trigger notification
            int newQuantity = availableQuantity - requestedQuantity;
            if (newQuantity < 10) {
                Log.d(TAG, "triggerNotification called for product: " + productName + ", quantity: " + newQuantity);
                triggerNotification(productName, newQuantity, i);
            }
        }
        return isValid;
    }

    private ArrayList<String> getImageUrisFromProductInfoList() {
        ArrayList<String> imageUris = new ArrayList<>();
        for (String productInfo : productInfoList) {
            String[] parts = productInfo.split("-");
            if (parts.length >= 3) {
                String imageUri = parts[2].trim();
                imageUris.add(imageUri);
            } else {
                imageUris.add(""); // Add an empty string if image URI is not available
            }
        }
        return imageUris;
    }

    @Override
    protected void onResume() {
        super.onResume();

        ArrayList<String> imageUriList = getImageUrisFromProductInfoList();

        // Retrieve the quantities from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("CartPreferences", Context.MODE_PRIVATE);
        quantities.clear();
        for (int i = 0; i < productInfoList.size(); i++) {
            String productName = productInfoList.get(i).split("-")[0].trim();
            int productQuantity = sharedPreferences.getInt(productName, 0); // Default to 0 if quantity not found
            quantities.add(productQuantity);
        }

        // Initialize adapter
        if (imageUriList != null) {
            adapter = new ProductListAdapter(this, R.layout.cart_product_item, productInfoList, imageUriList, true);
            listView.setAdapter(adapter);
        }

        adapter.notifyDataSetChanged();
        updateTotalPrice();
    }

    // Method to calculate and update total price
    // Method to calculate and update total price
    void updateTotalPrice() {
        double totalPrice = calculateTotalPrice();
        txtTotCart.setText(String.format(Locale.getDefault(), "₱%.2f", totalPrice));
    }

    double calculateTotalPrice() {
        double totalPrice = 0;
        for (int i = 0; i < productInfoList.size(); i++) {
            String productInfo = productInfoList.get(i);
            String[] parts = productInfo.split("-");
            if (parts.length >= 2) {
                String priceString = parts[1].trim().replaceAll("[^0-9.]", "");
                if (!priceString.isEmpty()) {
                    double price = Double.parseDouble(priceString);
                    int quantity = quantities.get(i);
                    totalPrice += price * quantity;
                }
            }
        }
        return Math.round(totalPrice * 100.0) / 100.0;
    }

    private void triggerNotification(String productName, int quantity, int notificationId) {
        if (quantity > 0) {
            NotificationUtils.createNotificationChannel(this);

            Intent intent = new Intent(this, Inventory.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NotificationUtils.CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification) // Replace with your app's notification icon
                    .setContentTitle("Low Inventory Alert")
                    .setContentText("Product " + productName + " is low on stock. Only " + quantity + " left!")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                Log.d(TAG, "NotificationManager is not null. Sending notification...");
                notificationManager.notify(notificationId, builder.build());
            } else {
                Log.d(TAG, "NotificationManager is null. Cannot send notification.");
            }
        }
    }


    private void showDeleteAllConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation")
                .setMessage("Do you want to delete all products?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteAllProducts();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteAllProducts() {
        if (Cart.productInfoList.isEmpty()) {
            Toast.makeText(Cart.this, "Empty Cart. There are no products to delete.", Toast.LENGTH_SHORT).show();
        } else {
            Cart.productInfoList.clear();
            Cart.quantities.clear();
            SharedPreferences cartPreferences = getSharedPreferences("CartPreferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor cartEditor = cartPreferences.edit();
            cartEditor.clear();
            cartEditor.apply();

            adapter.notifyDataSetChanged();
            updateTotalPrice();
            Toast.makeText(Cart.this, "All products deleted from the cart!", Toast.LENGTH_SHORT).show();
        }
    }

}
