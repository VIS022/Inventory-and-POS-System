package com.example.posandinventorysystem;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class CustomAdapter extends CursorAdapter {

    private TextView txtTotalEarnings;
    private TextView txtItemsPurchased;

    public CustomAdapter(Context context, Cursor c, TextView txtTotalEarnings, TextView txtItemsPurchased) {
        super(context, c, 0);
        this.txtTotalEarnings = txtTotalEarnings;
        this.txtItemsPurchased = txtItemsPurchased;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_layout, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Initialize TextViews
        TextView itemName = view.findViewById(R.id.itemName);
        TextView itemCategory = view.findViewById(R.id.itemCategory);
        TextView itemPrice = view.findViewById(R.id.itemPrice);
        TextView itemQuantity = view.findViewById(R.id.itemQuantity);
        TextView itemBarcode = view.findViewById(R.id.itemBarcode);
        TextView itemCapital = view.findViewById(R.id.itemCapital);
        TextView itemPurchasedTextView = view.findViewById(R.id.itemPurchased);
        TextView totalEarningTextView = view.findViewById(R.id.totalEarning);

        ImageView productImage = view.findViewById(R.id.productImage);

        // Extract data from the cursor
        String imageUriString = cursor.getString(cursor.getColumnIndex("prodImageUri"));
        String name = cursor.getString(cursor.getColumnIndex("prodName"));
        String category = cursor.getString(cursor.getColumnIndex("prodCate"));
        double price = cursor.getDouble(cursor.getColumnIndex("prodPrice"));
        int quantity = cursor.getInt(cursor.getColumnIndex("prodQuantity"));
        String barcode = cursor.getString(cursor.getColumnIndex("prodBarcode"));
        double capital = cursor.getDouble(cursor.getColumnIndex("prodCapital"));

        // Retrieve the itemPurchased value from the cursor
        int itemPurchased = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_PROD_PURCHASED));
        itemPurchasedTextView.setText(String.valueOf(itemPurchased));

        // Retrieve the totalEarning value from the cursor
        double totalEarning = cursor.getDouble(cursor.getColumnIndex("earnings"));
        totalEarningTextView.setText(String.format("%.2f", totalEarning));

        // Load the image using Glide
        Uri imageUri = null;
        if (imageUriString != null && !imageUriString.isEmpty()) {
            imageUri = Uri.parse(imageUriString);
        }

        if (imageUri != null) {
            Glide.with(context)
                    .load(imageUri)
                    .placeholder(R.drawable.image_default)
                    .into(productImage);
        } else {
            productImage.setImageResource(R.drawable.image_default);
        }

        // Update TextViews with data
        itemName.setText(name);
        itemCategory.setText(category);
        itemPrice.setText(String.format("%.2f", price));
        itemQuantity.setText(String.valueOf(quantity));
        itemBarcode.setText(barcode);
        itemCapital.setText(String.format("%.2f", capital));
    }

    @Override
    public void changeCursor(Cursor cursor) {
        super.changeCursor(cursor);
        calculateTotals();
    }

    private void calculateTotals() {
        double totalEarningsFromCursor = 0;
        int totalItemsPurchasedFromCursor = 0;

        Cursor cursor = getCursor();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int itemPurchased = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_PROD_PURCHASED));
                double itemPrice = cursor.getDouble(cursor.getColumnIndex(DBHelper.COLUMN_PROD_PRICE));
                double itemCapital = cursor.getDouble(cursor.getColumnIndex(DBHelper.COLUMN_PROD_CAPITAL));
                totalEarningsFromCursor += (itemPurchased * itemPrice) - (itemPurchased * itemCapital);
                totalItemsPurchasedFromCursor += itemPurchased;
            } while (cursor.moveToNext());
        }

        if (txtTotalEarnings != null) {
            txtTotalEarnings.setText(String.format("%.2f", totalEarningsFromCursor));
        }
        if (txtItemsPurchased != null) {
            txtItemsPurchased.setText(String.valueOf(totalItemsPurchasedFromCursor));
        }
    }
}
