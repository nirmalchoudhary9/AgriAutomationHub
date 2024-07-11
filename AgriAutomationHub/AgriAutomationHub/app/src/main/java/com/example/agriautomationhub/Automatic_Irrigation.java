package com.example.agriautomationhub;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Automatic_Irrigation extends AppCompatActivity {

    TextView humidityTextView, temperatureTextView;
    Button modeToggleButton;
    TextView status;
    ImageView back;
    DatabaseReference dbRef;
    LinearLayout valveButtonsLayout;
    boolean isManualMode = false; // To track the current mode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_automatic_irrigation);

        // Initialize Firebase database reference
        dbRef = FirebaseDatabase.getInstance().getReference();

        back = findViewById(R.id.back_btn);

        back.setOnClickListener(v -> {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
        });


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.navigation_home) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    return true;
                }else if (id == R.id.navigation_news) {
                    // Handle News navigation
                    startActivity(new Intent(getApplicationContext(), NewsActivity.class));
                    return true;
                } else if (id == R.id.navigation_mandi) {
                    openWebsite();  // Replace with your URL
                    return true;
                }
                return false;
        });

        humidityTextView = findViewById(R.id.humidityValue);
        temperatureTextView = findViewById(R.id.temperatureValue);
        modeToggleButton = findViewById(R.id.modeToggleButton);
        valveButtonsLayout = findViewById(R.id.valveButtonsLayout);
        status = findViewById(R.id.statusTextView);

        // Listen for changes in Firebase database and update UI accordingly
        dbRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Long humidity = dataSnapshot.child("HumidityMeter").child("humidity").getValue(Long.class);
                    Long temperature = dataSnapshot.child("TemperatureMeter").child("temperature").getValue(Long.class);

                    if (humidity != null && temperature != null) {
                        humidityTextView.setText(""+humidity);
                        temperatureTextView.setText(""+temperature);
                    } else {
                        Toast.makeText(Automatic_Irrigation.this, "Error: Unexpected data format", Toast.LENGTH_SHORT).show();
                    }

                    status.setText("Status: ");
                    DataSnapshot modeSnapshot = dataSnapshot.child("Watering").child("Mode");
                    if (modeSnapshot.exists()) {
                        Long mode = modeSnapshot.getValue(Long.class);
                        if (mode != null && mode == 1) {
                            isManualMode = true;
                            modeToggleButton.setText("Automatic Mode");
                            status.append("Manual");
                            enableValveButtons();
                        } else {
                            isManualMode = false;
                            modeToggleButton.setText("Manual Mode");
                            status.append("Automatic");
                            disableValveButtons();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Automatic_Irrigation.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        populateValveButtons();
    }

    private void openWebsite() {
        String url = "https://eanugya.mp.gov.in/Inward_Quote.aspx";
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    @SuppressLint("SetTextI18n")
    private void populateValveButtons() {
        dbRef.child("MoistureMeter").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                valveButtonsLayout.removeAllViews();
                for (DataSnapshot sensorSnapshot : dataSnapshot.getChildren()) {
                    String sensorKey = sensorSnapshot.getKey();
                    Long moistureValue = sensorSnapshot.getValue(Long.class);
                    if (moistureValue != null && sensorKey != null) {
                        int valveNumber = Integer.parseInt(sensorKey.substring(6));
                        addValveSwitch(valveNumber, moistureValue);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Automatic_Irrigation.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addValveSwitch(int valveNumber, long moistureValue) {
        LinearLayout valveLayout = new LinearLayout(Automatic_Irrigation.this);
        valveLayout.setOrientation(LinearLayout.HORIZONTAL);
        valveLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView statusTextView = new TextView(Automatic_Irrigation.this);
        statusTextView.setText("Valve " + valveNumber + " Moisture value: " + moistureValue + "%");
        statusTextView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        statusTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        statusTextView.setTextColor(getResources().getColor(R.color.white));
        statusTextView.setPadding(dpToPx(), dpToPx(), dpToPx(), dpToPx());
        valveLayout.addView(statusTextView);

        Switch valveSwitch = new Switch(Automatic_Irrigation.this);
        valveSwitch.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        valveSwitch.setChecked(false);

        valveSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateSolenoidValveStatus(valveNumber, isChecked ? 1 : 0);
            }
        });
        valveLayout.addView(valveSwitch);

        valveButtonsLayout.addView(valveLayout);

        dbRef.child("Watering").child("Valve" + valveNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer valveStatus = dataSnapshot.getValue(Integer.class);
                if (valveStatus != null) {
                    valveSwitch.setChecked(valveStatus == 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled
            }
        });
    }

    private int dpToPx() {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) 8 * density);
    }

    private void enableValveButtons() {
        for (int i = 0; i < valveButtonsLayout.getChildCount(); i++) {
            View child = valveButtonsLayout.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout valveLayout = (LinearLayout) child;
                for (int j = 0; j < valveLayout.getChildCount(); j++) {
                    View button = valveLayout.getChildAt(j);
                    if (button instanceof Switch) {
                        button.setEnabled(true);
                    }
                }
            }
        }
    }

    private void disableValveButtons() {
        for (int i = 0; i < valveButtonsLayout.getChildCount(); i++) {
            View child = valveButtonsLayout.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout valveLayout = (LinearLayout) child;
                for (int j = 0; j < valveLayout.getChildCount(); j++) {
                    View button = valveLayout.getChildAt(j);
                    if (button instanceof Switch) {
                        button.setEnabled(false);
                    }
                }
            }
        }
    }

    public void toggleMode(View view) {
        if (isManualMode) {
            switchToAutomaticMode();
        } else {
            switchToManualMode();
        }
    }

    private void switchToAutomaticMode() {
        dbRef.child("Watering").child("Mode").setValue(0);
        modeToggleButton.setText("Manual Mode");
        status.setText("Status: Automatic");
        disableValveButtons();
        isManualMode = false;
    }

    private void switchToManualMode() {
        dbRef.child("Watering").child("Mode").setValue(1);
        modeToggleButton.setText("Automatic Mode");
        status.setText("Status: Manual");
        enableValveButtons();
        isManualMode = true;
    }

    private void updateSolenoidValveStatus(int valveNumber, int status) {
        dbRef.child("Watering").child("Valve" + valveNumber).setValue(status);
    }
}
