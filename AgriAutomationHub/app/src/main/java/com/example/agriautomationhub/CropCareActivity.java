//package com.example.agriautomationhub;
//
//import android.Manifest;
//import android.annotation.SuppressLint;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
//import android.graphics.Typeface;
//import android.graphics.text.LineBreaker;
//import android.net.Uri;
//import android.os.Build;
//import android.os.Bundle;
//import android.provider.MediaStore;
//import android.text.Spannable;
//import android.text.SpannableString;
//import android.text.style.RelativeSizeSpan;
//import android.text.style.StyleSpan;
//import android.util.Log;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//
//import com.example.agriautomationhub.ml.PlantDiseaseModel;
//import com.google.android.material.bottomnavigation.BottomNavigationView;
//import com.google.firebase.auth.FirebaseAuth;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.tensorflow.lite.DataType;
//import org.tensorflow.lite.support.image.TensorImage;
//import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.nio.ByteBuffer;
//import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//import java.util.List;
//
//public class CropCareActivity extends AppCompatActivity {
//
//    private ImageView imgView;
//    private TextView tv;
//    private Bitmap img;
//
//
//    @SuppressLint("MissingInflatedId")
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_crop_care);
//
//        ImageView back = findViewById(R.id.back_btn_crop_care);
//        back.setOnClickListener(v -> {
//            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//            startActivity(intent);
//            finish();
//        });
//
//        imgView = findViewById(R.id.imageView);
//        tv = findViewById(R.id.textView);
//        Button select = findViewById(R.id.btn1);
//        Button predict = findViewById(R.id.btn2);
//
//        ActivityResultLauncher<Intent> selectImageLauncher = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
//                        Uri uri = result.getData().getData();
//                        imgView.setImageURI(uri);
//                        try {
//                            img = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
//                        } catch (IOException e) {
//                            Log.e("Image Selection", "Error loading image", e);
//                        }
//                    }
//                });
//
//        select.setOnClickListener(v -> {
//            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//            intent.setType("image/*");
//            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
//            selectImageLauncher.launch(intent);
//        });
//
//        ActivityResultLauncher<Intent> captureImageLauncher = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
//                        Bundle extras = result.getData().getExtras();
//                        Bitmap imageBitmap = (Bitmap) extras.get("data");
//                        if (imageBitmap != null) {
//                            imgView.setImageBitmap(imageBitmap);
//                            img = imageBitmap;
//                        }
//                    }
//                });
//
//        Button captureButton = findViewById(R.id.btnCapture);
//        ImageView captureImg = findViewById(R.id.galleryImageView);
//        captureButton.setOnClickListener(v -> dispatchTakePictureIntent(captureImageLauncher));
//        captureImg.setOnClickListener(v -> dispatchTakePictureIntent(captureImageLauncher));
//
//        predict.setOnClickListener(v -> {
//            if (img == null) {
//                Toast.makeText(this, "Please select or capture an image first", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            img = Bitmap.createScaledBitmap(img, 256, 256, true);
//            try {
//                PlantDiseaseModel model = PlantDiseaseModel.newInstance(CropCareActivity.this);
//
//                // Load the image into a TensorImage
//                TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
//                tensorImage.load(img);
//
//                // Get the ByteBuffer of the TensorImage
//                ByteBuffer byteBuffer = tensorImage.getBuffer();
//
//                // Creates inputs for reference.
//                TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 256, 256, 3}, DataType.FLOAT32);
//                inputFeature0.loadBuffer(byteBuffer);
//
//                // Perform inference
//                PlantDiseaseModel.Outputs outputs = model.process(inputFeature0);
//                TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
//
//                // Release model resources
//                model.close();
//
//                // Load labels
//                String[] labels = loadLabels();
//
//                // Get the confidence scores from the output array
//                float[] confidenceScores = outputFeature0.getFloatArray();
//
//                // Get the index of the maximum value in the output array
//                int maxIndex = getMaxIndex(confidenceScores);
//
//                // Get the maximum confidence score and convert it to a percentage
//                float maxConfidenceScore = confidenceScores[maxIndex];
//                String confidencePercentage = String.format("%.2f", maxConfidenceScore * 100) + "%";
//
//                // TextView for displaying the cure info
//                TextView cureTextView = findViewById(R.id.cureTextView);
//
//                // Check if the confidence score is less than 80%
//                if (maxConfidenceScore < 0.8) {
//                    // If confidence is below 80%, display "Crop not detected" and confidence score
//                    tv.setText(getString(R.string.crop_not_detected) + "\nConfidence: " + confidencePercentage);
//
//                    // Clear the cure info TextView
//                    cureTextView.setText("");  // Clear previous crop's cure info
//                } else {
//                    // Set the text of the TextView with the corresponding label and confidence score
//                    String disease = labels[maxIndex];
//                    tv.setText(getString(R.string.disease_detected, disease) + "\nConfidence: " + confidencePercentage);
//
//                    // Now you can display the cure information based on the disease
//                    displayCureInfo(disease);  // Display new crop's cure info
//                }
//
//            } catch (IOException e) {
//                Log.e("Prediction", "Error running model inference", e);
//            }
//        });
//
//
//        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_crop_care);
//
//        bottomNavigationView.setOnItemSelectedListener(item -> {
//            int id = item.getItemId();
//            if (id == R.id.navigation_home) {
//                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                startActivity(intent);
//            } else if ( id == R.id.navigation_marketView ) {
//                startActivity(new Intent(getApplicationContext(), MarketViewActivity.class));
//            } else if ( id == R.id.navigation_news ) {
//                startActivity(new Intent(getApplicationContext(), NewsActivity.class));
//            } else if ( id == R.id.navigation_mandi ) {
//                startActivity(new Intent(CropCareActivity.this, MandiActivity.class));
//            }
//            return false;
//        });
//
//        if (!checkCameraPermission()) {
//            requestCameraPermission();
//        }
//    }
//
//    private static final int CAMERA_PERMISSION_CODE = 100;
//
//    // Check camera permission
//    private boolean checkCameraPermission() {
//        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
//    }
//
//    // Request camera permission
//    private void requestCameraPermission() {
//        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == CAMERA_PERMISSION_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permission granted, open camera
//            } else {
//                Toast.makeText(this, "Camera permission is required to use this feature", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    private void dispatchTakePictureIntent(ActivityResultLauncher<Intent> launcher) {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            launcher.launch(takePictureIntent);
//        } else {
//            Log.e("Camera", "Camera app is not available");
//            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    // Function to load labels from label.txt file
//    private String[] loadLabels() throws IOException {
//        String fileName = "labels.txt";
//        Log.d("LoadLabels", "Loading labels from file: " + fileName);
//        InputStream inputStream = getAssets().open(fileName);
//        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//        List<String> labels = new ArrayList<>();
//        String line;
//        while ((line = reader.readLine()) != null) {
//            labels.add(line);
//        }
//        reader.close();
//        return labels.toArray(new String[0]);
//    }
//
//    // Function to find the index of the highest value in a float array
//    private int getMaxIndex(float[] array) {
//        int maxIndex = 0;
//        for (int i = 1; i < array.length; i++) {
//            if (array[i] > array[maxIndex]) {
//                maxIndex = i;
//            }
//        }
//        return maxIndex;
//    }
//
////    private void displayCureInfo(String diseaseName) {
////        try {
////            InputStream inputStream = getAssets().open("crop_cure.json");
////            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
////            StringBuilder jsonBuilder = new StringBuilder();
////            String line;
////            while ((line = reader.readLine()) != null) {
////                jsonBuilder.append(line);
////            }
////            reader.close();
////
////            String jsonContent = jsonBuilder.toString();
////            Log.d("CropCure", "Loaded JSON content: " + jsonContent);  // Log the content for debugging
////
////            JSONObject jsonObject = new JSONObject(jsonContent);
////            if (jsonObject.has(diseaseName)) {
////                String cureInfo = jsonObject.getString(diseaseName);
////
////                TextView cureTextView = findViewById(R.id.cureTextView);
////                SpannableString spannableCureInfo = new SpannableString(cureInfo);
////                spannableCureInfo.setSpan(new RelativeSizeSpan(1.2f), 0, spannableCureInfo.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
////                spannableCureInfo.setSpan(new StyleSpan(Typeface.BOLD), 0, spannableCureInfo.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
////                cureTextView.setText(spannableCureInfo);
////            } else {
////                Log.d("CropCure", "No cure information found for the disease: " + diseaseName);
////                Toast.makeText(this, "No cure information available", Toast.LENGTH_SHORT).show();
////            }
////        } catch (IOException e) {
////            Log.e("CropCure", "Error loading JSON: " + e.getMessage(), e);
////            Toast.makeText(this, "Error retrieving cure information", Toast.LENGTH_SHORT).show();
////        } catch (JSONException e) {
////            Log.e("CropCure", "Error parsing JSON: " + e.getMessage(), e);
////            Toast.makeText(this, "Error parsing cure information", Toast.LENGTH_SHORT).show();
////        }
////    }
//
//    private void displayCureInfo(String disease) {
//        TextView cureTextView = findViewById(R.id.cureTextView); // Assuming you have a TextView for displaying cure info
//        String jsonString = loadJSONFromAsset();
//
//        if (jsonString != null) {
//            try {
//                JSONObject json = new JSONObject(jsonString);
//
//                // Convert the disease name to lowercase
//                String lowercaseDisease = disease.toLowerCase();
//
//                // Retrieve cure information using lowercase disease name
//                String cureInfo = json.optString(lowercaseDisease, getString(R.string.cure_not_available));
//
//                // Prepare the heading and the cure info
//                String heading = "Cure:\n";
//                SpannableString spannable = new SpannableString(heading + cureInfo);
//
//                // Set the heading (Cure) as bold and larger size
//                spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, heading.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                spannable.setSpan(new RelativeSizeSpan(1.2f), 0, heading.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//                // Set the formatted text into the TextView
//                cureTextView.setText(spannable);
//
//                // Justify text for API 26 and above
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    cureTextView.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
//                }
//
//            } catch (JSONException e) {
//                Log.e("DisplayCureInfo", "Error parsing JSON", e);
//                cureTextView.setText(getString(R.string.error_loading_cure));
//            }
//        } else {
//            // Error in loading JSON file
//            cureTextView.setText(getString(R.string.error_loading_cure));
//        }
//    }
//
//    private String loadJSONFromAsset() {
//        String json;
//        String filename = "cure.json";
//        try {
//            InputStream is = getAssets().open(filename);
//            int size = is.available();
//            byte[] buffer = new byte[size];
//            is.read(buffer);
//            is.close();
//            json = new String(buffer, StandardCharsets.UTF_8);
//        } catch (IOException e) {
//            Log.e("LoadJSON", "Error loading JSON from asset", e);
//            return null;
//        }
//        return json;
//    }
//}
//
//
//
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
    private static final String API_KEY = "2IaEuoCn64HkUzPqKynfy3DAxpl4h5qybxLZzXJUPdU1soiQ7q";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_care);

        imgView = findViewById(R.id.imageView);
        tv = findViewById(R.id.textView);
        cureTextView = findViewById(R.id.cureTextView);
        Button select = findViewById(R.id.btn1);
        Button predict = findViewById(R.id.btn2);
        Button captureButton = findViewById(R.id.btnCapture);
        ImageView captureImg = findViewById(R.id.galleryImageView);

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
                            Log.d(TAG, "Image selected from gallery");
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
                            Log.d(TAG, "Image captured from camera");
                        }
                    }
                });

        captureButton.setOnClickListener(v -> dispatchTakePictureIntent(captureImageLauncher));
        captureImg.setOnClickListener(v -> dispatchTakePictureIntent(captureImageLauncher));

        predict.setOnClickListener(v -> {
            if (img == null) {
                Toast.makeText(this, "Please select or capture an image first", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "Image selected, preparing to encode and send");

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

            Log.d(TAG, "Sending JSON request to API: " + jsonObject.toString());

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
                            Log.d(TAG, "Received API response: " + new Gson().toJson(dr.result));

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
                                Log.d(TAG, "Top Crop: " + crop.name);
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
                                Log.d(TAG, "Top Disease: " + disease.name);
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
            } else if (id == R.id.navigation_marketView) {
                startActivity(new Intent(getApplicationContext(), MarketViewActivity.class));
            } else if (id == R.id.navigation_news) {
                startActivity(new Intent(getApplicationContext(), NewsActivity.class));
            } else if (id == R.id.navigation_mandi) {
                startActivity(new Intent(CropCareActivity.this, MandiActivity.class));
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
}
