package com.example.agriautomationhub;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PrivacyTermsActivity extends AppCompatActivity {

    TextView titleText, contentText;
    ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.privacy_terms_activity);

        titleText = findViewById(R.id.titleText);
        contentText = findViewById(R.id.contentText);

        // Retrieve which document to load
        String type = getIntent().getStringExtra("type");

        if (type == null)
            type = "privacy"; // default

        switch (type) {
            case "terms":
                titleText.setText("Terms & Conditions");
                contentText.setText(loadFromAssets("TERMS AND CONDITIONS.txt"));
                break;

            case "support":
                titleText.setText(getString(R.string.support_title));
                contentText.setText(Html.fromHtml(getString(R.string.support_content), Html.FROM_HTML_MODE_LEGACY));
                break;

            case "privacy":
            default:
                titleText.setText("Privacy Policy");
                contentText.setText(loadFromAssets("privacy_policy.txt"));
                break;
        }

        findViewById(R.id.back_btn_legal).setOnClickListener(v -> {
            onBackPressed();
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_legal);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (id == R.id.navigation_news) {
                startActivity(new Intent(this, NewsActivity.class));
                return true;
            } else if (id == R.id.navigation_mandi) {
                startActivity(new Intent(this, StatewiseMandiActivity.class));
                return true;
            } else if (id == R.id.navigation_profile) {
                startActivity(new Intent(this, ProfilePageActivity.class));
                return true;
            }
            return false;
        });
    }

    private String loadFromAssets(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(getAssets().open(fileName)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return "Error loading document.";
        }
        return stringBuilder.toString();
    }
}
