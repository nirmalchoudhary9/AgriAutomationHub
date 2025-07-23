package com.example.agriautomationhub;

import static com.example.agriautomationhub.net.MandiApi.convertDate;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.example.agriautomationhub.net.MandiApi;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import android.widget.Toast;

/**
 * Full MandiActivity – district→mandi, category→crop, API fetch, detail screen.
 */
public class MandiActivity extends AppCompatActivity {
    private static final String TAG = "MandiActivity";

    /* UI */
    private TextView reportDateTextView;
    private Spinner spinnerDistrict, spinnerMandi, spinnerCropCategory, spinnerCrop;
    private Calendar calendar;

    /* adapters */
    private ArrayAdapter<String> mandiAdapter;
    private ArrayAdapter<String> cropAdapter;

    /* maps */
    private final Map<String, List<String>> districtToMandiMap = new HashMap<>();
    private final Map<String, String> mandiToDistrictMap = new HashMap<>();
    private final Map<String, String> mandiMap = new HashMap<>();
    private final Map<String, List<String>> categoryToCropMap = new LinkedHashMap<>();
    private final Map<String, String> categoryToGroupCode = new HashMap<>();
    private final Map<String, String> cropToCommCode = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mandi);

        reportDateTextView = findViewById(R.id.report_date);
        spinnerDistrict    = findViewById(R.id.spinner_district);
        spinnerMandi       = findViewById(R.id.spinner_mandi);
        spinnerCropCategory= findViewById(R.id.spinner_crop_category);
        spinnerCrop        = findViewById(R.id.spinner_crop);
        calendar           = Calendar.getInstance();
        findViewById(R.id.btn_fetch).setOnClickListener(v -> openDetail());


        findViewById(R.id.back_btn_mandi).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        BottomNavigationView nav = findViewById(R.id.bottom_navigation_mandi);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                return true;
            } else if (id == R.id.navigation_news) {
                startActivity(new Intent(MandiActivity.this, NewsActivity.class));
                return true;
            } else if (id == R.id.navigation_marketView) {
                startActivity(new Intent(MandiActivity.this, MarketViewActivity.class));
                return true;
            } else if (id == R.id.navigation_mandi) {
                startActivity(new Intent(MandiActivity.this, MandiActivity.class));
                return true;
            }
            return false;
        });

        ArrayAdapter<CharSequence> distAdapter = ArrayAdapter.createFromResource(
                this, R.array.districts, android.R.layout.simple_spinner_item);
        distAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistrict.setAdapter(distAdapter);

        mandiAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        mandiAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMandi.setAdapter(mandiAdapter);

        ArrayAdapter<CharSequence> catAdapter = ArrayAdapter.createFromResource(
                this, R.array.crop_category, android.R.layout.simple_spinner_item);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCropCategory.setAdapter(catAdapter);

        cropAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        cropAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCrop.setAdapter(cropAdapter);

        loadMandiJson();
        loadCropMaster();
        setupListeners();

        reportDateTextView.setOnClickListener(v -> showDatePickerDialog());
    }

    /* ---------- data loaders ---------- */
    private void loadMandiJson() {
        try (InputStream is = getAssets().open("mandi_data.json");
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line; while ((line = br.readLine()) != null) sb.append(line);
            JSONArray arr = new JSONArray(sb.toString());
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                String dist  = o.getString("distName");
                String mandi = o.getString("mandiName");
                districtToMandiMap.computeIfAbsent(dist, k -> new ArrayList<>()).add(mandi);
                mandiToDistrictMap.put(mandi, o.getString("distCode"));
                mandiMap.put(mandi, o.getString("mandiCode"));
            }
        } catch (Exception e) { Log.e(TAG, "mandi json", e); }
    }

    private void loadCropMaster() {
        try (InputStream is = getAssets().open("crop_master.json");
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

            StringBuilder sb = new StringBuilder();

            String line; while ((line = br.readLine()) != null) sb.append(line);

            JSONArray groups = new JSONArray(sb.toString());
            for (int i = 0; i < groups.length(); i++) {
                JSONObject g = groups.getJSONObject(i);

                String groupCode = g.getString("commGroupCode");
                String groupName = g.getString("commGroupName");

                categoryToGroupCode.put(groupName, groupCode);

                JSONArray crops = g.getJSONArray("crops");
                List<String> cropNames = new ArrayList<>();

                for (int j = 0; j < crops.length(); j++) {
                    JSONObject c = crops.getJSONObject(j);
                    String commCode = c.getString("commCode");
                    String commName = c.getString("commName");

                    cropNames.add(commName);
                    cropToCommCode.put(commName, commCode);
                }
                categoryToCropMap.put(groupName, cropNames);
            }

            Log.d(TAG, "category→crop via JSON: " + categoryToCropMap);

        } catch (Exception e) {
            Log.e(TAG, "Error loading crop_master.json", e);
        }
    }

    /* ---------- listeners ---------- */
    private void setupListeners() {
        spinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                List<String> mandis = districtToMandiMap.getOrDefault(p.getItemAtPosition(pos), Collections.emptyList());
                mandiAdapter.clear(); mandiAdapter.addAll(mandis); mandiAdapter.notifyDataSetChanged();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerCropCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                List<String> crops = categoryToCropMap.getOrDefault(p.getItemAtPosition(pos), Collections.emptyList());
                cropAdapter.clear(); cropAdapter.addAll(crops); cropAdapter.notifyDataSetChanged();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    /* ---------- date picker ---------- */
    private void showDatePickerDialog() {
        new DatePickerDialog(this, (v, y, m, d) -> {
            Calendar sel = Calendar.getInstance();
            sel.set(y, m, d);

            if (sel.after(Calendar.getInstance())) {
                reportDateTextView.setText("Select Date");
            } else {
                // Show as dd/MM/yyyy
                String dateForDisplay = String.format("%02d/%02d/%04d", d, m + 1, y);
                reportDateTextView.setText(dateForDisplay);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    /* ---------- API + detail ---------- */
    private void openDetail() {
        String date = reportDateTextView.getText().toString();
        if (date.equals("Select Date")) { Log.e(TAG, "date missing"); return; }

        String mandiName = (String) spinnerMandi.getSelectedItem();
        String cropName  = (String) spinnerCrop.getSelectedItem();
        String category  = (String) spinnerCropCategory.getSelectedItem();
        if (mandiName == null || cropName == null || category == null) return;

        String distCode      = mandiToDistrictMap.get(mandiName);
        String mandiCode     = mandiMap.get(mandiName);
        String commGroupCode = categoryToGroupCode.get(category);
        String commCode      = cropToCommCode.get(cropName);
        if (distCode == null || mandiCode == null || commGroupCode == null || commCode == null) {
            Log.e(TAG, "codes missing"); return;
        }
        date = convertDate(date);
        String finalDate = date;
        Log.d(TAG, "PAYLOAD → date=" + finalDate +
                " dist=" + distCode +
                " mandi=" + mandiCode +
                " group=" + commGroupCode +
                " comm=" + commCode);
        new MandiApi().fetchMandiData(date, distCode, mandiCode, commGroupCode, commCode, new Callback() {
            @Override public void onFailure(Call c, IOException e) { runOnUiThread(() -> Log.e(TAG, "net", e)); }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> Log.e(TAG, "srv " + response.code()));
                    return;
                }

                // Safely read the response body only once
                final String json;
                try {
                    json = response.body().string(); // Can be called only once
                } catch (Exception e) {
                    runOnUiThread(() -> Log.e(TAG, "Failed to read body", e));
                    return;
                }

//                runOnUiThread(() -> {
//                    Intent i = new Intent(MandiActivity.this, MandiDetailActivity.class);
//                    i.putExtra("mandiName", mandiName);
//                    i.putExtra("reportDate", finalDate);
//                    i.putExtra("mandiData", json);
//                    startActivity(i);
//                });

                runOnUiThread(() -> {
                    try {
                        LinearLayout container = findViewById(R.id.mandi_data_container);
                        container.removeAllViews();

                        JSONObject root = new JSONObject(json);
                        JSONArray arr = root.optJSONArray("d");

                        if (arr == null || arr.length() == 0) {
                            TextView noData = new TextView(MandiActivity.this);
                            noData.setText("No data available.");
                            container.addView(noData);
                            return;
                        }

                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject item = arr.getJSONObject(i);

                            View card = getLayoutInflater().inflate(R.layout.item_mandi_data_card, container, false);

                            ((TextView) card.findViewById(R.id.variety)).setText("फसल: " + item.optString("commName", "N/A"));
                            ((TextView) card.findViewById(R.id.min_price)).setText("न्यूनतम: ₹" + item.optString("minValue", "-"));
                            ((TextView) card.findViewById(R.id.max_price)).setText("अधिकतम: ₹" + item.optString("maxValue", "-"));
                            container.addView(card);
                        }
                    } catch (Exception e) {
                        Log.e("MandiActivity", "JSON parsing error", e);
                        Toast.makeText(MandiActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /* ---------- menu ---------- */
    @Override public boolean onCreateOptionsMenu(Menu menu) { getMenuInflater().inflate(R.menu.main, menu); return true; }
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) { FirebaseAuth.getInstance().signOut(); startActivity(new Intent(this, LoginActivity.class)); finish(); return true; }
        if (item.getItemId() == R.id.action_settings) { startActivity(new Intent(this, SettingsPage.class)); return true; }
        if (item.getItemId() == R.id.action_help) { startActivity(new Intent(this, HelpActivity.class)); return true; }
        return super.onOptionsItemSelected(item);
    }
}
