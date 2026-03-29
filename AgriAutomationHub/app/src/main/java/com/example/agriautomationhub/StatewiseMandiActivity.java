package com.example.agriautomationhub;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.MaterialDatePicker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StatewiseMandiActivity extends AppCompatActivity {

    private static final String TAG = "StatewiseMandiActivity";

    private AutoCompleteTextView spinnerState, spinnerDistrict, spinnerCommodity;
    private TextView selectedDateText;
    private Button btnFetch;

    private RecyclerView recyclerView;
    private MandiAdapter mandiAdapter;
    private List<MandiData> mandiDataList = new ArrayList<>();

    private String selectedDate = "";

    // Only required data structures
    private final HashMap<String, List<String>> stateToDistricts = new HashMap<>();
    private final List<String> stateList = new ArrayList<>();
    private final List<String> commodityList = new ArrayList<>();

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

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .build();

        selectedDateText.setOnClickListener(v ->
                datePicker.show(getSupportFragmentManager(), "DATE_PICKER"));

        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
            selectedDate = sdf.format(new Date(selection));
            selectedDateText.setText(selectedDate);
        });

        loadCsvData();
        setupStateSpinner();
        setupCommoditySpinner();

        spinnerState.setOnItemClickListener((parent, view, position, id) -> {
            String selectedState = parent.getItemAtPosition(position).toString();
            updateDistrictSpinner(selectedState);
        });

        btnFetch.setOnClickListener(v -> {
            String state = spinnerState.getText().toString().trim();
            String district = spinnerDistrict.getText().toString().trim();
            String commodity = spinnerCommodity.getText().toString().trim();

            if (state.isEmpty() || district.isEmpty() || commodity.isEmpty() || selectedDate.isEmpty()) {
                Toast.makeText(this, "Please select all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String url = buildUrl(state, district, commodity, convertDateFormat(selectedDate));

            Log.d(TAG, "URL: " + url);

            fetchAndParseApiData(url);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_mandi);
        bottomNavigationView.setSelectedItemId(R.id.navigation_mandi);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else if (item.getItemId() == R.id.navigation_profile) {
                startActivity(new Intent(this, ProfilePageActivity.class));
                finish();
            } else if (item.getItemId() == R.id.navigation_news) {
                startActivity(new Intent(this, NewsActivity.class));
                finish();
            }
            return false;
        });
    }

    private void loadCsvData() {
        try {
            InputStream stateStream = getAssets().open("state_district.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(stateStream));
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");

                String state = parts[1].trim();
                String district = parts[3].trim();

                if (!stateToDistricts.containsKey(state)) {
                    stateToDistricts.put(state, new ArrayList<>());
                    stateList.add(state);
                }

                stateToDistricts.get(state).add(district);
            }

            reader.close();

            InputStream commStream = getAssets().open("commodities.csv");
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(commStream));
            reader2.readLine();

            while ((line = reader2.readLine()) != null) {
                String[] parts = line.split(",");
                String commodity = parts[0].trim();
                commodityList.add(commodity);
            }

            reader2.close();

        } catch (Exception e) {
            Toast.makeText(this, "CSV Load Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupStateSpinner() {
        Collections.sort(stateList);
        spinnerState.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, stateList));
    }

    private void updateDistrictSpinner(String state) {
        List<String> districts = stateToDistricts.get(state);
        if (districts != null) {
            Collections.sort(districts);
            spinnerDistrict.setAdapter(new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, districts));
        }
    }

    private void setupCommoditySpinner() {
        Collections.sort(commodityList);
        spinnerCommodity.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, commodityList));
    }

    public static String convertDateFormat(String inputDate) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
            SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
            Date date = input.parse(inputDate);
            return output.format(date);
        } catch (Exception e) {
            return "";
        }
    }

    private String buildUrl(String state, String district, String commodity, String date) {
        try {
            return "https://api.data.gov.in/resource/35985678-0d79-46b4-9ed6-6f13308a1d24?" +
                    "api-key=579b464db66ec23bdd000001154c00c96862492d7339442d4186089e" +
                    "&format=json&limit=10" +
                    "&filters[State]=" +state +
                    "&filters[District]=" + district+
                    "&filters[Commodity]=" + commodity+
                    "&filters[Arrival_Date]=" + date;

        } catch (Exception e) {
            return "";
        }
    }

    private void fetchAndParseApiData(String url) {
        OkHttpClient client = new OkHttpClient();

        new Thread(() -> {
            try {
                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();

                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "HTTP Error", Toast.LENGTH_SHORT).show());
                    return;
                }

                String json = response.body().string();
                JSONObject root = new JSONObject(json);
                JSONArray records = root.getJSONArray("records");

                mandiDataList.clear();

                for (int i = 0; i < records.length(); i++) {
                    JSONObject obj = records.getJSONObject(i);

                    mandiDataList.add(new MandiData(
                            obj.optString("Market"),
                            obj.optString("Commodity"),
                            obj.optString("Min_Price"),
                            obj.optString("Max_Price"),
                            obj.optString("Arrival_Date")
                    ));
                }

                runOnUiThread(() -> { if (mandiDataList.isEmpty()) { Toast.makeText(this, "⚠️ No mandi data found.",Toast.LENGTH_LONG).show(); } else { mandiAdapter.notifyDataSetChanged(); } });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}