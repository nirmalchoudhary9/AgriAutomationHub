package com.example.agriautomationhub;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

public class MainActivity extends AppCompatActivity implements OnServiceClickListener {

    private static final String TAG = "MainActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private TextView weatherInfo;
    private TextView weatherLocation;
    private static final String API_KEY = "";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";

    private FusedLocationProviderClient fusedLocationClient;

    /** @noinspection deprecation*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences preferences = getSharedPreferences("Settings", MODE_PRIVATE);
        String language = preferences.getString("My_Lang", "");
        setLocale(language);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start the background internet service
        Intent serviceIntent = new Intent(this, BackgroundInternetService.class);
        startService(serviceIntent);

        // Initialize Firebase
        FirebaseAuth.getInstance();

        weatherInfo = findViewById(R.id.weather_info);
        weatherLocation = findViewById(R.id.weather_location);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLastLocation();
        }

        FloatingActionButton fabChatBot = findViewById(R.id.fabChatBot);
        fabChatBot.setOnClickListener(view -> {
            // Open the ChatActivity
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            startActivity(intent);
        });

        LinearLayout fertilizer = findViewById(R.id.fertilizer_calculator);
        fertilizer.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ExpenseCalculatorActivity.class);
            startActivity(intent);
        });

        LinearLayout schemes = findViewById(R.id.gov_schemes);
        schemes.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SchemesActivity.class);
            startActivity(intent);
        });


        initializeServices();
        initializeBottomNavigation();

    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
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
        ViewPager2 viewPager2 = findViewById(R.id.viewPagerServices);
        List<Service> serviceList = new ArrayList<>();
        serviceList.add(new Service(R.string.auto_irrigation, R.drawable.auto));
        serviceList.add(new Service(R.string.crop_care, R.drawable.crop_disease));
        serviceList.add(new Service(R.string.crop_recommendation, R.drawable.crop_recommender));
        serviceList.add(new Service(R.string.selling_price_calculator, R.drawable.selling_price));

        // Set the adapter for ViewPager2
        ServicesAdapter adapter = new ServicesAdapter(this, serviceList, this);
        viewPager2.setAdapter(adapter);

        // Setup TabLayout with ViewPager2 using TabLayoutMediator
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout, viewPager2,
                (tab, position) -> {
                    // Set tab text directly as "Tab X", where X is the tab index
                    tab.setText(String.valueOf(position + 1));
                }
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
            } else if (id == R.id.navigation_marketView) {
                startActivity(new Intent(MainActivity.this, MarketViewActivity.class));
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
        // Stop the background service
        stopService(new Intent(this, BackgroundInternetService.class));
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

    @SuppressLint("SetTextI18n")
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
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<WeatherResponse> call, @NonNull Response<WeatherResponse> response) {
                Log.d(TAG, "Response code: " + response.code());  // Log the response code
                if (response.isSuccessful()) {
                    WeatherResponse weatherResponse = response.body();
                    if (weatherResponse != null) {
                        double temp = weatherResponse.getMain().getTemp();
                        int humidity = weatherResponse.getMain().getHumidity();
                        String description = weatherResponse.getWeather()[0].getDescription();
                        String location = weatherResponse.getName(); // Get the location

                        // Get current date
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
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
                        assert response.errorBody() != null;
                        Log.e(TAG, "Response not successful: " + response.errorBody().string());
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFailure(@NonNull Call<WeatherResponse> call, @NonNull Throwable t) {
                weatherInfo.setText("Failed to get weather data");
                Log.e(TAG, "API call failed: ", t);
            }
        });
    }



    @SuppressLint("SetTextI18n")
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
    public void onServiceClick(Service service) {
        if (service != null) {
            int serviceNameResId = service.getName(); // Get the resource ID directly

            if (serviceNameResId == R.string.auto_irrigation) {
                startActivity(new Intent(this, Automatic_Irrigation.class));
            } else if (serviceNameResId == R.string.crop_care) {
                startActivity(new Intent(this, CropCareActivity.class));
            } else if (serviceNameResId == R.string.crop_recommendation) {
                startActivity(new Intent(this, CropRecommenderActivity.class));
            } else if (serviceNameResId == R.string.selling_price_calculator) {
                startActivity(new Intent(MainActivity.this, SellingPriceCalculatorActivity.class));
            } else {
                Log.e(TAG, "Unknown service: " + getString(serviceNameResId));
            }
        } else {
            Log.e(TAG, "Service is null");
        }
    }

}
