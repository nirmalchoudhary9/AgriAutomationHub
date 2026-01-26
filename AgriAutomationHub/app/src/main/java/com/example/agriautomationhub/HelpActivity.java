package com.example.agriautomationhub;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HelpActivity extends AppCompatActivity {

    ImageView back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        back = findViewById(R.id.back_btn);

        back.setOnClickListener(v -> {
                onBackPressed();
        });

        findViewById(R.id.submit_button).setOnClickListener(v -> {
            android.widget.Toast.makeText(this, "Support request submitted successfully!", android.widget.Toast.LENGTH_SHORT).show();
            // Clear fields after submission
            ((android.widget.EditText)findViewById(R.id.name_input)).setText("");
            ((android.widget.EditText)findViewById(R.id.email_input)).setText("");
            ((android.widget.EditText)findViewById(R.id.message_input)).setText("");
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_help);

        bottomNavigationView.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if(id == R.id.navigation_home)
                {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }else if (id == R.id.navigation_news) {
                    // Handle News navigation
                    startActivity(new Intent(getApplicationContext(), NewsActivity.class));
                    return true;
                } else if (id == R.id.navigation_mandi) {
                    startActivity(new Intent(HelpActivity.this, StatewiseMandiActivity.class));
                    return true;
                } else if (id == R.id.navigation_profile) {
                    startActivity(new Intent(HelpActivity.this, ProfilePageActivity.class));
                    return true;
                }
                return false;
        });
    }

    private void openWebsite() {
        String url = "https://eanugya.mp.gov.in/Inward_Quote.aspx";
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

}