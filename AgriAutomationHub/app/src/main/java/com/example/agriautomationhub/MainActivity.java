package com.example.agriautomationhub;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.ui.AppBarConfiguration;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements NetworkChangeReceiver.NetworkChangeListener {

    private static final String TAG = "MainActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private AppBarConfiguration mAppBarConfiguration;

    private HorizontalScrollView horizontalScrollView;
    private ImageView scrollIndicator;

    private TextView weatherInfo;
    private TextView weatherLocation;
    private static final String API_KEY = "7e23b9a25a90846111d856e437e11535";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";

    private FusedLocationProviderClient fusedLocationClient;
    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        FirebaseAuth auth = FirebaseAuth.getInstance();

        weatherInfo = findViewById(R.id.weather_info);
        weatherLocation = findViewById(R.id.weather_location);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLastLocation();
        }

        LinearLayout autoIrrigationLayout = findViewById(R.id.auto_irrigation_layout);
        autoIrrigationLayout.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Automatic_Irrigation.class)));

        LinearLayout cropDiseaseLayout = findViewById(R.id.crop_disease_layout);
        cropDiseaseLayout.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CropCareActivity.class)));

        LinearLayout soilFertility = findViewById(R.id.soil_fertility_layout);
        soilFertility.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Soil_Fertility_check.class)));

        LinearLayout cropRecommender = findViewById(R.id.crop_recommendation_layout);
        cropRecommender.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CropRecommenderActivity.class)));

        LinearLayout fertilizerCal = findViewById(R.id.fertilizer_calculator_layout);
        fertilizerCal.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, FertilizerCalculatorActivity.class)));

        horizontalScrollView = findViewById(R.id.horizontal_scroll_view);
        scrollIndicator = findViewById(R.id.scroll_indicator);

        // Ensure child view is of type GridLayout (or the specific type of the child view)
        GridLayout gridLayout = (GridLayout) horizontalScrollView.getChildAt(0);

        // Add scroll listener
        horizontalScrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollX = horizontalScrollView.getScrollX();
                int maxScrollX = gridLayout.getWidth() - horizontalScrollView.getWidth();

                // Hide the indicator if at the end of the scroll, show otherwise
                if (scrollX >= maxScrollX) {
                    scrollIndicator.setVisibility(View.GONE);
                } else {
                    scrollIndicator.setVisibility(View.VISIBLE);
                }
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_main);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                return true;
            } else if (id == R.id.navigation_news) {
                // Handle News navigation
                startActivity(new Intent(MainActivity.this, NewsActivity.class));
                return true;
            } else if (id == R.id.navigation_mandi) {
                startActivity(new Intent(MainActivity.this, MandiActivity.class));
                return true;
            }
            return false;
        });

        // Register network change receiver
        networkChangeReceiver = new NetworkChangeReceiver(this);
        registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister network change receiver
        unregisterReceiver(networkChangeReceiver);
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


    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        getWeatherData(latitude, longitude);
                    } else {
                        weatherInfo.setText("Unable to get location.");
                    }
                });
    }

    private void getWeatherData(double latitude, double longitude) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherApiService apiService = retrofit.create(WeatherApiService.class);

        Call<WeatherResponse> call = apiService.getCurrentWeather(latitude, longitude, API_KEY, "metric");

        Log.d(TAG, "Request URL: " + call.request().url());  // Log the request URL

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                Log.d(TAG, "Response code: " + response.code());  // Log the response code
                if (response.isSuccessful()) {
                    WeatherResponse weatherResponse = response.body();
                    if (weatherResponse != null) {
                        double temp = weatherResponse.getMain().getTemp();
                        String description = weatherResponse.getWeather()[0].getDescription();
                        String location = weatherResponse.getName(); // Get the location

                        String weatherText = "Temperature: " + temp + "°C\n" +
                                "Condition: " + description;
                        weatherInfo.setText(weatherText);
                        weatherLocation.setText(location); // Set the location text
                        Log.d(TAG, "Weather data retrieved: " + weatherText);
                    } else {
                        weatherInfo.setText("No weather data available");
                        Log.e(TAG, "Weather response is null");
                    }
                } else {
                    weatherInfo.setText("Failed to get weather data");
                    try {
                        Log.e(TAG, "Response not successful: " + response.errorBody().string());
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                weatherInfo.setText("Failed to get weather data");
                Log.e(TAG, "API call failed: ", t);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
            else {
                weatherInfo.setText("Location permission denied");
            }
        }
    }

    @Override
    public void onNetworkChange(boolean isConnected) {
        if (isConnected) {
            getLastLocation();
        } else {
            weatherInfo.setText("No internet connection.");
        }
    }
}
