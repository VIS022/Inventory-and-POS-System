// Receipt.java
package com.example.posandinventorysystem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class Receipt extends AppCompatActivity {

    private ListView receiptListView;
    private ReceiptAdapter receiptAdapter;
    private ArrayList<String> paidProducts;
    private ArrayList<String> purchaseDates;
    private ArrayList<Integer> paidAmountList;
    private ArrayList<Double> exchangeList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        receiptListView = findViewById(R.id.receiptListView);
        paidProducts = new ArrayList<>();
        purchaseDates = new ArrayList<>();
        paidAmountList = new ArrayList<>();
        exchangeList = new ArrayList<>();

        // Set up the adapter with custom receipt layout
        receiptAdapter = new ReceiptAdapter(this, R.layout.custom_receipt_layout, paidProducts, purchaseDates);
        receiptListView.setAdapter(receiptAdapter);

        loadReceipts();
        deleteReceiptsFromPreviousMonth();


        // Set up long click listener for the receiptListView
        receiptListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected receipt's purchase date
                String selectedPurchaseDate = purchaseDates.get(position);

                // Create an AlertDialog to confirm receipt removal
                AlertDialog.Builder builder = new AlertDialog.Builder(Receipt.this);
                builder.setTitle("Confirm Deletion");
                builder.setMessage("Are you sure you want to remove this receipt?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Remove the selected receipt from SharedPreferences
                        SharedPreferences sharedPreferences = getSharedPreferences("PurchaseHistory", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.remove(selectedPurchaseDate);
                        editor.remove(selectedPurchaseDate + "_paidAmount");
                        editor.remove(selectedPurchaseDate + "_exchange");
                        editor.apply();

                        // Remove the selected receipt from the lists
                        int index = purchaseDates.indexOf(selectedPurchaseDate);
                        purchaseDates.remove(index);
                        paidProducts.remove(index);
                        paidAmountList.remove(index);
                        exchangeList.remove(index);

                        // Notify the adapter about the data change
                        receiptAdapter.notifyDataSetChanged();

                    }
                });
                builder.setNegativeButton("No", null);
                builder.show();

                return true;
            }
        });
    }

    private void loadReceipts() {
        paidProducts.clear();
        purchaseDates.clear();
        paidAmountList.clear();
        exchangeList.clear();

        // Retrieve all receipts and their corresponding dates from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("PurchaseHistory", Context.MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String receiptId = entry.getKey();
            if (!receiptId.endsWith("_paidAmount") && !receiptId.endsWith("_exchange")) {
                String paidProductsString = entry.getValue().toString();

                // Check if the receipt has been deleted
                if (!sharedPreferences.contains(receiptId)) {
                    Log.d("Receipt", "Skipping deleted receipt: " + receiptId);
                    continue; // Skip the deleted receipt
                }

                // Split the receipt ID to extract the purchase date and time
                String[] receiptIdParts = receiptId.split("_");
                String purchaseDate = receiptId; // Use the receiptId as the purchase date by default

                if (receiptIdParts.length >= 2) {
                    // If the receiptId has the expected format, extract the purchase date and time
                    purchaseDate = receiptIdParts[0] + " " + receiptIdParts[1];
                }

                // Retrieve the paid amount and exchange for the current receipt
                float paidAmount = sharedPreferences.getFloat(receiptId + "_paidAmount", 0.0f);
                float exchange = sharedPreferences.getFloat(receiptId + "_exchange", 0.0f);

                // Split the paid products string into an array
                String[] paidProductsArray = paidProductsString.split(",");

                // Create a StringBuilder to store the combined product information for the current receipt
                StringBuilder combinedProductInfo = new StringBuilder();

                // Calculate the total price for the current receipt
                double totalPrice = 0;
                // Iterate over the paid products for the current receipt
                for (String paidProduct : paidProductsArray) {
                    String[] productDetails = paidProduct.split("\\|");
                    if (productDetails.length >= 3) {
                        String productName = productDetails[0].trim();
                        String price = productDetails[1].trim();
                        int quantity = Integer.parseInt(productDetails[2].trim());

                        // Calculate the total price for the current product
                        String priceValue = price.replaceAll("[^0-9]", "");
                        double priceDouble = Double.parseDouble(priceValue);
                        double productTotalPrice = priceDouble * quantity / 100; // Divide by 100 to convert cents to dollars

                        // Append the product name, price, quantity, and total price to the StringBuilder with spacing
                        combinedProductInfo.append("Product: ").append(productName).append(" - ").append(price).append(" x ").append(quantity).append(" = ₱").append(String.format("%.2f", productTotalPrice)).append("\n\n");

                        // Add the product total price to the total price for the current receipt
                        totalPrice += productTotalPrice;
                    }
                }

                // Append the total price, paid amount, and exchange to the combined product information
                combinedProductInfo.append("Total Price: ₱").append(String.format("%.2f", totalPrice)).append("\n\n");
                combinedProductInfo.append("Paid Amount: ₱").append(String.format("%.2f", paidAmount)).append("\n\n");

                // Create a SpannableString for the exchange text
                SpannableString exchangeSpannable = new SpannableString("Change: ₱" + String.format("%.2f", exchange));
                exchangeSpannable.setSpan(new ForegroundColorSpan(Color.BLUE), 0, exchangeSpannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                combinedProductInfo.append(exchangeSpannable);

                // Add the combined product information and purchase date to the respective lists
                paidProducts.add(combinedProductInfo.toString());
                purchaseDates.add(purchaseDate);
                paidAmountList.add((int) (paidAmount * 100)); // Convert dollars to cents
                exchangeList.add((double) exchange);
            }
        }
    }

    private void deleteReceiptsFromPreviousMonth() {
        SharedPreferences sharedPreferences = getSharedPreferences("PurchaseHistory", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Get the current date
        Calendar currentDate = Calendar.getInstance();

        // Set the date to the 1st day of the current month
        currentDate.set(Calendar.DAY_OF_MONTH, 1);
        currentDate.set(Calendar.HOUR_OF_DAY, 0);
        currentDate.set(Calendar.MINUTE, 0);
        currentDate.set(Calendar.SECOND, 0);
        currentDate.set(Calendar.MILLISECOND, 0);

        // Calculate the timestamp for the 1st day of the current month
        long currentMonthStartTimestamp = currentDate.getTimeInMillis();

        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String receiptId = entry.getKey();
            if (!receiptId.endsWith("_paidAmount") && !receiptId.endsWith("_exchange")) {
                String[] receiptIdParts = receiptId.split("_");
                if (receiptIdParts.length >= 2) {
                    String dateString = receiptIdParts[0];
                    String timeString = receiptIdParts[1];

                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        String timestampString = dateString + " " + timeString;
                        Date date = sdf.parse(timestampString);
                        long timestampMillis = date.getTime();

                        if (timestampMillis < currentMonthStartTimestamp) {
                            // Receipt is from the previous month or earlier, delete it
                            editor.remove(receiptId);
                            editor.remove(receiptId + "_paidAmount");
                            editor.remove(receiptId + "_exchange");
                        }
                    } catch (ParseException e) {
                        // Handle the parsing exception if necessary
                        e.printStackTrace();
                    }
                }
            }
        }

        editor.apply();
    }

}
