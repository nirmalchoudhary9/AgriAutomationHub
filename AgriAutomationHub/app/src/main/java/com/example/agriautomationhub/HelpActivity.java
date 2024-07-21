package com.example.agriautomationhub;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HelpActivity extends AppCompatActivity {

    ImageView back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_help);

        back = findViewById(R.id.back_btn);

        back.setOnClickListener(v -> {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

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
                    startActivity(new Intent(HelpActivity.this, MandiActivity.class));
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