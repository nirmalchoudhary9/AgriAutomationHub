package com.example.agriautomationhub;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

public class CropDetailActivity extends AppCompatActivity {

    private static final String TAG = "CropDetailActivity";
    TextView cropNameText;
    ExpandableListView expandableListView;
    CropDetailAdapter listAdapter;
    List<String> listDataHeader;
    HashMap<String, List<Object>> listDataChild;
    String current_lang = null;

    @Override
    protected void attachBaseContext(Context newBase) {
        // Get the saved language from shared preferences
        SharedPreferences sharedPreferences = newBase.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        String language = sharedPreferences.getString("app_language", "en");  // Default to English if not set

        Context context = LocaleHelper.setLocale(newBase);
        super.attachBaseContext(context);
    }

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

    private String getAppLanguage() {
        // Retrieve the selected language from shared preferences
        SharedPreferences preferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
        return preferences.getString("AppLanguage", Locale.getDefault().getLanguage());
    }

    private void loadCropDetails(String cropName) {
        try {
            // Get the app language instead of system language
            String currentLanguage = getAppLanguage(); // Use the custom method
            String jsonFileName; // Default to English JSON
            current_lang = currentLanguage;

            // Choose the appropriate JSON file based on the language
            switch (currentLanguage) {
                case "hi": // Hindi
                    jsonFileName = "crop_details_hindi.json";
                    break;
                case "en":
                    jsonFileName = "crop_details_en.json";  // Spanish JSON file
                    break;
                default:
                    jsonFileName = "crop_details_en.json"; // Default to English
                    break;
            }

            Log.d(TAG, "Loading JSON file: " + jsonFileName);

            // Load the appropriate JSON file
            InputStream inputStream = getAssets().open(jsonFileName);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();

            String json = new String(buffer, "UTF-8");
            JSONObject jsonObject = new JSONObject(json);

            if (jsonObject.has(cropName)) {
                JSONObject cropObject = jsonObject.getJSONObject(cropName);
                Log.d(TAG, "Crop details found for: " + cropName);

                listDataHeader = new ArrayList<>();
                listDataChild = new HashMap<>();

                // Populate the expandable list with crop details
                if(current_lang.equals("en"))
                {
                    for (String s : Arrays.asList("Plant Selection", "Planting", "Monitoring", "Site Selection", "Field Preparation", "Weeding", "Irrigation", "Fertilization Organic", "Fertilization Chemical", "Preventive Measure", "Plant Protection Chemical", "Harvesting", "Post-Harvest")) {
                        populateListData(cropObject, s);
                    }
                }
                else if(current_lang.equals("hi"))
                {
                    for (String s : Arrays.asList("पौधों का चयन", "बीजाई", "निगरानी", "स्थान चयन", "फसल की तैयारी", "खरपतवार", "सिंचाई", "कार्बनिक उर्वरक", "रासायनिक उर्वरक", "निवारक उपाय", "रसायनिक संरक्षण", "कटाई", "पोस्ट-हार्वेस्ट")) {
                        populateListData(cropObject, s);
                    }
                }


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
            } else if (value instanceof JSONArray) {
                List<String> arrayValues = new ArrayList<>();
                JSONArray jsonArray = (JSONArray) value;
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
            } else if (value instanceof JSONArray) {
                List<String> arrayValues = new ArrayList<>();
                JSONArray jsonArray = (JSONArray) value;
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
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
