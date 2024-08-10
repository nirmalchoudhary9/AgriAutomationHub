package com.example.agriautomationhub;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.agriautomationhub.R;

public class SellingPriceCalculatorActivity extends AppCompatActivity {


    private EditText editTextSeedCost, editTextFertilizerCost, editTextLaborCost, editTextOtherCosts;
    private EditText editTextYield;
    private TextView textViewPriceResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selling_price_calculator);

        // Initialize the UI elements
        editTextSeedCost = findViewById(R.id.editTextSeedCost);
        editTextFertilizerCost = findViewById(R.id.editTextFertilizerCost);
        editTextLaborCost = findViewById(R.id.editTextLaborCost);
        editTextOtherCosts = findViewById(R.id.editTextOtherCosts);
        editTextYield = findViewById(R.id.editTextYield);
        textViewPriceResult = findViewById(R.id.textViewPriceResult);
        Button buttonCalculatePrice = findViewById(R.id.buttonCalculatePrice);

        // Set the button click listener
        buttonCalculatePrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateMinimumSellingPrice();
            }
        });
    }

    private void calculateMinimumSellingPrice() {
        // Get values from the input fields
        double seedCost = parseDouble(editTextSeedCost.getText().toString());
        double fertilizerCost = parseDouble(editTextFertilizerCost.getText().toString());
        double laborCost = parseDouble(editTextLaborCost.getText().toString());
        double otherCosts = parseDouble(editTextOtherCosts.getText().toString());
        double yield = parseDouble(editTextYield.getText().toString());

        // Calculate total costs
        double totalCosts = seedCost + fertilizerCost + laborCost + otherCosts;

        // Calculate minimum selling price per kg to avoid loss
        double minimumSellingPrice = (totalCosts / yield) * 100;

        // Display the result
        textViewPriceResult.setText("Minimum Selling Price: ₹" + String.format("%.2f", minimumSellingPrice) + " per 100kg");
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}