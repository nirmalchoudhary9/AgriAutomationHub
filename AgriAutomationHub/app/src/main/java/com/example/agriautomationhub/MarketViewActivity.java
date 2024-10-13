package com.example.agriautomationhub;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MarketViewActivity extends AppCompatActivity {
    private static final String TAG = "MarketViewActivity";

    private Spinner districtSpinner, mandiSpinner, commGroupSpinner, cropSpinner;
    private Button fetchDataButton;
    private LineChart priceChart;

    private Map<String, List<String>> districtMandiMap = new HashMap<>();
    private Map<String, List<String>> commGroupCommMap = new HashMap<>();

    // ODBC Connection String
    String ConnectionURL = "jdbc:jtds:sqlserver://database-agriautomationhub.database.windows.net:1433;DatabaseName=crop_price_dataset;user=AgriAutomationHub@database-agriautomationhub;password=Agri@2024;ssl=require;sslProtocol=TLSv1.2;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";
    // Load the JDBC Driver
    static {
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market_view);

        districtSpinner = findViewById(R.id.districtSpinner);
        mandiSpinner = findViewById(R.id.mandiSpinner);
        commGroupSpinner = findViewById(R.id.commGroupSpinner);
        cropSpinner = findViewById(R.id.cropSpinner);
        fetchDataButton = findViewById(R.id.fetchDataButton);
        priceChart = findViewById(R.id.priceChart);

        // Hide the chart initially
        priceChart.setVisibility(View.GONE);

        loadJsonData();
        setupDistrictSpinner();
        setupCommGroupSpinner();

        fetchDataButton.setOnClickListener(v -> {
            String district = districtSpinner.getSelectedItem().toString();
            String mandi = mandiSpinner.getSelectedItem().toString();
            String crop = cropSpinner.getSelectedItem().toString();

            // Clear the previous chart before fetching new data
            priceChart.clear();
            priceChart.setVisibility(View.GONE); // Hide the chart while new data is fetched
            new FetchDataTask().execute(district, mandi, crop);
        });
    }

    private void loadJsonData() {
        try {
            // Load and parse mandi_data.json
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

            // Load and parse crop_data.json
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
        List<String> districts = new ArrayList<>(districtMandiMap.keySet());
        ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, districts);
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        districtSpinner.setAdapter(districtAdapter);

        districtSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDistrict = districts.get(position);
                List<String> mandis = districtMandiMap.get(selectedDistrict);
                updateMandiSpinner(mandis);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void updateMandiSpinner(List<String> mandis) {
        ArrayAdapter<String> mandiAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mandis);
        mandiAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mandiSpinner.setAdapter(mandiAdapter);
    }

    private void setupCommGroupSpinner() {
        List<String> commGroups = new ArrayList<>(commGroupCommMap.keySet());
        ArrayAdapter<String> commGroupAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, commGroups);
        commGroupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        commGroupSpinner.setAdapter(commGroupAdapter);

        commGroupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCommGroup = commGroups.get(position);
                List<String> commNames = commGroupCommMap.get(selectedCommGroup);
                updateCropSpinner(commNames);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void updateCropSpinner(List<String> commNames) {
        ArrayAdapter<String> commAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, commNames);
        commAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cropSpinner.setAdapter(commAdapter);
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
        protected ArrayList<DataPoint> doInBackground(String... params) {
            String district = params[0];
            String mandi = params[1];
            String crop = params[2];
            ArrayList<DataPoint> dataPoints = new ArrayList<>();  // Collect DataPoints here

            try (Connection conn = DriverManager.getConnection(ConnectionURL)) {
                Log.d(TAG, "Please select a valid date.");
                String query = "SELECT TOP 7 Date, maxValue FROM crop_prices WHERE districtName = ? AND mandiName = ? AND commName = ? ORDER BY Date DESC";
                PreparedStatement statement = conn.prepareStatement(query);
                statement.setString(1, district);
                statement.setString(2, mandi);
                statement.setString(3, crop);
                ResultSet rs = statement.executeQuery();

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                while (rs.next()) {
                    String dateStr = rs.getString("Date");
                    float maxValue = rs.getFloat("maxValue");

                    // Parse date and add to dataPoints
                    Date date = sdf.parse(dateStr);
                    dataPoints.add(new DataPoint(date, maxValue));
                }

                Collections.sort(dataPoints, new Comparator<DataPoint>() {
                    @Override
                    public int compare(DataPoint dp1, DataPoint dp2) {
                        return dp1.date.compareTo(dp2.date);  // Sort by date
                    }
                });
                rs.close();
            } catch (SQLException | ParseException e) {
                e.printStackTrace();
            }
            return dataPoints;  // Return DataPoint list instead of Entry list
        }

        @Override
        protected void onPostExecute(ArrayList<DataPoint> dataPoints) {
            super.onPostExecute(dataPoints);

            if (dataPoints.isEmpty()) {
                Toast.makeText(MarketViewActivity.this, "No data found for the selected options.", Toast.LENGTH_LONG).show();
                priceChart.setVisibility(View.GONE);  // Hide the chart if no data
            } else {
                // Proceed with displaying the chart
                ArrayList<Entry> entries = new ArrayList<>();
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM");  // Format for displaying dates
                final List<String> xLabels = new ArrayList<>();  // List to hold date labels for the X-axis

                for (int i = 0; i < dataPoints.size(); i++) {
                    DataPoint dp = dataPoints.get(i);
                    entries.add(new Entry(i, dp.maxValue));  // Max value for the chart
                    xLabels.add(sdf.format(dp.date));  // Add date label to X-axis
                }

                // Customize the line chart data set for better visualization
                LineDataSet dataSet = new LineDataSet(entries, "Price Over Time");
                dataSet.setLineWidth(3f);  // Line thickness
                dataSet.setCircleRadius(5f);  // Circle size
                dataSet.setCircleColor(Color.parseColor("#1D9A85"));  // Circle color
                dataSet.setColor(Color.parseColor("#1D9A85"));  // Line color
                dataSet.setDrawCircleHole(true);  // Hollow circle
                dataSet.setCircleHoleColor(Color.WHITE);  // Hollow center color
                dataSet.setValueTextSize(10f);  // Value size
                dataSet.setDrawValues(true);  // Show value labels on points

                LineData lineData = new LineData(dataSet);
                priceChart.setData(lineData);

                // Customize the chart appearance
                priceChart.getDescription().setEnabled(false);  // Disable description
                priceChart.getLegend().setEnabled(false);  // Disable legend
                priceChart.setExtraBottomOffset(20f);  // Extra space at the bottom to avoid label clipping

                // Customize the X-axis
                XAxis xAxis = priceChart.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);  // X-axis at the bottom
                xAxis.setGranularity(1f);  // Show 1 value at a time
                xAxis.setLabelRotationAngle(-45);  // Rotate labels for better visibility
                xAxis.setAvoidFirstLastClipping(true);  // Prevent clipping of first/last labels
                xAxis.setDrawLabels(true);  // Ensure labels are drawn

                // Set date labels on the X-axis using ValueFormatter
                xAxis.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        if (value >= 0 && value < xLabels.size()) {
                            return xLabels.get((int) value);  // Return date label
                        } else {
                            return "";  // Return empty string if out of range
                        }
                    }
                });

                // Customize the Y-axis (left side)
                YAxis leftAxis = priceChart.getAxisLeft();
                leftAxis.setGranularity(1f);  // 1-unit step for price
                float maxY = Float.MIN_VALUE;
                for (Entry entry : entries) {
                    maxY = Math.max(maxY, entry.getY());
                }
                leftAxis.setAxisMaximum(maxY + 5);  // Set a buffer above max value
                leftAxis.setDrawGridLines(true);
                leftAxis.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return String.format(Locale.getDefault(), "%.0f", value);  // Format without decimals
                    }
                });

                // Disable the right Y-axis
                priceChart.getAxisRight().setEnabled(false);

                // Show the chart and refresh it
                priceChart.setVisibility(View.VISIBLE);
                priceChart.invalidate();  // Redraw the chart
            }
        }
    }
}