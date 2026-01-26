package com.example.agriautomationhub;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FieldMeasureActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 200;

    private GoogleMap mMap;
    private Polygon fieldPolygon;
    private final List<LatLng> polygonPoints = new ArrayList<>();
    private final List<Marker> vertexMarkers = new ArrayList<>();

    private TextView areaText;
    private Spinner unitSpinner;

    private FusedLocationProviderClient fusedLocationClient;
    private LatLng currentLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_field_measure);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Field Measure");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        areaText = findViewById(R.id.area_text);
        unitSpinner = findViewById(R.id.unit_spinner);

        // Spinner setup
        String[] units = {"in²", "ft²", "yd²", "acres", "hectares"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, units);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(adapter);

        // ✅ Recalculate area automatically when unit is changed
        unitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                // Recalculate area if polygon already exists
                if (polygonPoints.size() >= 3) {
                    calculateArea();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Buttons
        findViewById(R.id.btn_undo).setOnClickListener(v -> undoLastPoint());
        findViewById(R.id.btn_clear).setOnClickListener(v -> clearPolygon());
        findViewById(R.id.btn_my_location).setOnClickListener(v -> getUserLocation());
        findViewById(R.id.btn_zoom_in).setOnClickListener(v -> mMap.animateCamera(CameraUpdateFactory.zoomIn()));
        findViewById(R.id.btn_zoom_out).setOnClickListener(v -> mMap.animateCamera(CameraUpdateFactory.zoomOut()));

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "Your API Key");
        }

//        setupBottomNavigation();
    }

//    private void setupBottomNavigation() {
//        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_field);
//
//        bottomNavigationView.setOnItemSelectedListener(item -> {
//            int id = item.getItemId();
//            if (id == R.id.navigation_home) {
//                startActivity(new Intent(FieldMeasureActivity.this, MainActivity.class));
//                return true;
//            } else if (id == R.id.navigation_profile) {
//                startActivity(new Intent(FieldMeasureActivity.this, ProfilePageActivity.class));
//                return true;
//            } else if (id == R.id.navigation_mandi) {
//                startActivity(new Intent(FieldMeasureActivity.this, StatewiseMandiActivity.class));
//                return true;
//            } else if (id == R.id.navigation_news) {
//                startActivity(new Intent(FieldMeasureActivity.this, NewsActivity.class));
//                return true;
//            }
//            return false;
//        });
//    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        getUserLocation();

        mMap.setOnMapClickListener(this::addVertex);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(@NonNull Marker marker) {}

            @Override
            public void onMarkerDrag(@NonNull Marker marker) {
                updatePolygon();
            }

            @Override
            public void onMarkerDragEnd(@NonNull Marker marker) {
                updatePolygon();
            }
        });
    }

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 17));
                mMap.setMyLocationEnabled(true);
            }
        });
    }

    private void addVertex(LatLng point) {
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(point)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .draggable(true));
        vertexMarkers.add(marker);
        polygonPoints.add(point);
        updatePolygon();
    }

    private void updatePolygon() {
        polygonPoints.clear();
        for (Marker m : vertexMarkers) {
            polygonPoints.add(m.getPosition());
        }

        if (fieldPolygon != null) {
            fieldPolygon.remove();
        }

        if (polygonPoints.size() >= 3) {
            fieldPolygon = mMap.addPolygon(new PolygonOptions()
                    .addAll(polygonPoints)
                    .strokeWidth(4)
                    .strokeColor(0xFF00AA00)
                    .fillColor(0x5500FF00));
            calculateArea();
        } else {
            areaText.setText("Add at least 3 points to form a polygon");
        }
    }

    @SuppressLint("DefaultLocale")
    private void calculateArea() {
        double areaMeters = SphericalUtil.computeArea(polygonPoints);
        String selectedUnit = unitSpinner.getSelectedItem().toString();
        String areaResult;

        switch (selectedUnit) {
            case "in²":
                areaResult = String.format("%.2f in²", areaMeters * 1550.0031);
                break;
            case "ft²":
                areaResult = String.format("%.2f ft²", areaMeters * 10.7639);
                break;
            case "yd²":
                areaResult = String.format("%.2f yd²", areaMeters * 1.19599);
                break;
            case "acres":
                areaResult = String.format("%.4f acres", areaMeters * 0.000247105);
                break;
            case "hectares":
                areaResult = String.format("%.4f ha", areaMeters * 0.0001);
                break;
            default:
                areaResult = String.format("%.2f m²", areaMeters);
        }

        areaText.setText(String.format("Area: %s", areaResult));
    }

    private void undoLastPoint() {
        if (!vertexMarkers.isEmpty()) {
            Marker last = vertexMarkers.remove(vertexMarkers.size() - 1);
            last.remove();
            updatePolygon();
        }
    }

    private void clearPolygon() {
        for (Marker m : vertexMarkers) {
            m.remove();
        }
        vertexMarkers.clear();
        polygonPoints.clear();
        if (fieldPolygon != null) {
            fieldPolygon.remove();
            fieldPolygon = null;
        }
        areaText.setText("Area: 0");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        searchItem.setOnMenuItemClickListener(v -> {
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                    .build(FieldMeasureActivity.this);
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
            return true;
        });

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                LatLng latLng = place.getLatLng();
                if (latLng != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                    mMap.addMarker(new MarkerOptions().position(latLng).title(place.getAddress()));
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Toast.makeText(this, "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
            }
        }
    }
}



