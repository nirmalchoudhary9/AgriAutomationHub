
package com.example.agriautomationhub;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.example.agriautomationhub.utils.PrefsManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private TextView weatherInfo;
    private TextView weatherLocation;
    private android.widget.ImageView weatherIcon;

    private TextView navUserName, navUserPhone, navUserEmail;
    private TextView greetingText, dashboardUserName;
    private CircleImageView navUserImage;

    // private static final String API_KEY = "7e23b9a25a90846111d856e437e11535";
    private static final String API_KEY = BuildConfig.OPENWEATHER_API_KEY;
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";

    private FusedLocationProviderClient fusedLocationClient;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private PrefsManager prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences preferences = getSharedPreferences("Settings", MODE_PRIVATE);
        String language = preferences.getString("My_Lang", "");
        setLocale(language);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase init
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        prefs = new PrefsManager(this);
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // if user not logged in → go to login
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // Drawer & Toolbar setup
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Fetch navigation header views
        View headerView = navigationView.getHeaderView(0);
        navUserName = headerView.findViewById(R.id.nav_user_name);
        navUserPhone = headerView.findViewById(R.id.nav_user_phone);
        navUserEmail = headerView.findViewById(R.id.nav_user_email);
        navUserImage = headerView.findViewById(R.id.nav_user_image);

        // 🎨 Specialized Styling for Logout
        android.view.MenuItem logoutItem = navigationView.getMenu().findItem(R.id.nav_logout);
        if (logoutItem != null) {
            android.text.SpannableString s = new android.text.SpannableString(logoutItem.getTitle());
            s.setSpan(new android.text.style.ForegroundColorSpan(android.graphics.Color.parseColor("#C62828")), 0,
                    s.length(), 0);
            logoutItem.setTitle(s);
            if (logoutItem.getIcon() != null) {
                logoutItem.getIcon().setTint(android.graphics.Color.parseColor("#C62828"));
            }
        }

        if (currentUser != null) {
            String uid = currentUser.getUid();

            // 1️⃣ Fetch once after login (saves in SharedPreferences)
            fetchUserData(uid);

            // 2️⃣ Load instantly from SharedPreferences
            loadUserProfile(uid);

            // 3️⃣ Keep listening for updates (realtime sync)
            listenForUserUpdates(uid);
        }

        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                super.onDrawerClosed(drawerView);
                navigationView.getMenu().setGroupCheckable(0, true, false);
                for (int i = 0; i < navigationView.getMenu().size(); i++) {
                    navigationView.getMenu().getItem(i).setChecked(false);
                }
                navigationView.getMenu().setGroupCheckable(0, true, true);
            }
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfilePageActivity.class));
            } else if (id == R.id.nav_language) {
                showLanguageSelectionDialog();
            } else if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                prefs.clearUser();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else if (id == R.id.nav_privacy) {
                Intent intent = new Intent(this, PrivacyTermsActivity.class);
                intent.putExtra("type", "privacy");
                startActivity(intent);
            } else if (id == R.id.nav_terms) {
                Intent intent = new Intent(this, PrivacyTermsActivity.class);
                intent.putExtra("type", "terms");
                startActivity(intent);
            } else if (id == R.id.nav_help) {
                startActivity(new Intent(this, HelpActivity.class));
            }

            drawerLayout.closeDrawer(GravityCompat.START);

            // 🔑 clear highlight
            for (int i = 0; i < navigationView.getMenu().size(); i++) {
                navigationView.getMenu().getItem(i).setChecked(false);
            }

            return true;
        });

        weatherInfo = findViewById(R.id.weather_info);
        weatherLocation = findViewById(R.id.weather_location);
        weatherIcon = findViewById(R.id.weather_icon_main);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Location Permission
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION },
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLastLocation();
        }

        // FAB Chatbot
        FloatingActionButton fabChatBot = findViewById(R.id.fabChatBot);
        fabChatBot.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, ChatActivity.class)));

        greetingText = findViewById(R.id.greeting_text);
        dashboardUserName = findViewById(R.id.dashboard_user_name);

        initializeServices();
        initializeBottomNavigation();
    }

    private void fetchUserData(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String phone = documentSnapshot.getString("phone");
                        String email = documentSnapshot.getString("email");
                        String imageUrl = documentSnapshot.getString("photoUrl");

                        prefs.saveUser(name, phone, email, imageUrl);

                        // Update NavDrawer immediately
                        loadUserProfile(uid);
                        // Debug Toast
                        // Toast.makeText(MainActivity.this, "Profile Loaded: " + name,
                        // Toast.LENGTH_SHORT).show();
                    } else {
                        // Document missing
                        Toast.makeText(MainActivity.this, "Profile missing in DB for: " + uid, Toast.LENGTH_LONG)
                                .show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Failed to fetch profile: " + e.getMessage(), Toast.LENGTH_LONG)
                            .show();
                });
    }

    private void loadUserProfile(String uid) {
        String name = prefs.getName();
        navUserName.setText(name);
        navUserPhone.setText(prefs.getPhone());
        navUserEmail.setText(prefs.getEmail());

        if (dashboardUserName != null && name != null && !name.isEmpty()) {
            dashboardUserName.setText(name);
        }

        Glide.with(this)
                .load(prefs.getImageUrl())
                .placeholder(R.drawable.ic_person_placeholder) // fallback image
                .into(navUserImage);
    }

    private void listenForUserUpdates(String uid) {
        db.collection("users").document(uid)
                .addSnapshotListener((snapshot, e) -> {
                    if (snapshot != null && snapshot.exists()) {
                        String name = snapshot.getString("name");
                        String phone = snapshot.getString("phone");
                        String email = snapshot.getString("email");
                        String imageUrl = snapshot.getString("photoUrl");

                        prefs.saveUser(name, phone, email, imageUrl);

                        // Update NavDrawer UI instantly
                        loadUserProfile(uid);
                    }
                });
    }

    private void showLanguageSelectionDialog() {
        String[] languages = { "English", "Hindi" };
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Select Language")
                .setItems(languages, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            LocaleHelper.setLocale(this, "en");
                            break;
                        case 1:
                            LocaleHelper.setLocale(this, "hi");
                            break;
                    }
                })
                .show();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START); // close drawer first
        } else {
            finishAffinity(); // closes all activities
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_main);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    private void initializeServices() {
        GridLayout gridLayout = findViewById(R.id.grid_smart_support);
        int childCount = gridLayout.getChildCount();

        for (int i = 0; i < childCount; i++) {
            View serviceView = gridLayout.getChildAt(i);
            final int index = i;
            serviceView.setOnClickListener(v -> {
                switch (index) {
                    case 0:
                        startActivity(new Intent(MainActivity.this, DeviceLinkActivity.class));
                        break;
                    case 1:
                        startActivity(new Intent(MainActivity.this, CropCareActivity.class));
                        break;
                    case 2:
                        startActivity(new Intent(MainActivity.this, CropRecommenderActivity.class));
                        break;
                    case 3:
                        startActivity(new Intent(MainActivity.this, SellingPriceCalculatorActivity.class));
                        break;
                    case 4:
                        startActivity(new Intent(MainActivity.this, NewsActivity.class));
                        break;
                    case 5:
                        startActivity(new Intent(MainActivity.this, StatewiseMandiActivity.class));
                        break;
                    case 6:
                        startActivity(new Intent(MainActivity.this, FieldMeasureActivity.class));
                        break;
                    case 7:
                        startActivity(new Intent(MainActivity.this, MarketViewActivity.class));
                        break;
                }
            });
        }
    }

    private void initializeBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_main);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_news) {
                startActivity(new Intent(MainActivity.this, NewsActivity.class));
                return false;
            } else if (id == R.id.navigation_profile) {
                startActivity(new Intent(MainActivity.this, ProfilePageActivity.class));
                return false;
            } else if (id == R.id.navigation_mandi) {
                startActivity(new Intent(MainActivity.this, StatewiseMandiActivity.class));
                return false;
            }
            return true;
        });
    }

    @SuppressLint("SetTextI18n")
    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION },
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        getWeatherData(location.getLatitude(), location.getLongitude());
                    } else {
                        weatherInfo.setText(R.string.unable_to_get_location);
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

        call.enqueue(new Callback<WeatherResponse>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<WeatherResponse> call,
                    @NonNull Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse weatherResponse = response.body();
                    double temp = weatherResponse.getMain().getTemp();
                    int humidity = weatherResponse.getMain().getHumidity();
                    String description = weatherResponse.getWeather()[0].getDescription();
                    String location = weatherResponse.getName();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
                    String currentDate = dateFormat.format(new Date());

                    // Dynamic Icon Update
                    int conditionId = weatherResponse.getWeather()[0].getId();
                    updateWeatherIcon(conditionId);

                    weatherInfo.setText(getString(R.string.weather_format, String.valueOf(temp),
                            String.valueOf(humidity), description));
                    weatherLocation.setText(getString(R.string.weather_location_format, location, currentDate));
                } else {
                    weatherInfo.setText(R.string.weather_fetch_failed);
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFailure(@NonNull Call<WeatherResponse> call, @NonNull Throwable t) {
                weatherInfo.setText(R.string.weather_fetch_failed);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                weatherInfo.setText(R.string.location_permission_denied);
            }
        }
    }

    private void updateWeatherIcon(int conditionId) {
        if (weatherIcon == null)
            return;

        int iconRes;
        if (conditionId >= 200 && conditionId <= 232) {
            iconRes = R.drawable.ic_storm;
        } else if (conditionId >= 300 && conditionId <= 321) {
            iconRes = R.drawable.ic_rain; // Drizzle
        } else if (conditionId >= 500 && conditionId <= 531) {
            iconRes = R.drawable.ic_rain;
        } else if (conditionId >= 600 && conditionId <= 622) {
            iconRes = R.drawable.ic_snow;
        } else if (conditionId >= 701 && conditionId <= 781) {
            iconRes = R.drawable.ic_mist; // Atmosphere (Mist, Smoke, etc.)
        } else if (conditionId == 800) {
            iconRes = R.drawable.ic_sun;
        } else if (conditionId >= 801 && conditionId <= 804) {
            iconRes = R.drawable.ic_cloudy;
        } else {
            iconRes = R.drawable.ic_sun; // Default
        }
        weatherIcon.setImageResource(iconRes);
    }
}
