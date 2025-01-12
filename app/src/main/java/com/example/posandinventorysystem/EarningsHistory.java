package com.example.posandinventorysystem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EarningsHistory extends AppCompatActivity {

    private DBHelper dbHelper;
    private ListView earningHistoryListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earnings_history);

        dbHelper = new DBHelper(this);
        earningHistoryListView = findViewById(R.id.earningHistoryListView);

        Intent intent = getIntent();
        String currentEarnings = intent.getStringExtra("earnings");
        String currentCapital = intent.getStringExtra("capital");
        String currentTotal = intent.getStringExtra("total");

        populateEarningsHistory(currentEarnings, currentCapital, currentTotal);
    }

    private void populateEarningsHistory(String currentEarnings, String currentCapital, String currentTotal) {
        Cursor cursor = dbHelper.getEarningsHistory();
        if (cursor != null && cursor.moveToFirst()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
            List<EarningsHistoryItem> earningsHistoryList = new ArrayList<>();

            do {
                int id = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ID));
                int month = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_MONTH));
                int year = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_YEAR));

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.YEAR, year);
                String monthYear = dateFormat.format(calendar.getTime());

                EarningsHistoryItem item = new EarningsHistoryItem(id, monthYear, currentEarnings, currentCapital, currentTotal);
                earningsHistoryList.add(item);
            } while (cursor.moveToNext());

            cursor.close();

            EarningsHistoryAdapter adapter = new EarningsHistoryAdapter(this, earningsHistoryList);
            earningHistoryListView.setAdapter(adapter);

            // Set long click listener on the ListView
            earningHistoryListView.setOnItemLongClickListener((parent, view, position, id) -> {
                EarningsHistoryItem item = earningsHistoryList.get(position);
                showDeleteConfirmationDialog(item);
                return true;
            });
        }
    }
    private void showDeleteConfirmationDialog(EarningsHistoryItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete History");
        builder.setMessage("Do you want to delete this history?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            deleteHistoryItem(item);
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }

    private void deleteHistoryItem(EarningsHistoryItem item) {
        int deletedRows = dbHelper.deleteEarningsHistory(item.getId());
        if (deletedRows > 0) {
            Toast.makeText(this, "History deleted successfully", Toast.LENGTH_SHORT).show();
            populateEarningsHistory("", "", ""); // Refresh the list after deletion
        } else {
            Toast.makeText(this, "Failed to delete history", Toast.LENGTH_SHORT).show();
        }
    }


}
