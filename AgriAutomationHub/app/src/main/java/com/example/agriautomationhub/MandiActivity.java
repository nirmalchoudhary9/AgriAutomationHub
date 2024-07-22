package com.example.agriautomationhub;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.DatePickerDialog;
import android.widget.ArrayAdapter;
import android.graphics.Typeface;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Calendar;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MandiActivity extends AppCompatActivity {

    private static final String TAG = "MandiActivity";

    private TextView reportDateTextView;
    private Spinner spinnerMandiName, spinnerCrop;
    private Button btnFetchData;
    private LinearLayout linearLayoutResults;
    private Calendar calendar;

    private final OkHttpClient client = new OkHttpClient();
    private final String BASE_URL = "https://www.eanugya.mp.gov.in/Anugya_e/frontData.asmx";

    private List<String> mandiNames = new ArrayList<>();
    private List<String> cropNames = new ArrayList<>();

    private Map<String, String> mandiToDistrictMap = new HashMap<>();
    private Map<String, String> cropToCommGroupMap = new HashMap<>();
    private Map<String, String> mandiMap = new HashMap<>();
    private Map<String, String> cropMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mandi);

        reportDateTextView = findViewById(R.id.report_date);
        calendar = Calendar.getInstance();
        spinnerMandiName = findViewById(R.id.mandi_name);
        spinnerCrop = findViewById(R.id.crop);
        btnFetchData = findViewById(R.id.btn_fetch_data);
        LinearLayout linearLayoutResults = findViewById(R.id.linear_layout_results);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_mandi);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
            else if (id == R.id.navigation_news)
            {
                startActivity(new Intent(getApplicationContext(), NewsActivity.class));
                return true;
            }
            else if (id == R.id.navigation_mandi)
            {
                startActivity(new Intent(MandiActivity.this, MandiActivity.class));
                return true;
            }
            return false;
        });

        loadJsonData();

        btnFetchData.setOnClickListener(v -> fetchDataAndDisplayResults());

        reportDateTextView.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                MandiActivity.this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);

                    // Check if selected date is not in the future
                    if (selectedDate.after(Calendar.getInstance())) {
                        // Show error or reset to current date
                        reportDateTextView.setText("Select Date");
                    } else {
                        // Update TextView with selected date
                        String date = String.format("%d/%d/%d", dayOfMonth, month + 1, year);
                        reportDateTextView.setText(date);
                        // Optionally, call checkAllSelections() to enable fetchDataButton
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Set maximum date to today
        datePickerDialog.getDatePicker().setMaxDate(Calendar.getInstance().getTimeInMillis());

        datePickerDialog.show();
    }

    private void loadJsonData() {
        try {
            // Load JSON data
            InputStream mandiDataStream = getAssets().open("mandi_data.json");
            InputStream cropDataStream = getAssets().open("crop_data.json");

            BufferedReader mandiReader = new BufferedReader(new InputStreamReader(mandiDataStream));
            BufferedReader cropReader = new BufferedReader(new InputStreamReader(cropDataStream));

            StringBuilder mandiStringBuilder = new StringBuilder();
            StringBuilder cropStringBuilder = new StringBuilder();

            String line;
            while ((line = mandiReader.readLine()) != null) {
                mandiStringBuilder.append(line);
            }
            while ((line = cropReader.readLine()) != null) {
                cropStringBuilder.append(line);
            }

            mandiReader.close();
            cropReader.close();

            // Convert JSON strings to objects
            JSONArray mandiJsonArray = new JSONArray(mandiStringBuilder.toString());
            JSONArray cropJsonArray = new JSONArray(cropStringBuilder.toString());

            // Parse mandi JSON data
            for (int i = 0; i < mandiJsonArray.length(); i++) {
                JSONObject mandiObject = mandiJsonArray.getJSONObject(i);
                int districtCode = mandiObject.getInt("distCode");
                int mandiCode = mandiObject.getInt("mandiCode");
                String mandiName = mandiObject.getString("mandiName");
                mandiMap.put(mandiName, String.valueOf(mandiCode));
                mandiToDistrictMap.put(mandiName, String.valueOf(districtCode));
                mandiNames.add(mandiName);
                Log.d(TAG, "Fetching data for DistCode=" + districtCode + ", MandiCode=" + mandiCode );

            }

            // Parse crop JSON data
            for (int i = 0; i < cropJsonArray.length(); i++) {
                JSONObject cropObject = cropJsonArray.getJSONObject(i);
                int commGroupCode = cropObject.getInt("commGroupCode");
                int cropCode = cropObject.getInt("commCode");
                String cropName = cropObject.getString("commName");
                cropMap.put(cropName, String.valueOf(cropCode));
                cropToCommGroupMap.put(cropName, String.valueOf(commGroupCode));
                cropNames.add(cropName);
                Log.d(TAG, "Fetching data for CommGroupCode=" + commGroupCode + ", CommCode=" + cropCode);

            }

            // Set data to spinners
            ArrayAdapter<String> mandiAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mandiNames);
            spinnerMandiName.setAdapter(mandiAdapter);

            ArrayAdapter<String> cropAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cropNames);
            spinnerCrop.setAdapter(cropAdapter);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error loading JSON data", e);
        }
    }

    private void fetchDataAndDisplayResults() {
        String reportDate = reportDateTextView.getText().toString();
        String mandiName = spinnerMandiName.getSelectedItem() != null ? spinnerMandiName.getSelectedItem().toString() : null;
        String cropName = spinnerCrop.getSelectedItem() != null ? spinnerCrop.getSelectedItem().toString() : null;

        if (reportDate.equals("Select Date") || mandiName == null || cropName == null) {
            Log.e(TAG, "Invalid input values: Date=" + reportDate + ", Mandi=" + mandiName + ", Crop=" + cropName);
            return;
        }

        String distCode = mandiToDistrictMap.get(mandiName);
        String mandiCode = mandiMap.get(mandiName);
        String commGroupCode = cropToCommGroupMap.get(cropName);
        String commCode = cropMap.get(cropName);

        Log.d(TAG, "Fetching data for Date=" + reportDate + ", DistCode=" + distCode + ", MandiCode=" + mandiCode + ", CommGroupCode=" + commGroupCode + ", CommCode=" + commCode);

        new FetchDataTask().execute(reportDate, distCode, mandiCode, commGroupCode, commCode);
    }

    private class FetchDataTask extends AsyncTask<String, Void, Map<String, String>> {

        @Override
        protected Map<String, String> doInBackground(String... params) {
            String reportDate = params[0];
            String distCode = params[1];
            String mandiCode = params[2];
            String commGroupCode = params[3];
            String commCode = params[4];

            try {
                JSONObject cropData = getAllData(reportDate, distCode, mandiCode, commGroupCode, commCode);

                Log.d(TAG, "Fetching data for formatedDate=" + reportDate + ", DistCode=" + distCode + ", MandiCode=" + mandiCode + ", CommGroupCode=" + commGroupCode + ", CommCode=" + commCode);

                if (cropData.has("d") && cropData.getJSONArray("d").length() > 0) {
                    JSONObject data = cropData.getJSONArray("d").getJSONObject(0);
                    String minValue = data.optString("minValue", "N/A");
                    String maxValue = data.optString("maxValue", "N/A");
                    String commName = data.optString("commName", "N/A");
                    String commGroupName = data.optString("commGroupName", "N/A");
                    String mandiName = data.optString("mandiName", "N/A");
                    String distName = data.optString("distName", "N/A");

                    Map<String, String> result = new HashMap<>();
                    result.put("Date", reportDate);
                    result.put("Minimum Value", minValue);
                    result.put("Maximum Value", maxValue);
                    result.put("Commodity Name", commName);
                    result.put("Commodity Group Name", commGroupName);
                    result.put("Mandi Name", mandiName);
                    result.put("District Name", distName);

                    return result;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Error fetching data", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Map<String, String> result) {
            super.onPostExecute(result);
            // Ensure you are referring to the correct layout
            LinearLayout linearLayoutResults = findViewById(R.id.linear_layout_results);

            if (linearLayoutResults == null) {
                Log.e(TAG, "LinearLayout with ID 'linear_layout_results' not found");
                return;
            }

            if (result != null) {
                Log.d(TAG, "Data fetched successfully: " + result);
                linearLayoutResults.removeAllViews();

                // Create and add TextViews for each data item in the specified order
                addItemToLayout(linearLayoutResults, "Crop ", result.get("Commodity Name"));
                addItemToLayout(linearLayoutResults, "Minimum Price ", result.get("Minimum Value"));
                addItemToLayout(linearLayoutResults, "Maximum Price ", result.get("Maximum Value"));
                addItemToLayout(linearLayoutResults, "Mandi Name ", result.get("Mandi Name"));
                addItemToLayout(linearLayoutResults, "District Name ", result.get("District Name"));
                addItemToLayout(linearLayoutResults, "Date ", result.get("Date"));

            }
            else {
                Log.e(TAG, "No data fetched");
            }
        }
    }

    private void addItemToLayout(LinearLayout layout, String key, String value) {
        LinearLayout itemLayout = new LinearLayout(MandiActivity.this);
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setPadding(0, 8, 0, 8);

        TextView label = new TextView(MandiActivity.this);
        label.setText(key + ": ");
        label.setTypeface(null, Typeface.BOLD);
        label.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        ));

        TextView valueText = new TextView(MandiActivity.this);
        valueText.setText(value != null ? value : "N/A");
        valueText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                2f
        ));

        itemLayout.addView(label);
        itemLayout.addView(valueText);
        layout.addView(itemLayout);
    }

    private JSONObject getAllData(String reportDate, String distCode, String mandiCode, String commGroupCode, String commCode) throws IOException, JSONException, ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = dateFormat.parse(reportDate);

        SimpleDateFormat newDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = newDateFormat.format(date);

        Log.d(TAG, "Formatted Date: " + formattedDate);
        Log.d(TAG, "DistCode: " + distCode);
        Log.d(TAG, "MandiCode: " + mandiCode);
        Log.d(TAG, "CommGroupCode: " + commGroupCode);
        Log.d(TAG, "CommCode: " + commCode);

        String url = BASE_URL + "/GetAllData";
        JSONObject payload = new JSONObject();
        payload.put("date", formattedDate);
        payload.put("distCode", distCode);
        payload.put("mandiCode", mandiCode);
        payload.put("commGroupCode", commGroupCode);
        payload.put("commCode", commCode);

        Log.d(TAG, "Request Payload: " + payload.toString());

        RequestBody body = RequestBody.create(MediaType.get("application/json; charset=utf-8"), payload.toString());
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                Log.e(TAG, "Server Response: " + response);
                throw new IOException("Unexpected code " + response);
            }
            String responseData = response.body().string();
            Log.d(TAG, "Server Response Data: " + responseData);
            return new JSONObject(responseData);
        }
    }
}
