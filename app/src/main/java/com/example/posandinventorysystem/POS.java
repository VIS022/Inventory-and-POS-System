package com.example.posandinventorysystem;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class POS extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pos);

        ListView listViewPOS= findViewById(R.id.secListView);
        String[] names = {
                "Pencil"
        };
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, names
        );
        listViewPOS.setAdapter(arrayAdapter);


    }
}
