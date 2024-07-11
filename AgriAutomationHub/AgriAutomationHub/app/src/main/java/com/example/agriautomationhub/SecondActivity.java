package com.example.agriautomationhub;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

public class SecondActivity extends AppCompatActivity {

    private ImageView imgView, back;
    private Button select, predict;
    private TextView tv;
    private Bitmap img;

    private static final int REQUEST_IMAGE_CAPTURE = 101;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        back = findViewById(R.id.back_btn);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        imgView = findViewById(R.id.imageView);
        tv = findViewById(R.id.textView);
        select = findViewById(R.id.btn1);
        predict = findViewById(R.id.btn2);

        select.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to open the file manager
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                // Set the type of files to select (images)
                intent.setType("image/*");
                // Allow multiple selections
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                // Start the activity to select files
                startActivityForResult(intent, 100);
            }
        });

        Button captureButton = findViewById(R.id.btnCapture);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        predict.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                img = Bitmap.createScaledBitmap(img, 256, 256, true);
                try {
                    PlantDiseaseModel model = PlantDiseaseModel.newInstance(SecondActivity.this);

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
                    String[] labels = loadLabels("labels.txt");

                    // Get the index of the maximum value in the output array
                    int maxIndex = getMaxIndex(outputFeature0.getFloatArray());

                    // Set the text of the TextView with the corresponding label
                    String disease = labels[maxIndex];
                    tv.setText("Disease: " + disease);

                    // Now you can display the cure information based on the disease
                    displayCureInfo(disease);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
                    openWebsite("https://eanugya.mp.gov.in/Inward_Quote.aspx");  // Replace with your URL
                    return true;
                }
                return false;
            }
        });
    }

    private void openWebsite(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    // Function to load labels from label.txt file
    private String[] loadLabels(String fileName) throws IOException {
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
        String jsonString = loadJSONFromAsset("cure.json");

        if (jsonString != null) {
            try {
                JSONObject json = new JSONObject(jsonString);

                // Convert the disease name to lowercase
                String lowercaseDisease = disease.toLowerCase();

                // Retrieve cure information using lowercase disease name
                String cureInfo = json.optString(lowercaseDisease, "Cure information not available");
                cureTextView.setText("Cure:\n" + cureInfo);

            } catch (JSONException e) {
                e.printStackTrace();
                cureTextView.setText("Error loading cure information");
            }
        } else {
            // Error in loading JSON file
            cureTextView.setText("Error loading cure information");
        }
    }

    private String loadJSONFromAsset(String filename) {
        String json;
        try {
            InputStream is = getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            imgView.setImageURI(data.getData());

            Uri uri = data.getData();
            try {
                img = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            if (imageBitmap != null) {
                imgView.setImageBitmap(imageBitmap);
                img = imageBitmap;
            }
        }
    }
}
