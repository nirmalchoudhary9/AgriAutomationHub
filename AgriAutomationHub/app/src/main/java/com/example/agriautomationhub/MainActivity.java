package com.example.agriautomationhub;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements NetworkChangeReceiver.NetworkChangeListener, OnServiceClickListener {

    private static final String TAG = "MainActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private TextView weatherInfo;
    private TextView weatherLocation;
    private static final String API_KEY = "7e23b9a25a90846111d856e437e11535";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";

    private FusedLocationProviderClient fusedLocationClient;
    private NetworkChangeReceiver networkChangeReceiver;

    private ViewPager2 viewPager2;
    private ServicesAdapter adapter;
    private List<Service> serviceList;

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

        LinearLayout fertilizer = findViewById(R.id.fertilizer_calculator);
        fertilizer.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FertilizerCalculatorActivity.class);
            startActivity(intent);
        });

        LinearLayout selling_price = findViewById(R.id.price_calculator);
        selling_price.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SellingPriceCalculatorActivity.class);
            startActivity(intent);
        });

        initializeServices();
        initializeBottomNavigation();

        // Register network change receiver
        networkChangeReceiver = new NetworkChangeReceiver(this);
        registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Handle the changes if needed
        ViewPager2 viewPager2 = findViewById(R.id.viewPagerServices);
        if (viewPager2 != null) {
            viewPager2.requestLayout(); // Force a layout update
        }
    }

    private void initializeServices() {
        viewPager2 = findViewById(R.id.viewPagerServices);
        serviceList = new ArrayList<>();
        serviceList.add(new Service("Auto Irrigation", R.drawable.auto));
        serviceList.add(new Service("Soil Fertility Check", R.drawable.soil));
        serviceList.add(new Service("Crop Disease Info", R.drawable.crop_disease));
        serviceList.add(new Service("Crop Recommender", R.drawable.crop_recommender));

        adapter = new ServicesAdapter(this, serviceList, this);
        viewPager2.setAdapter(adapter);

        // Setup TabLayout with ViewPager2
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout, viewPager2,
                (tab, position) -> tab.setText(String.valueOf(position + 1))
        ).attach();
    }

    private void initializeBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_main);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                return true;
            } else if (id == R.id.navigation_news) {
                startActivity(new Intent(MainActivity.this, NewsActivity.class));
                return true;
            } else if (id == R.id.navigation_mandi) {
                startActivity(new Intent(MainActivity.this, MandiActivity.class));
                return true;
            }
            return false;
        });
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
                        int humidity = weatherResponse.getMain().getHumidity();
                        String description = weatherResponse.getWeather()[0].getDescription();
                        String location = weatherResponse.getName(); // Get the location

                        // Get current date
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());
                        String currentDate = dateFormat.format(new Date());

                        String weatherText = "Temperature: " + temp + "°C\n" +
                                "Humidity: " + humidity + "%\n" +
                                "Condition: " + description;
                        weatherInfo.setText(weatherText);
                        weatherLocation.setText(location +" , "+ currentDate); // Set the location text
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
            } else {
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

    @Override
    public void onServiceClick(Service service) {
        if (service != null) {
            switch (service.getName()) {
                case "Auto Irrigation":
                    startActivity(new Intent(this, Automatic_Irrigation.class));
                    break;
                case "Soil Fertility Check":
                    startActivity(new Intent(this, Soil_Fertility_check.class));
                    break;
                case "Crop Disease Info":
                    startActivity(new Intent(this, CropCareActivity.class));
                    break;
                case "Crop Recommender":
                    startActivity(new Intent(this, CropRecommenderActivity.class));
                    break;
                default:
                    Log.e(TAG, "Unknown service: " + service.getName());
                    break;
            }
        } else {
            Log.e(TAG, "Service is null");
        }
    }
}
