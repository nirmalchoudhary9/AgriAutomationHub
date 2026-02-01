package com.example.agriautomationhub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DeviceLinkActivity extends AppCompatActivity {

    private static final String DEFAULT_USER_ID = "testUser123";
    private static final String DEFAULT_DEVICE_ID = "irrigation001";
    private static final String DEFAULT_REG_TOKEN = "ABC123TOKEN";

    com.google.android.material.textfield.TextInputLayout userIdLayout, deviceIdLayout, tokenLayout;
    EditText userIdInput, deviceIdInput, tokenInput;
    Button linkDeviceBtn;
    android.widget.ImageView backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_link);

        userIdLayout = findViewById(R.id.userIdInputLayout);
        deviceIdLayout = findViewById(R.id.deviceIdInputLayout);
        tokenLayout = findViewById(R.id.tokenInputLayout);

        userIdInput = findViewById(R.id.userIdInput);
        deviceIdInput = findViewById(R.id.deviceIdInput);
        tokenInput = findViewById(R.id.tokenInput);
        linkDeviceBtn = findViewById(R.id.linkDeviceBtn);
        backBtn = findViewById(R.id.back_btn_device_link);

        backBtn.setOnClickListener(v -> finish());
        linkDeviceBtn.setOnClickListener(v -> verifyAndProceed());
    }

    private void verifyAndProceed() {
        String userId = userIdInput.getText().toString().trim();
        String deviceId = deviceIdInput.getText().toString().trim();
        String token = tokenInput.getText().toString().trim();

        // Reset errors
        userIdLayout.setError(null);
        deviceIdLayout.setError(null);
        tokenLayout.setError(null);

        boolean isValid = true;

        if (userId.isEmpty()) {
            userIdLayout.setError("User ID is required");
            isValid = false;
        }

        if (deviceId.isEmpty()) {
            deviceIdLayout.setError("Device ID is required");
            isValid = false;
        }

        if (token.isEmpty()) {
            tokenLayout.setError("Token is required");
            isValid = false;
        }

        if (!isValid)
            return;

        if (userId.equals(DEFAULT_USER_ID)
                && deviceId.equals(DEFAULT_DEVICE_ID)
                && token.equals(DEFAULT_REG_TOKEN)) {

            Toast.makeText(this, "Device linked successfully!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Automatic_Irrigation.class));
            finish();

        } else {
            Toast.makeText(this, "Invalid device credentials", Toast.LENGTH_SHORT).show();
            // Provide specific feedback if possible
            if (!userId.equals(DEFAULT_USER_ID))
                userIdLayout.setError("Invalid User ID");
            if (!deviceId.equals(DEFAULT_DEVICE_ID))
                deviceIdLayout.setError("Invalid Device ID");
            if (!token.equals(DEFAULT_REG_TOKEN))
                tokenLayout.setError("Invalid Token");
        }
    }
}
