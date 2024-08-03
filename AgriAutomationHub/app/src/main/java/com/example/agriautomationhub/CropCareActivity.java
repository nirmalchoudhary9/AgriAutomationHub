package com.example.agriautomationhub;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.agriautomationhub.ml.PlantDiseaseModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CropCareActivity extends AppCompatActivity {

    private ImageView imgView;
    private TextView tv;
    private Bitmap img;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_care);

        ImageView back = findViewById(R.id.back_btn);
        back.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        });

        imgView = findViewById(R.id.imageView);
        tv = findViewById(R.id.textView);
        Button select = findViewById(R.id.btn1);
        Button predict = findViewById(R.id.btn2);

        ActivityResultLauncher<Intent> selectImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        imgView.setImageURI(uri);
                        try {
                            img = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                        } catch (IOException e) {
                            Log.e("Image Selection", "Error loading image", e);
                        }
                    }
                });

        select.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
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

        Button captureButton = findViewById(R.id.btnCapture);
        captureButton.setOnClickListener(v -> dispatchTakePictureIntent(captureImageLauncher));

        predict.setOnClickListener(v -> {
            img = Bitmap.createScaledBitmap(img, 256, 256, true);
            try {
                PlantDiseaseModel model = PlantDiseaseModel.newInstance(CropCareActivity.this);

                // Load the image into a TensorImage
                TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
                tensorImage.load(img);

                // Get the ByteBuffer of the TensorImage
                ByteBuffer byteBuffer = tensorImage.getBuffer();

                // Creates inputs for reference.
                TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 256, 256, 3}, DataType.FLOAT32);
                inputFeature0.loadBuffer(byteBuffer);

                // Perform inference
                PlantDiseaseModel.Outputs outputs = model.process(inputFeature0);
                TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

                // Release model resources
                model.close();

                // Load labels
                String[] labels = loadLabels();

                // Get the index of the maximum value in the output array
                int maxIndex = getMaxIndex(outputFeature0.getFloatArray());

                // Set the text of the TextView with the corresponding label
                String disease = labels[maxIndex];
                tv.setText(getString(R.string.disease_detected, disease));

                // Now you can display the cure information based on the disease
                displayCureInfo(disease);

            } catch (IOException e) {
                Log.e("Prediction", "Error running model inference", e);
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            } else if (id == R.id.navigation_news) {
                // Handle News navigation
                startActivity(new Intent(getApplicationContext(), NewsActivity.class));
                return true;
            } else if (id == R.id.navigation_mandi) {
                startActivity(new Intent(CropCareActivity.this, MandiActivity.class));
                return true;
            }
            return false;
        });
    }

    // Function to load labels from label.txt file
    private String[] loadLabels() throws IOException {
        String fileName = "labels.txt";
        Log.d("LoadLabels", "Loading labels from file: " + fileName);
        InputStream inputStream = getAssets().open(fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        List<String> labels = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            labels.add(line);
        }
        Log.d("LoadLabels", "Labels loaded successfully");
        return labels.toArray(new String[0]);
    }

    // Function to get the index of the maximum value in an array
    private int getMaxIndex(float[] array) {
        int maxIndex = 0;
        float maxValue = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > maxValue) {
                maxIndex = i;
                maxValue = array[i];
            }
        }
        return maxIndex;
    }

    private void displayCureInfo(String disease) {
        TextView cureTextView = findViewById(R.id.cureTextView); // Assuming you have a TextView for displaying cure info
        String jsonString = loadJSONFromAsset();

        if (jsonString != null) {
            try {
                JSONObject json = new JSONObject(jsonString);

                // Convert the disease name to lowercase
                String lowercaseDisease = disease.toLowerCase();

                // Retrieve cure information using lowercase disease name
                String cureInfo = json.optString(lowercaseDisease, getString(R.string.cure_not_available));
                cureTextView.setText(getString(R.string.cure_info, cureInfo));

            } catch (JSONException e) {
                Log.e("DisplayCureInfo", "Error parsing JSON", e);
                cureTextView.setText(getString(R.string.error_loading_cure));
            }
        } else {
            // Error in loading JSON file
            cureTextView.setText(getString(R.string.error_loading_cure));
        }
    }

    private String loadJSONFromAsset() {
        String json;
        String filename = "cure.json";
        try {
            InputStream is = getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Log.e("LoadJSON", "Error loading JSON from asset", e);
            return null;
        }
        return json;
    }

    private void dispatchTakePictureIntent(ActivityResultLauncher<Intent> launcher) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            launcher.launch(takePictureIntent);
        }
    }
}
