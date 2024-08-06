package com.example.agriautomationhub;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

        Intent intent = getIntent();
        String cropName = intent.getStringExtra("cropName");

        if (cropName != null) {
            Log.d(TAG, "Received crop name: " + cropName);
            cropNameText.setText(cropName);
            loadCropDetails(cropName);
        } else {
            Log.e(TAG, "No crop name received in intent");
        }
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
                childList.add(parseNestedJSONObject(nestedKey, (JSONObject) value));
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

    private Map<String, Object> parseNestedJSONObject(String key, JSONObject jsonObject) throws JSONException {
        Map<String, Object> nestedMap = new HashMap<>();
        nestedMap.put("key", key);
        List<String> values = new ArrayList<>();
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String nestedKey = keys.next();
            Object value = jsonObject.get(nestedKey);
            if (value instanceof JSONObject) {
                nestedMap.put(nestedKey, parseNestedJSONObject(nestedKey, (JSONObject) value));
            } else if (value instanceof String) {
                values.add(nestedKey + ": " + value);
            } else if (value instanceof org.json.JSONArray) {
                List<String> arrayValues = new ArrayList<>();
                org.json.JSONArray jsonArray = (org.json.JSONArray) value;
                for (int i = 0; i < jsonArray.length(); i++) {
                    arrayValues.add(jsonArray.getString(i));
                }
                values.add(nestedKey + ":\n" + String.join("\n", arrayValues));
            }
        }
        nestedMap.put("values", values);
        return nestedMap;
    }

}
