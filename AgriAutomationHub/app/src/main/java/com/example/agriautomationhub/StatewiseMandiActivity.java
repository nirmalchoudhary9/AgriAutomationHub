package com.example.agriautomationhub;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.MaterialDatePicker;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StatewiseMandiActivity extends AppCompatActivity {

    private Spinner spinnerState, spinnerDistrict, spinnerCommodity;
    private TextView selectedDateText, mandiOutputTextView;
    private Button  btnFetch;
    RecyclerView recyclerView;
    MandiAdapter mandiAdapter;
    List<MandiData> mandiDataList = new ArrayList<>();
    private String selectedDate = "";
    private final HashMap<String, String[]> stateDistrictMap = new HashMap<>();
    private final HashMap<String, List<String>> stateToDistricts = new HashMap<>();
    private final HashMap<String, String> commodityMap = new HashMap<>();

    private final HashSet<String> stateSet = new HashSet<>();
    private final HashMap<String, String> stateCodeMap = new HashMap<>();
    private final HashMap<String, String> districtCodeMap = new HashMap<>();

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


        findViewById(R.id.back_btn_mandi).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

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

        btnFetch.setOnClickListener(v -> {
            String state = spinnerState.getSelectedItem().toString();
            String district = spinnerDistrict.getSelectedItem().toString();
            String commodity = spinnerCommodity.getSelectedItem().toString();
            String date = selectedDate;

            String key = (state + "-" + district).toLowerCase().trim();
            if (!stateDistrictMap.containsKey(key)) {
                mandiOutputTextView.setText("Invalid state-district combination.");
                return;
            }

            if (!commodityMap.containsKey(commodity.toLowerCase().trim())) {
                mandiOutputTextView.setText("Invalid commodity.");
                return;
            }

            String[] codes = stateDistrictMap.get(key);
            String url = buildUrl(
                    codes[0], codes[1], commodityMap.get(commodity.toLowerCase().trim()), state, district, commodity, date
            );

            fetchAndParseHtml(url);
        });

        spinnerState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                updateDistrictSpinner(spinnerState.getSelectedItem().toString());
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        BottomNavigationView nav = findViewById(R.id.bottom_navigation_mandi);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                return true;
            } else if (id == R.id.navigation_news) {
                startActivity(new Intent(StatewiseMandiActivity.this, NewsActivity.class));
                return true;
            } else if (id == R.id.navigation_marketView) {
                startActivity(new Intent(StatewiseMandiActivity.this, MarketViewActivity.class));
                return true;
            } else if (id == R.id.navigation_mandi) {
                startActivity(new Intent(StatewiseMandiActivity.this, MandiActivity.class));
                return true;
            }
            return false;
        });
    }

    private void loadCsvMappings() {
        try {
            InputStream stateStream = getAssets().open("mandi_state_map.csv");
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

            InputStream commStream = getAssets().open("commodity_mapping.csv");
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(commStream));
            reader2.readLine(); // skip header
            while ((line = reader2.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    commodityMap.put(parts[0].trim().toLowerCase(), parts[1].trim());
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sortedStates);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerState.setAdapter(adapter);
    }

    private void updateDistrictSpinner(String selectedState) {
        List<String> districts = stateToDistricts.get(selectedState);
        if (districts != null) {
            Collections.sort(districts);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, districts);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerDistrict.setAdapter(adapter);
        }
    }

    private void setupCommoditySpinner() {
        List<String> commodities = new ArrayList<>(commodityMap.keySet());
        Collections.sort(commodities);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, commodities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCommodity.setAdapter(adapter);
    }

    private String buildUrl(String stateCode, String districtCode, String commodityCode,
                            String state, String district, String commodity, String date) {
        try {
            return "https://agmarknet.gov.in/SearchCmmMkt.aspx?" +
                    "Tx_Commodity=" + URLEncoder.encode(commodityCode, "UTF-8") +
                    "&Tx_State=" + URLEncoder.encode(stateCode, "UTF-8") +
                    "&Tx_District=" + URLEncoder.encode(districtCode, "UTF-8") +
                    "&Tx_Market=0" +
                    "&DateFrom=" + URLEncoder.encode(date, "UTF-8") +
                    "&DateTo=" + URLEncoder.encode(date, "UTF-8") +
                    "&Fr_Date=" + URLEncoder.encode(date, "UTF-8") +
                    "&To_Date=" + URLEncoder.encode(date, "UTF-8") +
                    "&Tx_Trend=0" +
                    "&Tx_CommodityHead=" + URLEncoder.encode(commodity, "UTF-8") +
                    "&Tx_StateHead=" + URLEncoder.encode(state, "UTF-8") +
                    "&Tx_DistrictHead=" + URLEncoder.encode(district, "UTF-8") +
                    "&Tx_MarketHead=" + URLEncoder.encode("--Select--", "UTF-8");
        } catch (Exception e) {
            return "";
        }
    }

    private void fetchAndParseHtml(String url) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0")
                .build();

        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> showErrorRow("HTTP Error: " + response.code()));
                    return;
                }

                String html = response.body().string();
                Document doc = Jsoup.parse(html);
                Elements tables = doc.select("table");
                Element mandiTable = null;

                for (Element table : tables) {
                    Elements headerRow = table.select("tr").first() != null ? table.select("tr").first().select("th") : null;
                    if (headerRow != null) {
                        for (Element header : headerRow) {
                            String headerText = header.text().toLowerCase();
                            if (headerText.contains("market") || headerText.contains("arrival") || headerText.contains("commodity")) {
                                mandiTable = table;
                                break;
                            }
                        }
                    }
                    if (mandiTable != null) break;
                }

                if (mandiTable == null) {
                    runOnUiThread(() -> showErrorRow("⚠️ Mandi data table not found."));
                    return;
                }

                Elements rows = mandiTable.select("tr");
                mandiDataList.clear(); // Clear old data

                for (int i = 1; i < rows.size(); i++) { // skip header
                    Elements cols = rows.get(i).select("td");

                    if (cols.size() >= 10) {
                        String market = cols.get(2).text();
                        String commodity = cols.get(3).text();
                        String minPrice = cols.get(6).text();
                        String maxPrice = cols.get(7).text();
                        String date = cols.get(9).text();

                        MandiData data = new MandiData(market, commodity, minPrice, maxPrice, date);
                        mandiDataList.add(data);
                    }
                }

                runOnUiThread(() -> {
                    if (mandiDataList.isEmpty()) {
                        showErrorRow("⚠️ No mandi data found for this selection.");
                    } else {
                        mandiAdapter.notifyDataSetChanged();
                    }
                });

            } catch (IOException e) {
                runOnUiThread(() -> showErrorRow("❌ Request error: " + e.getMessage()));
            } catch (Exception e) {
                runOnUiThread(() -> showErrorRow("❌ Unexpected error: " + e.getMessage()));
            }
        }).start();
    }

    private void showErrorRow(String message) {
        runOnUiThread(() ->
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        );
    }
}
