package com.example.agriautomationhub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsPage extends AppCompatActivity {

    ImageView logout, back;
    RelativeLayout languageSelector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("SettingsPage", "onCreate called");

        // Set locale before setContentView
        LocaleHelper.setLocale(this);

        setContentView(R.layout.activity_settings);

        // Initialize UI components
        logout = findViewById(R.id.action_logout);
        back = findViewById(R.id.back_btn);
        languageSelector = findViewById(R.id.language_selector);

        // Set logout functionality
        logout.setOnClickListener(v -> logoutUser());

        // Set back button functionality
        back.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsPage.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Set language selection dialog
        languageSelector.setOnClickListener(v -> showLanguageSelectionDialog());
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showLanguageSelectionDialog() {
        String[] languages = {"English", "Hindi"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Select Language")
                .setItems(languages, (dialog, which) -> {
                    switch (which) {
                        case 0: // English
                            LocaleHelper.setLocale(this, "en");
                            break;
                        case 1: // Hindi
                            LocaleHelper.setLocale(this, "hi");
                            break;
                    }
                    // No need to recreate the activity here
                })
                .show();
    }

}
