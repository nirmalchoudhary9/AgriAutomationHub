package com.example.agriautomationhub;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class FertilizerCalculatorActivity extends AppCompatActivity {

    EditText soilPhInput, landAreaInput;
    Spinner cropTypeSpinner;
    Button calculateBtn;
    TextView outputText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fertilizer_calculator);

        soilPhInput = findViewById(R.id.soil_ph_input);
        landAreaInput = findViewById(R.id.land_area_input);
        cropTypeSpinner = findViewById(R.id.crop_type_spinner);
        calculateBtn = findViewById(R.id.calculate_btn);
        outputText = findViewById(R.id.output_text);

        // Initialize the Spinner with crop types
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.crop_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cropTypeSpinner.setAdapter(adapter);

        calculateBtn.setOnClickListener(v -> {
            try {
                // Get input values
                float soilPh = Float.parseFloat(soilPhInput.getText().toString());
                String cropType = cropTypeSpinner.getSelectedItem().toString();
                float landArea = Float.parseFloat(landAreaInput.getText().toString());

                // Calculate fertilizer requirement
                float fertilizerAmount = calculateFertilizer(soilPh, cropType, landArea);

                // Display result
                outputText.setText(String.format("Fertilizer required: %.2f kg", fertilizerAmount));

            } catch (NumberFormatException e) {
                outputText.setText("Please enter valid numbers.");
            }
        });

        ImageView back = findViewById(R.id.back_btn_fertilizer);
        back.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_fertilizer);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                return true;
            }else if (id == R.id.navigation_news) {
                // Handle News navigation
                startActivity(new Intent(getApplicationContext(), NewsActivity.class));
                return true;
            } else if (id == R.id.navigation_mandi) {
                startActivity(new Intent(FertilizerCalculatorActivity.this, MandiActivity.class));
                return true;
            }
            return false;
        });
    }

    private float calculateFertilizer(float soilPh, String cropType, float landArea) {
        // Example logic for calculation
        float baseFertilizer = 100; // Base amount for calculation
        float phFactor = (soilPh < 6.0) ? 1.2f : (soilPh <= 7.0) ? 1.0f : 0.8f;
        float cropFactor = (cropType.equalsIgnoreCase("Corn")) ? 1.5f : 1.0f;

        return baseFertilizer * phFactor * cropFactor * landArea;
    }
}
