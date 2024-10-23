package com.example.agriautomationhub;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MandiDetailActivity extends AppCompatActivity {

    private static final String TAG = "MandiDetailActivity";
    private static final String BASE_URL = "https://www.eanugya.mp.gov.in/Anugya_e/frontData.asmx";
    private LinearLayout linearLayoutResults;
    private TextView mandiNameTextView; // Declare the TextView for the Mandi name
    private TextView selectedDateTextView; // TextView for the selected date

    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mandi_detail);

        linearLayoutResults = findViewById(R.id.linear_layout_results);
        mandiNameTextView = findViewById(R.id.mandiNameTextView); // Initialize the TextView
        selectedDateTextView = findViewById(R.id.selectedDateTextView); // Initialize the TextView for the selected date

        // Retrieve data from intent
        AtomicReference<Intent> intent = new AtomicReference<>(getIntent());
        String reportDate = intent.get().getStringExtra("reportDate");
        String distCode = intent.get().getStringExtra("distCode");
        String mandiCode = intent.get().getStringExtra("mandiCode");
        String mandiName = intent.get().getStringExtra("mandiName");

        ImageView back = findViewById(R.id.back_btn_mandi_detail);
        back.setOnClickListener(v -> {
            intent.set(new Intent(getApplicationContext(), MandiActivity.class));
            startActivity(intent.get());
            finish();
        });

        // Set the Mandi name text
        if (mandiName != null && !mandiName.isEmpty()) {
            mandiNameTextView.setText(mandiName);
        } else {
            mandiNameTextView.setText("Unknown Mandi"); // Fallback text if Mandi name is not provided
        }

        // Set the selected date text
        if (reportDate != null && !reportDate.isEmpty()) {
            selectedDateTextView.setText("Selected Date: " + reportDate);
        } else {
            selectedDateTextView.setText("Date not provided"); // Fallback text if date is not provided
        }

        // Fetch and display data
        new FetchDataTask().execute(reportDate, distCode, mandiCode);
    }

    private class FetchDataTask extends AsyncTask<String, Void, List<Map<String, String>>> {

        @Override
        protected List<Map<String, String>> doInBackground(String... params) {
            String reportDate = params[0];
            String distCode = params[1];
            String mandiCode = params[2];

            try {
                JSONObject jsonResponse = getMandiTabData(reportDate, distCode, mandiCode);
                return parseJson(jsonResponse);
            } catch (IOException | JSONException | ParseException e) {
                Log.e(TAG, "Error fetching data", e);
            }
            return null;
        }
        @Override
        protected void onPostExecute(List<Map<String, String>> result) {
            if (linearLayoutResults == null) {
                Log.e(TAG, "LinearLayout with ID 'linear_layout_results' not found");
                return;
            }

            if (result != null && !result.isEmpty()) {
                Log.d(TAG, "Data fetched successfully: " + result);
                linearLayoutResults.removeAllViews();

                for (Map<String, String> data : result) {
                    addItemToLayout(data);
                }
            } else {
                Log.d(TAG, "No data available for the selected mandi");
                displayNoDataMessage();
            }
        }

        private void displayNoDataMessage() {
            // Remove any previous results
            linearLayoutResults.removeAllViews();

            // Display a message to the user
            TextView noDataTextView = new TextView(MandiDetailActivity.this);
            noDataTextView.setText("No data available for the selected options.");
            noDataTextView.setPadding(16, 16, 16, 16);
            noDataTextView.setTextSize(16);  // Optional: Set text size
            noDataTextView.setGravity(Gravity.CENTER);  // Optional: Center the text
            linearLayoutResults.addView(noDataTextView);
        }

        private JSONObject getMandiTabData(String reportDate, String distCode, String mandiCode) throws IOException, JSONException, ParseException {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = dateFormat.parse(reportDate);

            SimpleDateFormat newDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            String formattedDate = newDateFormat.format(date);

            String url = BASE_URL + "/GetMandiTabData";
            JSONObject payload = new JSONObject();
            payload.put("date", formattedDate);
            payload.put("date2", formattedDate);
            payload.put("distCode", distCode);
            payload.put("mandiCode", mandiCode);

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
                if (responseData.startsWith("{") || responseData.startsWith("[")) {
                    // If the response is JSON (unexpected)
                    return new JSONObject(responseData);
                }
                // Return as JSONObject to be consistent with expected output type
                return new JSONObject();
            }
        }

        private List<Map<String, String>> parseJson(JSONObject jsonResponse) {
            List<Map<String, String>> resultList = new ArrayList<>();
            try {
                JSONArray dataArray = jsonResponse.getJSONArray("d");

                for (int i = 0; i < ((JSONArray) dataArray).length(); i++) {
                    JSONObject dataObject = dataArray.getJSONObject(i);
                    Map<String, String> dataMap = new HashMap<>();

                    dataMap.put("Minimum Value", dataObject.optString("minValue", "N/A"));
                    dataMap.put("Maximum Value", dataObject.optString("maxValue", "N/A"));
                    dataMap.put("Crop Name", dataObject.optString("commName", "N/A"));

                    resultList.add(dataMap);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing JSON data", e);
            }
            return resultList;
        }
    }

    private void addItemToLayout(Map<String, String> data) {
        // Create a TableLayout to hold the data rows
        TableLayout tableLayout = new TableLayout(this);
        tableLayout.setPadding(32, 24, 32, 24); // Add padding for overall layout
        tableLayout.setBackgroundResource(R.drawable.table_background); // Optional: add background

        // Iterate through the data map and create rows for each key-value pair
        for (Map.Entry<String, String> entry : data.entrySet()) {
            TableRow tableRow = new TableRow(this);
            tableRow.setPadding(0, 8, 0, 8);// Padding between rows
            
            // Create TextView for the key with bold styling
            TextView keyTextView = new TextView(this);
            keyTextView.setText(entry.getKey() + ": ");
            keyTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            keyTextView.setTextColor(getResources().getColor(R.color.colorPrimary)); // Primary color for keys
            keyTextView.setTextSize(16); // Set text size
            keyTextView.setPadding(8, 0, 8, 0); // Padding within the key TextView

            // Create TextView for the value
            TextView valueTextView = new TextView(this);
            valueTextView.setText(entry.getValue());
            valueTextView.setTextColor(getResources().getColor(R.color.colorAccent)); // Accent color for values
            valueTextView.setTextSize(16); // Set text size
            valueTextView.setPadding(8, 0, 8, 0); // Padding within the value TextView

            // Add TextViews to the row
            tableRow.addView(keyTextView);
            tableRow.addView(valueTextView);

            // Add row to the TableLayout
            tableLayout.addView(tableRow);
        }

        // Add the TableLayout to the parent LinearLayout
        linearLayoutResults.addView(tableLayout);
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
