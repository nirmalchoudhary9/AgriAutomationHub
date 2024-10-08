package com.example.agriautomationhub;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class CropDetailActivity extends AppCompatActivity {

    private static final String TAG = "CropDetailActivity";
    TextView cropNameText;
    ExpandableListView expandableListView;
    CropDetailAdapter listAdapter;
    List<String> listDataHeader;
    HashMap<String, List<Object>> listDataChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_detail);

        cropNameText = findViewById(R.id.cropNameText);
        expandableListView = findViewById(R.id.expandableListView);

        AtomicReference<Intent> intent = new AtomicReference<>(getIntent());
        String cropName = intent.get().getStringExtra("cropName");

        if (cropName != null) {
            Log.d(TAG, "Received crop name: " + cropName);
            // Convert to title case (capitalize first letter of each word)
            String titleCaseCropName = capitalizeFirstLetter(cropName);
            cropNameText.setText(titleCaseCropName);
            loadCropDetails(cropName);
        } else {
            Log.e(TAG, "No crop name received in intent");
        }

        ImageView back = findViewById(R.id.back_btn_crop_detail);
        back.setOnClickListener(v -> {
            intent.set(new Intent(getApplicationContext(), CropRecommenderActivity.class));
            startActivity(intent.get());
            finish();
        });

    }
    // Method to capitalize the first letter of each word
    private String capitalizeFirstLetter(String input) {
        StringBuilder result = new StringBuilder(input.length());
        boolean capitalizeNext = true;
        for (char c : input.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                c = Character.toTitleCase(c);
                capitalizeNext = false;
            }
            result.append(c);
        }
        return result.toString();
    }

    private void loadCropDetails(String cropName) {
        try {
            InputStream inputStream = getAssets().open("crop_details.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            int bytesRead = inputStream.read(buffer);
            inputStream.close();

            if (bytesRead != size) {
                throw new IOException("Could not read the entire file");
            }

            String json = new String(buffer, "UTF-8");
            JSONObject jsonObject = new JSONObject(json);

            if (jsonObject.has(cropName)) {
                JSONObject cropObject = jsonObject.getJSONObject(cropName);
                Log.d(TAG, "Crop details found for: " + cropName);

                listDataHeader = new ArrayList<>();
                listDataChild = new HashMap<>();

                populateListData(cropObject, "Plant Selection");
                populateListData(cropObject, "Planting");
                populateListData(cropObject, "Monitoring");
                populateListData(cropObject, "Site Selection");
                populateListData(cropObject, "Field Preparation");
                populateListData(cropObject, "Weeding");
                populateListData(cropObject, "Irrigation");
                populateListData(cropObject, "Fertilization Organic");
                populateListData(cropObject, "Fertilization Chemical");
                populateListData(cropObject, "Preventive Measure");
                populateListData(cropObject, "Plant Protection Chemical");
                populateListData(cropObject, "Harvesting");
                populateListData(cropObject, "Post-Harvest");

                listAdapter = new CropDetailAdapter(this, listDataHeader, listDataChild);
                expandableListView.setAdapter(listAdapter);
            } else {
                Log.e(TAG, "Crop name not found in JSON: " + cropName);
            }

        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error reading JSON file", e);
        }
    }

    private void populateListData(JSONObject cropObject, String key) throws JSONException {
        JSONObject nestedObject = cropObject.optJSONObject(key);
        if (nestedObject == null) {
            listDataHeader.add(key);
            List<Object> childList = new ArrayList<>();
            childList.add("N/A");
            listDataChild.put(key, childList);
            return;
        }

        listDataHeader.add(key);
        List<Object> childList = new ArrayList<>();
        Iterator<String> keys = nestedObject.keys();
        while (keys.hasNext()) {
            String nestedKey = keys.next();
            Object value = nestedObject.get(nestedKey);
            if (value instanceof JSONObject) {
                // Add each nested JSONObject entry as a separate item
                String nestedJsonString = parseNestedJSONObjectToString(nestedKey, (JSONObject) value);
                childList.add(nestedJsonString);
            } else if (value instanceof String) {
                childList.add(nestedKey + ": " + value);
            } else if (value instanceof org.json.JSONArray) {
                List<String> arrayValues = new ArrayList<>();
                org.json.JSONArray jsonArray = (org.json.JSONArray) value;
                for (int i = 0; i < jsonArray.length(); i++) {
                    arrayValues.add(jsonArray.getString(i));
                }
                childList.add(nestedKey + ":\n" + String.join("\n", arrayValues));
            }
        }
        listDataChild.put(key, childList);
    }

    private String parseNestedJSONObjectToString(String key, JSONObject jsonObject) throws JSONException {
        StringBuilder sb = new StringBuilder();
        sb.append(key).append(":\n");
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String nestedKey = keys.next();
            Object value = jsonObject.get(nestedKey);
            if (value instanceof JSONObject) {
                // Recursively handle nested JSONObjects
                sb.append(parseNestedJSONObjectToString(nestedKey, (JSONObject) value));
            } else if (value instanceof String) {
                sb.append(nestedKey).append(": ").append(value).append("\n");
            } else if (value instanceof org.json.JSONArray) {
                List<String> arrayValues = new ArrayList<>();
                org.json.JSONArray jsonArray = (org.json.JSONArray) value;
                for (int i = 0; i < jsonArray.length(); i++) {
                    arrayValues.add(jsonArray.getString(i));
                }
                sb.append(nestedKey).append(":\n").append(String.join("\n", arrayValues)).append("\n");
            }
        }
        return sb.toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            return logoutUser();
        }
        if (id == R.id.action_settings) {
            return settings();
        }
        if (id == R.id.action_help) {
            Intent intent = new Intent(getApplicationContext(), HelpActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean logoutUser() {
        FirebaseAuth.getInstance().signOut();
        // Redirect to login screen or any other desired activity
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
        return true;
    }

    private boolean settings() {
        Intent intent = new Intent(getApplicationContext(), SettingsPage.class);
        startActivity(intent);
        return true;
    }
}
