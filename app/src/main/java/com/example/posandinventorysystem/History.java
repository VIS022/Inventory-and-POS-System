package com.example.posandinventorysystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Map;

public class History extends AppCompatActivity {

    private ListView historyListView;
    private ArrayAdapter<String> historyAdapter;
    private ArrayList<String> purchaseDates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historyListView = findViewById(R.id.historyListView);
        purchaseDates = new ArrayList<>();

        // Retrieve purchase history data from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("PurchaseHistory", Context.MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String date = entry.getKey();
            purchaseDates.add(date);
        }

        historyAdapter = new ArrayAdapter<>(this, R.layout.custom_history_layout, R.id.historyText, purchaseDates);
        historyListView.setAdapter(historyAdapter);

        historyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedDate = purchaseDates.get(position);
                Intent intent = new Intent(History.this, Receipt.class);
                intent.putExtra("selectedDate", selectedDate);
                startActivity(intent);
            }
        });
    }
}
