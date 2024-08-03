package com.example.agriautomationhub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsPage extends AppCompatActivity {

    ImageView logout, back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        logout = findViewById(R.id.action_logout);
        back = findViewById(R.id.back_btn);

        logout.setOnClickListener(v -> logoutUser());

        back.setOnClickListener(v -> {
                Intent intent = new Intent(SettingsPage.this, MainActivity.class);
                startActivity(intent);
                finish();
        });
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        // Redirect to login screen or any other desired activity
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }
}