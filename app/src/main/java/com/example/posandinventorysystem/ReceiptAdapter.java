// ReceiptAdapter.java
package com.example.posandinventorysystem;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ReceiptAdapter extends ArrayAdapter<String> {
    private Context context;
    private int layoutResourceId;
    private ArrayList<String> paidProducts;
    private ArrayList<String> purchaseDates;

    public ReceiptAdapter(Context context, int layoutResourceId, ArrayList<String> paidProducts, ArrayList<String> purchaseDates) {
        super(context, layoutResourceId, paidProducts);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.paidProducts = paidProducts;
        this.purchaseDates = purchaseDates;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(layoutResourceId, parent, false);

        TextView dateTextView = view.findViewById(R.id.dateTextView);
        TextView productInfoTextView = view.findViewById(R.id.productInfoTextView);
        TextView totalPriceTextView = view.findViewById(R.id.totalPriceTextView);
        TextView paidAmountTextView = view.findViewById(R.id.paidAmountTextView);
        TextView exchangeTextView = view.findViewById(R.id.exchangeTextView);

        String paidProduct = paidProducts.get(position);
        String purchaseDate = purchaseDates.get(position);

        // Split the paid product string into lines
        String[] lines = paidProduct.split("\n\n");

        // Create a SpannableStringBuilder to store the product information with colored product names
        SpannableStringBuilder productInfoBuilder = new SpannableStringBuilder();

        // Iterate over the lines and append the product information to the SpannableStringBuilder
        for (String line : lines) {
            if (line.startsWith("Product: ")) {
                SpannableString productSpannable = new SpannableString(line + "\n\n");
                int startIndex = line.indexOf(":") + 1;
                int endIndex = line.indexOf("-", startIndex);
                if (endIndex == -1) {
                    endIndex = line.length();
                }
                productSpannable.setSpan(new ForegroundColorSpan(Color.RED), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                productInfoBuilder.append(productSpannable);
            } else if (line.startsWith("Total Price: ")) {
                totalPriceTextView.setText(line);
            } else if (line.startsWith("Paid Amount: ")) {
                paidAmountTextView.setText(line);
            } else if (line.startsWith("Change: ")) {
                exchangeTextView.setText(line);
            }
        }

        dateTextView.setText(purchaseDate);
        productInfoTextView.setText(productInfoBuilder);

        return view;
    }



}

