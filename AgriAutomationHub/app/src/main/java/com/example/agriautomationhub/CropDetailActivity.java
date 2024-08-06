package com.example.agriautomationhub;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textview.MaterialTextView;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

public class CropDetailActivity extends AppCompatActivity {

    TextView cropNameText, cropPlantSelectionText, cropPlantingText,
            cropMonitoringText, cropSiteSelectionText, cropFieldPreparationText, cropWeedingText,
            cropIrrigationText, cropFertilizationOrganicText, cropFertilizationChemicalText,
            cropPreventiveMeasureText, cropPlantProtectionChemicalText, cropHarvestingText,
            cropPostHarvestText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_detail);

        cropNameText = findViewById(R.id.cropNameText);
        cropPlantSelectionText = findViewById(R.id.cropPlantSelectionText);
        cropPlantingText = findViewById(R.id.cropPlantingText);
        cropMonitoringText = findViewById(R.id.cropMonitoringText);
        cropSiteSelectionText = findViewById(R.id.cropSiteSelectionText);
        cropFieldPreparationText = findViewById(R.id.cropFieldPreparationText);
        cropWeedingText = findViewById(R.id.cropWeedingText);
        cropIrrigationText = findViewById(R.id.cropIrrigationText);
        cropFertilizationOrganicText = findViewById(R.id.cropFertilizationOrganicText);
        cropFertilizationChemicalText = findViewById(R.id.cropFertilizationChemicalText);
        cropPreventiveMeasureText = findViewById(R.id.cropPreventiveMeasureText);
        cropPlantProtectionChemicalText = findViewById(R.id.cropPlantProtectionChemicalText);
        cropHarvestingText = findViewById(R.id.cropHarvestingText);
        cropPostHarvestText = findViewById(R.id.cropPostHarvestText);

        Intent intent = getIntent();
        String cropName = intent.getStringExtra("cropName");

        if (cropName != null) {
            cropNameText.setText(cropName);
            loadCropDetails(cropName);
        }
    }

    private void loadCropDetails(String cropName) {
        try {
            InputStream inputStream = getAssets().open("crop_details.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();

            String json = new String(buffer, "UTF-8");
            JSONObject jsonObject = new JSONObject(json);

            JSONObject cropObject = jsonObject.getJSONObject(cropName);

            cropPlantSelectionText.setText(cropObject.optString("Plant Selection", "N/A"));
            cropPlantingText.setText(cropObject.optString("Planting", "N/A"));
            cropMonitoringText.setText(cropObject.optString("Monitoring", "N/A"));
            cropSiteSelectionText.setText(cropObject.optString("Site Selection", "N/A"));
            cropFieldPreparationText.setText(cropObject.optString("Field Preparation", "N/A"));
            cropWeedingText.setText(cropObject.optString("Weeding", "N/A"));
            cropIrrigationText.setText(cropObject.optString("Irrigation", "N/A"));
            cropFertilizationOrganicText.setText(cropObject.optString("Fertilization Organic", "N/A"));
            cropFertilizationChemicalText.setText(cropObject.optString("Fertilization Chemical", "N/A"));
            cropPreventiveMeasureText.setText(cropObject.optString("Preventive Measure", "N/A"));
            cropPlantProtectionChemicalText.setText(cropObject.optString("Plant Protection Chemical", "N/A"));
            cropHarvestingText.setText(cropObject.optString("Harvesting", "N/A"));
            cropPostHarvestText.setText(cropObject.optString("Post-Harvest", "N/A"));

        } catch (IOException | JSONException e) {
            Log.e("CropDetailActivity", "Error reading JSON file", e);
        }
    }
}
