package com.example.posandinventorysystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class Earnings extends AppCompatActivity {

    private TextView txtLblEarnings;
    private TextView txtLblCapital;
    private TextView txtLblTotal;
    private DBHelper dbHelper;

    private TextView txtDeleteAfterDate;

    Button btnEarnDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earnings);

        txtLblEarnings = findViewById(R.id.txtLblEarnings);
        txtLblCapital = findViewById(R.id.txtLblCapital);
        txtLblTotal = findViewById(R.id.txtLblTotal);
        btnEarnDate = findViewById(R.id.btnToEarnData);
        txtDeleteAfterDate = findViewById(R.id.txtDeleteAfterDate);

        dbHelper = new DBHelper(this);

        updateEarnings();

        btnEarnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Earnings.this, EarningsHistory.class);
                intent.putExtra("earnings", txtLblEarnings.getText().toString());
                intent.putExtra("capital", txtLblCapital.getText().toString());
                intent.putExtra("total", txtLblTotal.getText().toString());
                startActivity(intent);
            }
        });


    }
    private void updateEarnings() {
        double earnings = 0;
        double capital = 0;

        // Set the deletion date to July 1, 2024
        Calendar deletionDate = Calendar.getInstance();
        deletionDate.set(2024, Calendar.JULY, 1, 0, 0, 0);

        // Calculate the remaining time until deletion
        long currentTimeMillis = System.currentTimeMillis();
        long deletionTimeMillis = deletionDate.getTimeInMillis();
        long remainingTimeMillis = deletionTimeMillis - currentTimeMillis;

        // Convert remaining time to days, hours, minutes, and seconds
        long seconds = remainingTimeMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        // Format the remaining time
        String remainingTime = String.format("%dd %02d:%02d:%02d", days, hours % 24, minutes % 60, seconds % 60);

        // Set the deletion date text
        txtDeleteAfterDate.setText("Delete After: " + remainingTime);

        // Retrieve the inventory data
        Cursor inventoryCursor = dbHelper.getAllProductDetails("", "", "", true, false, 0);
        if (inventoryCursor != null && inventoryCursor.moveToFirst()) {
            do {
                int itemPurchased = inventoryCursor.getInt(inventoryCursor.getColumnIndex(DBHelper.COLUMN_PROD_PURCHASED));
                double itemPrice = inventoryCursor.getDouble(inventoryCursor.getColumnIndex(DBHelper.COLUMN_PROD_PRICE));
                double itemCapital = inventoryCursor.getDouble(inventoryCursor.getColumnIndex(DBHelper.COLUMN_PROD_CAPITAL));

                earnings += itemPurchased * itemPrice;
                capital += itemCapital * itemPurchased;
            } while (inventoryCursor.moveToNext());
            inventoryCursor.close();
        }

        double total = earnings - capital;

        // Reset the earnings data every month
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);
        SharedPreferences prefs = getSharedPreferences("EarningsPrefs", MODE_PRIVATE);
        int lastResetMonth = prefs.getInt("lastResetMonth", -1);
        int lastResetYear = prefs.getInt("lastResetYear", -1);

        if (currentMonth != lastResetMonth || currentYear != lastResetYear) {
            // Insert the earnings data into the EarningsHistory table
            dbHelper.insertEarningsHistory(earnings, capital, total);

            // Reset the earnings, capital, and total values
            earnings = 0;
            capital = 0;
            total = 0;

            // Update the last reset month and year
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("lastResetMonth", currentMonth);
            editor.putInt("lastResetYear", currentYear);
            editor.apply();
        }

        txtLblEarnings.setText(String.format("%.2f", earnings));
        txtLblCapital.setText(String.format("%.2f", capital));
        txtLblTotal.setText(String.format("%.2f", total));
    }

}
