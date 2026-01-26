package com.example.agriautomationhub;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.text.LineBreaker;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.agriautomationhub.network.DemoRetrofitClient;
import com.example.agriautomationhub.network.model.DemoResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CropCareActivity extends AppCompatActivity {

    private static final String TAG = "CropCareActivity";

    private ImageView imgView;
    private TextView tv, cureTextView;
    private Bitmap img;
    private static final int CAMERA_PERMISSION_CODE = 100;
//    private static final String API_KEY = "2IaEuoCn64HkUzPqKynfy3DAxpl4h5qybxLZzXJUPdU1soiQ7q";
    private static final String API_KEY = BuildConfig.CROPCARE_API_KEY;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_care);

        imgView = findViewById(R.id.imageView);
        tv = findViewById(R.id.textView);
        cureTextView = findViewById(R.id.cureTextView);
        
        tv.setText("Capture or select a crop photo to begin scan.");
        cureTextView.setText("");
        Button select = findViewById(R.id.btn1);
        Button predict = findViewById(R.id.btn2);
        Button captureButton = findViewById(R.id.btnCapture);
// Removed redundant captureImg reference

        ImageView back = findViewById(R.id.back_btn_crop_care);
        back.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        });

        ActivityResultLauncher<Intent> selectImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        imgView.setImageURI(uri);
                        try {
                            img = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                        } catch (IOException e) {
                            Log.e(TAG, "Error loading image from gallery", e);
                        }
                    }
                });

        select.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("image/*");
            selectImageLauncher.launch(intent);
        });

        ActivityResultLauncher<Intent> captureImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        if (imageBitmap != null) {
                            imgView.setImageBitmap(imageBitmap);
                            img = imageBitmap;
                        }
                    }
                });

        captureButton.setOnClickListener(v -> dispatchTakePictureIntent(captureImageLauncher));
// Redundant listener removed

        predict.setOnClickListener(v -> {
            if (img == null) {
                Toast.makeText(this, "Please select or capture an image first", Toast.LENGTH_SHORT).show();
                return;
            }
            
            tv.setText("Analyzing plant health...");
            cureTextView.setText("Searching for treatment protocols...");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            img.compress(Bitmap.CompressFormat.JPEG, 90, baos);
            String base64Image = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);

            JsonObject jsonObject = new JsonObject();
            JsonArray imageArray = new JsonArray();
            imageArray.add(base64Image);

            jsonObject.add("images", imageArray);
//            jsonObject.addProperty("latitude", 49.207);
//            jsonObject.addProperty("longitude", 16.608);
//            jsonObject.addProperty("similar_images", false);

            DemoRetrofitClient.getInstance().getApi()
                    .predictJson(API_KEY, jsonObject)
                    .enqueue(new Callback<DemoResponse>() {
                        @Override
                        public void onResponse(Call<DemoResponse> call, Response<DemoResponse> response) {
                            if (!response.isSuccessful() || response.body() == null) {
                                Log.e(TAG, "API response unsuccessful: " + response.code());
                                tv.setText("Prediction failed. Try again.");
                                cureTextView.setText("");
                                return;
                            }

                            DemoResponse dr = response.body();

                            DemoResponse.Suggestion crop = dr.getTopCrop();
                            DemoResponse.Suggestion disease = dr.getTopDisease();

                            StringBuilder resultText = new StringBuilder();

                            if (crop != null) {
                                resultText.append("Crop:\n • ")
                                        .append(crop.name)
                                        .append(" (")
                                        .append(crop.scientificName)
                                        .append(") — ")
                                        .append(String.format("%.1f%%", crop.probability * 100))
                                        .append("\n\n");
                            } else {
                                resultText.append("No crop detected.\n\n");
                                Log.w(TAG, "No crop suggestion found");
                            }

                            if (disease != null) {
                                resultText.append("Disease:\n • ")
                                        .append(disease.name)
                                        .append(" (")
                                        .append(disease.scientificName)
                                        .append(") — ")
                                        .append(String.format("%.1f%%", disease.probability * 100));
                                displayCureInfo(disease.name);
                            } else {
                                resultText.append("No disease detected.");
                                cureTextView.setText("");
                                Log.w(TAG, "No disease suggestion found");
                            }

                            tv.setText(resultText.toString());
                        }

                        @Override
                        public void onFailure(Call<DemoResponse> call, Throwable t) {
                            Log.e(TAG, "API call failed", t);
                            tv.setText("Error: " + t.getMessage());
                            cureTextView.setText("");
                        }
                    });
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_crop_care);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            } else if (id == R.id.navigation_profile) {
                startActivity(new Intent(getApplicationContext(), ProfilePageActivity.class));
            } else if (id == R.id.navigation_news) {
                startActivity(new Intent(getApplicationContext(), NewsActivity.class));
            } else if (id == R.id.navigation_mandi) {
                startActivity(new Intent(CropCareActivity.this, StatewiseMandiActivity.class));
            }
            return false;
        });

        if (!checkCameraPermission()) {
            requestCameraPermission();
        }
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Camera permission is required to use this feature", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void dispatchTakePictureIntent(ActivityResultLauncher<Intent> launcher) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            launcher.launch(takePictureIntent);
        } else {
            Log.e(TAG, "Camera app is not available");
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayCureInfo(String diseaseNameRaw) {
        String jsonString = loadJSONFromAsset();
        if (jsonString == null) {
            cureTextView.setText(getString(R.string.error_loading_cure));
            return;
        }

        try {
            JSONObject json = new JSONObject(jsonString);

            // Normalize disease name
            String diseaseName = diseaseNameRaw.trim().toLowerCase();

            // Try direct match
            String cureInfo = json.optString(diseaseName, null);

            // If not found, try partial match (fallback)
            if (cureInfo == null || cureInfo.isEmpty()) {
                for (Iterator<String> it = json.keys(); it.hasNext(); ) {
                    String key = it.next();
                    if (diseaseName.contains(key.toLowerCase()) || key.toLowerCase().contains(diseaseName)) {
                        cureInfo = json.optString(key);
                        break;
                    }
                }
            }

            if (cureInfo == null || cureInfo.isEmpty()) {
                cureInfo = getString(R.string.cure_not_available);
            }

            // Stylize display
            String heading = "Cure:\n";
            SpannableString spannable = new SpannableString(heading + cureInfo);
            spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, heading.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new RelativeSizeSpan(1.2f), 0, heading.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            cureTextView.setText(spannable);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                cureTextView.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
            }

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing cure JSON", e);
            cureTextView.setText(getString(R.string.error_loading_cure));
        }
    }

    private String loadJSONFromAsset() {
        try (InputStream is = getAssets().open("cure.json");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } catch (IOException e) {
            Log.e(TAG, "Error loading JSON from asset", e);
            return null;
        }
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
        if (id == R.id.action_profile) {
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
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        return true;
    }

    private boolean settings() {
        Intent intent = new Intent(getApplicationContext(), ProfilePageActivity.class);
        startActivity(intent);
        return true;
    }
}