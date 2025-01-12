// ProductListAdapter.java
package com.example.posandinventorysystem;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ProductListAdapter extends ArrayAdapter<String> {

    private Context context;
    private int layoutResourceId;
    private boolean isCart;
    public ArrayList<String> originalProductData;
    public ArrayList<String> originalImageUris;
    public ArrayList<String> productData;
    public ArrayList<String> imageUris;

    public ProductListAdapter(Context context, int layoutResourceId, ArrayList<String> productData, ArrayList<String> imageUris, boolean isCart) {
        super(context, layoutResourceId, productData);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.originalProductData = new ArrayList<>(productData);
        this.originalImageUris = new ArrayList<>(imageUris);
        this.productData = productData;
        this.imageUris = imageUris;
        this.isCart = isCart;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(layoutResourceId, parent, false);

        ImageView posImage = view.findViewById(R.id.posImage);

        // Load the image using Glide
        String imageUri = imageUris.get(position);
        Glide.with(getContext())
                .load(imageUri)
                .placeholder(R.drawable.image_default) // Optional placeholder image
                .into(posImage);

        // Declare variables for views based on the context (Cart or POS)
        TextView productNameTextView;
        TextView priceTextView;
        ImageView addSignImageView = null;
        EditText quantityEditText = null;

        if (isCart) {
            // In your ProductListAdapter's getView method for cart_product_item.xml
            productNameTextView = view.findViewById(R.id.cartProductNameTextView);
            priceTextView = view.findViewById(R.id.cartPriceTextView);
            quantityEditText = view.findViewById(R.id.editTextCartQuantity);
        } else {
            // In your ProductListAdapter's getView method for custom_product_item.xml
            productNameTextView = view.findViewById(R.id.posProductNameTextView);
            priceTextView = view.findViewById(R.id.posPriceTextView);
            addSignImageView = view.findViewById(R.id.addSign);
        }

        // Extract product name and price from the ArrayList
        // Inside the getView method of the ProductListAdapter class
        String[] productInfo = productData.get(position).split("-");
        String productName = productInfo[0].trim();
        String priceString = productInfo[1].trim().replaceAll("[^0-9.]", ""); // Remove non-numeric characters except decimal point
        double price = Double.parseDouble(priceString);

        // Set product name and price to TextViews
        productNameTextView.setText(productName);
        priceTextView.setText("Price: ₱" + String.format("%.2f", price));


        // Handle long press to delete item from the cart
        if (isCart) {
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // Show a confirmation dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Confirm Deletion");
                    builder.setMessage("Are you sure you want to remove this item from the cart?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Remove the item from the cart
                            productData.remove(position);
                            imageUris.remove(position);
                            Cart.quantities.remove(position);
                            notifyDataSetChanged();
                            updateTotalPrice(); // Update total price after item deletion
                            Toast.makeText(context, "Item deleted", Toast.LENGTH_SHORT).show();

                            // Remove the quantity from SharedPreferences
                            SharedPreferences sharedPreferences = context.getSharedPreferences("CartPreferences", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.remove(productName);
                            editor.apply();
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Dismiss the dialog
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                    return true;
                }
            });
        }

        // Handle click on the "addSign" ImageView to add product to cart
        // Handle click on the "addSign" ImageView to add product to cart
        if (!isCart && addSignImageView != null) {
            addSignImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Add the product to the cart only if clicked in the POS activity
                    addToCart(productName, price, imageUri);
                    // Show toast message for successful addition
                    Toast.makeText(context, "Added to Cart Successfully", Toast.LENGTH_SHORT).show();
                }
            });
        }


        if (quantityEditText != null) {
            quantityEditText.setText(String.valueOf(Cart.quantities.get(position)));

            final int currentPosition = position;
            quantityEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                // Inside the afterTextChanged method of the TextWatcher
                @Override
                public void afterTextChanged(Editable s) {
                    String quantityString = s.toString();
                    int quantity = 1; // Default quantity
                    try {
                        quantity = Integer.parseInt(quantityString);
                    } catch (NumberFormatException e) {
                        // Handle invalid quantity format gracefully
                    }
                    Cart.quantities.set(currentPosition, quantity); // Update quantity in the list
                    updateTotalPrice();

                    // Save the updated quantities in SharedPreferences
                    SharedPreferences sharedPreferences = context.getSharedPreferences("CartPreferences", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    for (int i = 0; i < productData.size(); i++) {
                        String productName = productData.get(i).split("-")[0].trim();
                        int productQuantity = Cart.quantities.get(i);
                        editor.putInt(productName, productQuantity);
                    }
                    editor.apply();
                }
            });

            Button increaseBtn = view.findViewById(R.id.increaseNumber);
            Button decreaseBtn = view.findViewById(R.id.decreaseNumber);

            EditText finalQuantityEditText1 = quantityEditText;
            increaseBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int currentQuantity = Cart.quantities.get(currentPosition);
                    Cart.quantities.set(currentPosition, currentQuantity + 1); // Update quantity in the list
                    finalQuantityEditText1.setText(String.valueOf(Cart.quantities.get(currentPosition)));
                    updateTotalPrice();

                    // Save the updated quantities in SharedPreferences
                    SharedPreferences sharedPreferences = context.getSharedPreferences("CartPreferences", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    String productName = productData.get(currentPosition).split("-")[0].trim();
                    editor.putInt(productName, Cart.quantities.get(currentPosition));
                    editor.apply();
                }
            });

            EditText finalQuantityEditText = quantityEditText;
            decreaseBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int currentQuantity = Cart.quantities.get(currentPosition);
                    if (currentQuantity > 1) {
                        Cart.quantities.set(currentPosition, currentQuantity - 1); // Update quantity in the list
                        finalQuantityEditText.setText(String.valueOf(Cart.quantities.get(currentPosition)));
                        updateTotalPrice();

                        // Save the updated quantities in SharedPreferences
                        SharedPreferences sharedPreferences = context.getSharedPreferences("CartPreferences", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        String productName = productData.get(currentPosition).split("-")[0].trim();
                        editor.putInt(productName, Cart.quantities.get(currentPosition));
                        editor.apply();
                    }
                }
            });
        }

        return view;
    }

    // Method to handle addition of product to cart
    // Method to handle addition of product to cart
    private void addToCart(String productName, double price, String imageUri) {
        // Check if the product already exists in the cart
        int existingIndex = -1;
        for (int i = 0; i < Cart.productInfoList.size(); i++) {
            String[] productInfo = Cart.productInfoList.get(i).split("-");
            if (productInfo[0].trim().equals(productName)) {
                existingIndex = i;
                break;
            }
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences("CartPreferences", Context.MODE_PRIVATE);
        int currentQuantity = sharedPreferences.getInt(productName, 0);

        if (existingIndex != -1) {
            // If the product already exists, increment the quantity by 1
            Cart.quantities.set(existingIndex, currentQuantity + 1);
        } else {
            // If the product doesn't exist, add it to the cart with a default quantity of 1
            Cart.productInfoList.add(productName + " - ₱" + String.format("%.2f", price) + " - " + imageUri);
            Cart.quantities.add(currentQuantity + 1);
        }

        // Save the updated quantity in SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(productName, currentQuantity + 1);
        editor.apply();
    }


    // Method to update adapter's dataset with filtered data
    public void updateData(ArrayList<String> newProductData, ArrayList<String> newImageUris) {
        productData.clear();
        productData.addAll(newProductData);
        imageUris.clear();
        imageUris.addAll(newImageUris);
        originalProductData.clear();
        originalProductData.addAll(newProductData);
        originalImageUris.clear();
        originalImageUris.addAll(newImageUris);
        notifyDataSetChanged();
    }

    // Method to calculate and update total price
    public void updateTotalPrice() {
        if (isCart) {
            double totalPrice = ((Cart) context).calculateTotalPrice(); // Calculate total price of items in the cart
            TextView totalPriceTextView = ((Cart) context).findViewById(R.id.txtTotCart);
            totalPriceTextView.setText("₱" + totalPrice); // Set total price to txtTotCart TextView
            Log.d("TotalPrice", "Total Price: ₱" + totalPrice);
        }
    }


    private int calculateTotalPrice() {
        int totalPrice = 0;
        for (int i = 0; i < productData.size(); i++) {
            String product = productData.get(i);
            String[] productInfo = product.split("-");
            if (productInfo.length >= 2) {
                String priceString = productInfo[1].trim().replaceAll("[^0-9]", "");
                int price = Integer.parseInt(priceString);
                int quantity = Cart.quantities.get(i);
                totalPrice += price * quantity;
            }
        }
        return totalPrice;
    }
}
