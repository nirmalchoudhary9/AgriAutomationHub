package com.example.agriautomationhub;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import android.widget.AutoCompleteTextView;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.MaterialDatePicker;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StatewiseMandiActivity extends AppCompatActivity {
    private static final String TAG = "StatewiseMandiActivity";

    private AutoCompleteTextView spinnerState, spinnerDistrict, spinnerCommodity;
    private TextView selectedDateText, mandiOutputTextView;
    private Button  btnFetch;
    RecyclerView recyclerView;
    MandiAdapter mandiAdapter;
    List<MandiData> mandiDataList = new ArrayList<>();

    private String selectedDate = "";
    private final HashMap<String, String[]> stateDistrictMap = new HashMap<>();
    private final HashMap<String, List<String>> stateToDistricts = new HashMap<>();
    private final HashMap<String, String[]> commodityMap = new HashMap<>();
    private final HashMap<String, List<String>> groupToCommodity = new HashMap<>();

    private final HashSet<String> stateSet = new HashSet<>();
    private final HashMap<String, String> stateCodeMap = new HashMap<>();
    private final HashMap<String, String> districtCodeMap = new HashMap<>();

    private final HashSet<String> groupSet = new HashSet<>();
    private final HashMap<String, String> groupCodeMap = new HashMap<>();
    private final HashMap<String, String> commodityCodeMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statewise_mandi);

        spinnerState = findViewById(R.id.spinnerState);
        spinnerDistrict = findViewById(R.id.spinnerDistrict);
        spinnerCommodity = findViewById(R.id.spinnerCommodity);
        selectedDateText = findViewById(R.id.selectDate);
        btnFetch = findViewById(R.id.btnFetch);
        recyclerView = findViewById(R.id.mandiRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mandiAdapter = new MandiAdapter(mandiDataList);
        recyclerView.setAdapter(mandiAdapter);


        findViewById(R.id.back_btn_mandi).setOnClickListener(v -> onBackPressed());

        // Date Picker
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .build();

        selectedDateText.setOnClickListener(v -> datePicker.show(getSupportFragmentManager(), "DATE_PICKER"));

        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
            selectedDate = sdf.format(new Date(selection));
            selectedDateText.setText(selectedDate);
        });

        loadCsvMappings();

        setupStateSpinner();
        setupCommoditySpinner();

        // AutoCompleteTextView selection listener
        spinnerState.setOnItemClickListener((parent, view, position, id) -> {
            String selectedState = parent.getItemAtPosition(position).toString();
            updateDistrictSpinner(selectedState);
        });

        btnFetch.setOnClickListener(v -> {
            String state = spinnerState.getText().toString();
            String district = spinnerDistrict.getText().toString();
            String commodity = spinnerCommodity.getText().toString();
            String date = selectedDate;


            String key = (state + "-" + district).toLowerCase().trim();
            String key2 = (commodity).toLowerCase().trim();
            if (!stateDistrictMap.containsKey(key)) {
                mandiOutputTextView.setText("Invalid state-district combination.");
                return;
            }

            if (!commodityMap.containsKey(key2)) {
                mandiOutputTextView.setText("Invalid commodity.");
                return;
            }

            String[] codes = stateDistrictMap.get(key);
            String[] codes2 = commodityMap.get(key2);
            String url = buildUrl(codes[0], codes[1], codes2[1], codes2[0], convertDateFormat(date));

            Log.d(TAG, url);

            String part2 = url.substring(20);

            Log.w(TAG, part2);

            fetchAndParseApiData(url);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_mandi);
        bottomNavigationView.setSelectedItemId(R.id.navigation_mandi);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }else if (id == R.id.navigation_profile) {
                // Handle News navigation
                startActivity(new Intent(getApplicationContext(), ProfilePageActivity.class));
                finish();
            } else if (id == R.id.navigation_news) {
                // Handle News navigation
                startActivity(new Intent(getApplicationContext(), NewsActivity.class));
                finish();
            }
            return false;
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_mandi);
        bottomNavigationView.setSelectedItemId(R.id.navigation_mandi);
    }

    public static String convertDateFormat(String inputDate) {
        try {
            SimpleDateFormat inputFormat =
                    new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);

            SimpleDateFormat outputFormat =
                    new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

            Date date = inputFormat.parse(inputDate);
            return outputFormat.format(date);

        } catch (Exception e) {
            return ""; // or handle error properly
        }
    }

    private void loadCsvMappings() {
        try {
            InputStream stateStream = getAssets().open("state_district.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(stateStream));
            reader.readLine(); // skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String stateCode = parts[0].trim();
                    String stateName = parts[1].trim();
                    String districtCode = parts[2].trim();
                    String districtName = parts[3].trim();

                    String key = (stateName + "-" + districtName).toLowerCase();
                    stateDistrictMap.put(key, new String[]{stateCode, districtCode});
                    stateSet.add(stateName);
                    stateCodeMap.put(stateName, stateCode);
                    districtCodeMap.put(stateName + "-" + districtName, districtCode);

                    stateToDistricts.putIfAbsent(stateName, new ArrayList<>());
                    stateToDistricts.get(stateName).add(districtName);
                }
            }

            InputStream commStream = getAssets().open("commodities.csv");
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(commStream));
            reader2.readLine(); // skip header
            while ((line = reader2.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String groupCode = parts[2].trim();
                    String CommodityCode = parts[1].trim();
                    String CommodityName = parts[0].trim();

                    String key = (CommodityName).toLowerCase();
                    commodityMap.put(key, new String[]{groupCode, CommodityCode});
                }
            }
            reader2.close();
        } catch (IOException e) {
            Log.e("MANDI_LOG", "CSV Error: " + e.getMessage());
        }
    }

    private void setupStateSpinner() {
        List<String> sortedStates = new ArrayList<>(stateSet);
        Collections.sort(sortedStates);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, sortedStates);
        spinnerState.setAdapter(adapter);
    }

    private void updateDistrictSpinner(String selectedState) {
        List<String> districts = stateToDistricts.get(selectedState);
        if (districts != null) {
            Collections.sort(districts);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, districts);
            spinnerDistrict.setAdapter(adapter);
        }
    }

    private void setupCommoditySpinner() {
        List<String> commodities = new ArrayList<>(commodityMap.keySet());
        Collections.sort(commodities);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, commodities);
        spinnerCommodity.setAdapter(adapter);
    }

    private String buildUrl(String stateCode, String districtCode, String commodityCode,
                            String group, String date) {
        try {
            return "https://api.agmarknet.gov.in/v1/daily-price-arrival/report?" +
                    "from_date=" + URLEncoder.encode(date, "UTF-8") +
                    "&to_date=" + URLEncoder.encode(date, "UTF-8") +
                    "&data_type=100004" +
                    "&group=" + group +
                    "&commodity=" + commodityCode +
                    "&state=" + URLEncoder.encode("[" + stateCode + "]", "UTF-8") +
                    "&district=" + URLEncoder.encode("[" + districtCode + "]", "UTF-8") +
                    "&market=[100002]" +
                    "&grade=[100003]" +
                    "&variety=[100007]" +
                    "&page=1"+
                    "&limit=10";
        } catch (Exception e) {
            return "";
        }
    }

    private void fetchAndParseApiData(String url) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0")
                .header("Accept", "application/json")
                .build();

        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {

                if (!response.isSuccessful()) {
                    runOnUiThread(() -> showErrorRow("HTTP Error: " + response.code()));
                    return;
                }

                String jsonResponse = response.body().string();
                JSONObject root = new JSONObject(jsonResponse);

                if (!root.getBoolean("status")) {
                    runOnUiThread(() -> showErrorRow("API returned no data"));
                    return;
                }

                JSONArray records =
                        root.getJSONObject("data")
                                .getJSONArray("records");

                mandiDataList.clear();

                if (records.length() == 0) {
                    runOnUiThread(() -> showErrorRow("⚠️ No mandi data found."));
                    return;
                }

                JSONArray dataArray =
                        records.getJSONObject(0)
                                .getJSONArray("data");

                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject item = dataArray.getJSONObject(i);

                    String market = item.optString("market_name");
                    String commodity = item.optString("cmdt_name");
                    String minPrice = item.optString("min_price");
                    String maxPrice = item.optString("max_price");
                    String date = item.optString("arrival_date");

                    MandiData mandiData =
                            new MandiData(market, commodity, minPrice, maxPrice, date);

                    mandiDataList.add(mandiData);
                }

                runOnUiThread(() -> {
                    if (mandiDataList.isEmpty()) {
                        showErrorRow("⚠️ No mandi data found.");
                    } else {
                        mandiAdapter.notifyDataSetChanged();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        showErrorRow("❌ Error: " + e.getMessage())
                );
            }
        }).start();
    }

    private void showErrorRow(String message) {
        runOnUiThread(() ->
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        );
    }
}