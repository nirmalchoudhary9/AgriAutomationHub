package com.example.agriautomationhub;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.app.DatePickerDialog;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.graphics.Typeface;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class MandiActivity extends AppCompatActivity {

    private static final String TAG = "MandiActivity";

    private TextView reportDateTextView;
    private Spinner spinnerDistrict;
    private LinearLayout mandiLinearLayout;
    private Calendar calendar;

    static {
        new OkHttpClient();
    }

    private final Map<String, List<String>> districtToMandiMap = new HashMap<>();
    private final Map<String, String> mandiToDistrictMap = new HashMap<>();
    private final Map<String, String> mandiMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mandi);

        reportDateTextView = findViewById(R.id.report_date);
        calendar = Calendar.getInstance();
        spinnerDistrict = findViewById(R.id.spinner_district);
        mandiLinearLayout = findViewById(R.id.mandi_linear_layout);

        ImageView back = findViewById(R.id.back_btn_mandi);
        back.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_mandi);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            } else if (id == R.id.navigation_news) {
                startActivity(new Intent(getApplicationContext(), NewsActivity.class));
                return true;
            }
            return false;
        });

        // Load district names from array.xml
        ArrayAdapter<CharSequence> districtAdapter = ArrayAdapter.createFromResource(
                this, R.array.districts, android.R.layout.simple_spinner_item);
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistrict.setAdapter(districtAdapter);

        // Load mandi data
        loadJsonData();

        // Set up listeners for district selection
        setupListeners();

        reportDateTextView.setOnClickListener(v -> showDatePickerDialog());
    }

    private void setupListeners() {
        spinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDistrict = (String) parent.getItemAtPosition(position);
                List<String> mandis = districtToMandiMap.get(selectedDistrict);

                mandiLinearLayout.removeAllViews();
                if (mandis != null) {
                    for (String mandi : mandis) {
                        addMandiTextView(mandi);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Optionally handle no selection
            }
        });
    }

    private void addMandiTextView(String mandiName) {
        // Create a CardView to hold the TextView
        CardView cardView = new CardView(this);
        cardView.setCardElevation(8);
        cardView.setRadius(16);
        cardView.setUseCompatPadding(true);

        // Set layout parameters for the CardView
        LinearLayout.LayoutParams cardLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardLayoutParams.setMargins(16, 16, 16, 16);
        cardView.setLayoutParams(cardLayoutParams);

        // Create a TextView for the mandi name
        TextView mandiTextView = new TextView(this);
        mandiTextView.setText(mandiName);
        mandiTextView.setTextSize(18);
        mandiTextView.setPadding(24, 24, 24, 24);
        mandiTextView.setTextColor(ContextCompat.getColor(this, R.color.primaryTextColor));
        mandiTextView.setTypeface(null, Typeface.BOLD);
        mandiTextView.setBackgroundResource(R.drawable.mandi_text_view_background); // Custom background drawable
        mandiTextView.setGravity(Gravity.CENTER_VERTICAL);

        // Set an OnClickListener for the TextView
        mandiTextView.setOnClickListener(v -> openMandiDetailActivity(mandiName));

        // Add the TextView to the CardView
        cardView.addView(mandiTextView);

        // Add the CardView to the LinearLayout
        mandiLinearLayout.addView(cardView);
    }


    private void showDatePickerDialog() {
        if (calendar == null) {
            calendar = Calendar.getInstance(); // Initialize if it's null
        }

        @SuppressLint("SetTextI18n") DatePickerDialog datePickerDialog = new DatePickerDialog(
                MandiActivity.this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);

                    if (selectedDate.after(Calendar.getInstance())) {
                        reportDateTextView.setText("Select Date");
                    } else {
                        @SuppressLint("DefaultLocale") String date = String.format("%d/%d/%d", dayOfMonth, month + 1, year);
                        reportDateTextView.setText(date);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMaxDate(Calendar.getInstance().getTimeInMillis());
        datePickerDialog.show();
    }

    private void openMandiDetailActivity(String mandiName) {
        String reportDate = reportDateTextView.getText().toString();
        String distCode = mandiToDistrictMap.get(mandiName);
        String mandiCode = mandiMap.get(mandiName);

        if (reportDate.equals("Select Date")) {
            Log.e(TAG, "Please select a valid date.");
            return;
        }

        if (distCode == null || mandiCode == null) {
            Log.e(TAG, "Invalid mandi name or data not found for mandi: " + mandiName);
            return;
        }

        // Start MandiDetailActivity
        Intent intent = new Intent(MandiActivity.this, MandiDetailActivity.class);
        intent.putExtra("mandiName", mandiName);
        intent.putExtra("reportDate", reportDate);
        intent.putExtra("distCode", distCode);
        intent.putExtra("mandiCode", mandiCode);
        startActivity(intent);
    }

    private void loadJsonData() {
        try {
            InputStream mandiDataStream = getAssets().open("mandi_data.json");
            BufferedReader mandiReader = new BufferedReader(new InputStreamReader(mandiDataStream));
            StringBuilder mandiStringBuilder = new StringBuilder();
            String line;
            while ((line = mandiReader.readLine()) != null) {
                mandiStringBuilder.append(line);
            }
            mandiReader.close();

            JSONArray jsonArray = new JSONArray(mandiStringBuilder.toString());

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject mandiObject = jsonArray.getJSONObject(i);
                String districtName = mandiObject.getString("distName");
                String mandiName = mandiObject.getString("mandiName");

                if (!districtToMandiMap.containsKey(districtName)) {
                    districtToMandiMap.put(districtName, new ArrayList<>());
                }
                Objects.requireNonNull(districtToMandiMap.get(districtName)).add(mandiName);
                mandiMap.put(mandiName, mandiObject.getString("mandiCode"));
                mandiToDistrictMap.put(mandiName, mandiObject.getString("distCode"));
            }

            Log.d(TAG, "District to Mandi Map: " + districtToMandiMap);

        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            Log.e(TAG, "Error loading JSON data", e);
        }
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
