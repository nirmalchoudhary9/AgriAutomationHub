package com.example.agriautomationhub;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MarketViewActivity extends AppCompatActivity {
    private static final String TAG = "MarketViewActivity";

    private AutoCompleteTextView districtSpinner, mandiSpinner, commGroupSpinner, cropSpinner;
    private Button fetchDataButton;
    private ProgressBar progressBar;
    private LineChart priceChart;
    private TextView percentageChangeTextView;
    private ImageView back, percentageChangeIcon;
    private com.google.android.material.card.MaterialCardView percentageChangeSection;

    private Map<String, List<String>> districtMandiMap = new HashMap<>();
    private Map<String, List<String>> commGroupCommMap = new HashMap<>();

    private static final String API_BASE = "https://agri-marketview-hpfvhzdaa6hrdyeb.canadacentral-01.azurewebsites.net";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market_view);

        districtSpinner = findViewById(R.id.districtSpinner);
        mandiSpinner = findViewById(R.id.mandiSpinner);
        commGroupSpinner = findViewById(R.id.commGroupSpinner);
        cropSpinner = findViewById(R.id.cropSpinner);
        fetchDataButton = findViewById(R.id.fetchDataButton);
        progressBar = findViewById(R.id.progressBar);
        priceChart = findViewById(R.id.priceChart);
        percentageChangeSection = findViewById(R.id.percentageChangeSection);
        percentageChangeTextView = findViewById(R.id.percentageChangeTextView);
        percentageChangeIcon = findViewById(R.id.percentageChangeIcon);

        // Hide the chart initially
        priceChart.setVisibility(View.GONE);

        loadJsonData();
        setupDistrictSpinner();
        setupCommGroupSpinner();

        fetchDataButton.setOnClickListener(v -> {
            String district = districtSpinner.getText().toString();
            String mandi = mandiSpinner.getText().toString();
            String crop = cropSpinner.getText().toString();

            priceChart.clear();
            priceChart.setVisibility(View.GONE);
            new FetchDataTask().execute(district, mandi, crop);
        });

        back = findViewById(R.id.back_btn_market_view);
        back.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_market_view);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                return false;
            } else if (id == R.id.navigation_news) {
                startActivity(new Intent(getApplicationContext(), NewsActivity.class));
                return false;
            } else if (id == R.id.navigation_mandi) {
                startActivity(new Intent(getApplicationContext(), StatewiseMandiActivity.class));
                return false;
            }
            return false;
        });
    }

    private void loadJsonData() {
        try {
            // Load mandi_data.json
            InputStream mandiInputStream = getAssets().open("mandi_data.json");
            byte[] mandiBuffer = new byte[mandiInputStream.available()];
            mandiInputStream.read(mandiBuffer);
            mandiInputStream.close();
            String mandiJson = new String(mandiBuffer, "UTF-8");

            JSONArray mandiArray = new JSONArray(mandiJson);
            for (int i = 0; i < mandiArray.length(); i++) {
                JSONObject mandiObject = mandiArray.getJSONObject(i);
                String districtName = mandiObject.getString("distName");
                String mandiName = mandiObject.getString("mandiName");

                if (!districtMandiMap.containsKey(districtName)) {
                    districtMandiMap.put(districtName, new ArrayList<>());
                }
                districtMandiMap.get(districtName).add(mandiName);
            }

            // Load crop_data.json
            InputStream cropInputStream = getAssets().open("crop_data_full.json");
            byte[] cropBuffer = new byte[cropInputStream.available()];
            cropInputStream.read(cropBuffer);
            cropInputStream.close();
            String cropJson = new String(cropBuffer, "UTF-8");

            JSONArray cropArray = new JSONArray(cropJson);
            for (int i = 0; i < cropArray.length(); i++) {
                JSONObject cropObject = cropArray.getJSONObject(i);
                String commName = cropObject.getString("commName");
                String commGroupName = cropObject.getString("commGroupName");

                if (!commGroupCommMap.containsKey(commGroupName)) {
                    commGroupCommMap.put(commGroupName, new ArrayList<>());
                }
                commGroupCommMap.get(commGroupName).add(commName);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupDistrictSpinner() {
        String[] districtsArray = getResources().getStringArray(R.array.districts);
        List<String> districts = Arrays.asList(districtsArray);

        ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,
                districts);
        districtSpinner.setAdapter(districtAdapter);

        // Set first item as default
        if (!districts.isEmpty()) {
            districtSpinner.setText(districts.get(0), false);
            List<String> mandis = districtMandiMap.get(districts.get(0));
            if (mandis != null && !mandis.isEmpty()) {
                updateMandiSpinner(mandis);
            }
        }

        districtSpinner.setOnItemClickListener((parent, view, position, id) -> {
            String selectedDistrict = parent.getItemAtPosition(position).toString();
            List<String> mandis = districtMandiMap.get(selectedDistrict);
            if (mandis != null && !mandis.isEmpty()) {
                updateMandiSpinner(mandis);
            }
        });
    }

    private void updateMandiSpinner(List<String> mandis) {
        ArrayAdapter<String> mandiAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,
                mandis);
        mandiSpinner.setAdapter(mandiAdapter);

        // Set first item as default
        if (!mandis.isEmpty()) {
            mandiSpinner.setText(mandis.get(0), false);
        }
    }

    private void setupCommGroupSpinner() {
        List<String> commGroups = new ArrayList<>(commGroupCommMap.keySet());
        ArrayAdapter<String> commGroupAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,
                commGroups);
        commGroupSpinner.setAdapter(commGroupAdapter);

        // Set first item as default
        if (!commGroups.isEmpty()) {
            commGroupSpinner.setText(commGroups.get(0), false);
            List<String> commNames = commGroupCommMap.get(commGroups.get(0));
            updateCropSpinner(commNames);
        }

        commGroupSpinner.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCommGroup = parent.getItemAtPosition(position).toString();
            List<String> commNames = commGroupCommMap.get(selectedCommGroup);
            updateCropSpinner(commNames);
        });
    }

    private void updateCropSpinner(List<String> commNames) {
        ArrayAdapter<String> commAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,
                commNames);
        cropSpinner.setAdapter(commAdapter);

        // Set first item as default
        if (commNames != null && !commNames.isEmpty()) {
            cropSpinner.setText(commNames.get(0), false);
        }
    }

    public class DataPoint {
        public Date date;
        public float maxValue;

        public DataPoint(Date date, float maxValue) {
            this.date = date;
            this.maxValue = maxValue;
        }
    }

    private class FetchDataTask extends AsyncTask<String, Void, ArrayList<DataPoint>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            priceChart.setVisibility(View.GONE);
            percentageChangeSection.setVisibility(View.GONE);
        }

        @Override
        protected ArrayList<DataPoint> doInBackground(String... params) {
            String district = params[0];
            String mandi = params[1];
            String crop = params[2];

            ArrayList<DataPoint> dataPoints = new ArrayList<>();
            HttpURLConnection conn = null;

            try {
                String urlStr = API_BASE + "/api/prices"
                        + "?district=" + URLEncoder.encode(district, "UTF-8")
                        + "&mandi=" + URLEncoder.encode(mandi, "UTF-8")
                        + "&crop=" + URLEncoder.encode(crop, "UTF-8");

                URL url = new URL(urlStr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setUseCaches(false);
                conn.setDoInput(true);
                conn.setConnectTimeout(30000); // ⬅ 30 sec
                conn.setReadTimeout(30000); // ⬅ 30 sec
                conn.setRequestProperty("Connection", "keep-alive");

                int responseCode = conn.getResponseCode();
                InputStream is = (responseCode >= 200 && responseCode < 300)
                        ? conn.getInputStream()
                        : conn.getErrorStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONArray jsonArray = new JSONArray(response.toString());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    Date date = sdf.parse(obj.getString("Date"));
                    float maxValue = (float) obj.getDouble("maxValue");
                    dataPoints.add(new DataPoint(date, maxValue));
                }

                Collections.sort(dataPoints, (a, b) -> a.date.compareTo(b.date));

            } catch (Exception e) {
                Log.e("MarketView", "Network error", e);
            } finally {
                if (conn != null)
                    conn.disconnect();
            }

            return dataPoints;
        }

        @Override
        protected void onPostExecute(ArrayList<DataPoint> dataPoints) {
            progressBar.setVisibility(View.GONE);

            if (dataPoints == null || dataPoints.isEmpty()) {
                Toast.makeText(MarketViewActivity.this,
                        "Server is slow or unavailable. Please try again.",
                        Toast.LENGTH_LONG).show();
                priceChart.setVisibility(View.GONE);
                percentageChangeSection.setVisibility(View.GONE);
            } else {
                renderChart(dataPoints);
            }
        }
    }

    private void renderChart(ArrayList<DataPoint> dataPoints) {
        ArrayList<Entry> entries = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
        final List<String> xLabels = new ArrayList<>();

        for (int i = 0; i < dataPoints.size(); i++) {
            DataPoint dp = dataPoints.get(i);
            entries.add(new Entry(i, dp.maxValue));
            xLabels.add(sdf.format(dp.date));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Price Over Time");

        // Line styling
        dataSet.setLineWidth(3f);
        dataSet.setColor(Color.parseColor("#1D9A85"));
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Smooth curves
        dataSet.setCubicIntensity(0.2f);

        // Circle (point) styling
        dataSet.setCircleRadius(6f);
        dataSet.setCircleColor(Color.parseColor("#1D9A85"));
        dataSet.setCircleHoleRadius(3f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleColor(Color.WHITE);

        // Gradient fill under the line
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#1D9A85"));
        dataSet.setFillAlpha(50);

        // Value styling
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.parseColor("#2C3E50"));
        dataSet.setDrawValues(true);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "₹%.0f", value);
            }
        });

        LineData lineData = new LineData(dataSet);
        priceChart.setData(lineData);

        // Chart general settings
        priceChart.getDescription().setEnabled(false);
        priceChart.getLegend().setEnabled(false);
        priceChart.setExtraBottomOffset(15f);
        priceChart.setExtraTopOffset(15f);
        priceChart.setDrawGridBackground(false);
        priceChart.setTouchEnabled(true);
        priceChart.setDragEnabled(true);
        priceChart.setScaleEnabled(false);
        priceChart.setPinchZoom(false);

        // Animate the chart
        priceChart.animateXY(1000, 1000);

        // X-Axis styling
        XAxis xAxis = priceChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setDrawLabels(true);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.parseColor("#7F8C8D"));
        xAxis.setTextSize(10f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value >= 0 && value < xLabels.size()) {
                    return xLabels.get((int) value);
                }
                return "";
            }
        });

        // Y-Axis (Left) styling
        YAxis leftAxis = priceChart.getAxisLeft();
        leftAxis.setGranularity(1f);
        leftAxis.setTextColor(Color.parseColor("#7F8C8D"));
        leftAxis.setTextSize(10f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#ECF0F1"));
        leftAxis.setGridLineWidth(0.5f);

        float maxY = Float.MIN_VALUE;
        float minY = Float.MAX_VALUE;
        for (Entry entry : entries) {
            maxY = Math.max(maxY, entry.getY());
            minY = Math.min(minY, entry.getY());
        }
        leftAxis.setAxisMaximum(maxY + (maxY - minY) * 0.1f);
        leftAxis.setAxisMinimum(minY - (maxY - minY) * 0.1f);

        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "₹%.0f", value);
            }
        });

        // Disable right Y-axis
        priceChart.getAxisRight().setEnabled(false);

        priceChart.setVisibility(View.VISIBLE);
        priceChart.invalidate();

        // Calculate and display percentage change
        if (dataPoints.size() >= 2) {
            float lastPrice = dataPoints.get(dataPoints.size() - 1).maxValue;
            float secondLastPrice = dataPoints.get(dataPoints.size() - 2).maxValue;
            float percentageChange = ((lastPrice - secondLastPrice) / secondLastPrice) * 100;

            percentageChangeTextView.setText(String.format(Locale.getDefault(),
                    "%.2f%%", Math.abs(percentageChange)));
            percentageChangeIcon.setVisibility(View.VISIBLE);
            percentageChangeSection.setVisibility(View.VISIBLE);

            if (percentageChange > 0) {
                percentageChangeIcon.setImageResource(R.drawable.ic_arrow_up);
                percentageChangeTextView.setTextColor(Color.parseColor("#1D9A85"));
                percentageChangeTextView.setText("+" + String.format(Locale.getDefault(),
                        "%.2f%%", percentageChange));
            } else if (percentageChange < 0) {
                percentageChangeIcon.setImageResource(R.drawable.ic_arrow_down);
                percentageChangeTextView.setTextColor(Color.parseColor("#E74C3C"));
                percentageChangeTextView.setText(String.format(Locale.getDefault(),
                        "%.2f%%", percentageChange));
            } else {
                percentageChangeIcon.setVisibility(View.GONE);
                percentageChangeTextView.setTextColor(Color.parseColor("#95A5A6"));
                percentageChangeTextView.setText("0.00%");
            }
        } else {
            percentageChangeSection.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout)
            return logoutUser();
        if (id == R.id.action_profile)
            return settings();
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
        Intent intent = new Intent(getApplicationContext(), ProfilePageActivity.class);
        startActivity(intent);
        return true;
    }
}
