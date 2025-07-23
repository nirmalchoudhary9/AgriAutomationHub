package com.example.agriautomationhub;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SchemesActivity extends AppCompatActivity {

    private RecyclerView schemesRecyclerView;
    private SchemeAdapter schemeAdapter;
    private List<Scheme> schemeList;
    private EditText searchBar;
    private Spinner filterSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schemes);

        // Initialize views
        schemesRecyclerView = findViewById(R.id.schemesRecyclerView);
        searchBar = findViewById(R.id.searchBar);
        filterSpinner = findViewById(R.id.filterSpinner);

        // Initialize scheme list
        schemeList = new ArrayList<>();
        populateSchemeList();

        // Set up RecyclerView
        schemesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        schemeAdapter = new SchemeAdapter(schemeList);
        schemesRecyclerView.setAdapter(schemeAdapter);

        // Search functionality
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                schemeAdapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Filter functionality can be implemented similarly (optional)
    }

    private void populateSchemeList() {
        schemeList.add(new Scheme("PM-Kisan Yojana", "Financial assistance of ₹6000 per year"));
        schemeList.add(new Scheme("Soil Health Card Scheme", "Analysis and recommendations for better yields"));
        schemeList.add(new Scheme("KCC Scheme", "Low-interest loans for farmers"));
        schemeList.add(new Scheme("Fasal Bima Yojana", "Crop insurance against natural disasters"));
        schemeList.add(new Scheme("National Horticulture Mission", "Boosting horticulture production"));
    }
}
