//package com.example.agriautomationhub;
//
//import androidx.appcompat.app.AppCompatActivity;
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import com.google.android.material.bottomnavigation.BottomNavigationView;
//import com.google.firebase.auth.FirebaseAuth;
//
//import okhttp3.MediaType;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.RequestBody;
//import okhttp3.ResponseBody;
//import okhttp3.logging.HttpLoggingInterceptor;
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//import retrofit2.Retrofit;
//import retrofit2.converter.gson.GsonConverterFactory;
//import retrofit2.converter.scalars.ScalarsConverterFactory;
//
//import java.io.IOException;
//import java.util.concurrent.TimeUnit;
//
//public class CropRecommenderActivity extends AppCompatActivity {
//    private static final String TAG = "CropRecommenderActivity";
//    EditText nitrogen, phosphorus, potassium, temperature, humidity, ph, rainfall;
//    Button predict;
//    TextView output, details;
//    String predictedCrop = "";
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_crop_recommender);
//
//        // Initialize EditTexts, Buttons, etc.
//        nitrogen = findViewById(R.id.N_input);
//        phosphorus = findViewById(R.id.P_input);
//        potassium = findViewById(R.id.K_input);
//        temperature = findViewById(R.id.temperature_input);
//        humidity = findViewById(R.id.humidity_input);
//        ph = findViewById(R.id.ph_input);
//        rainfall = findViewById(R.id.rainfall_input);
//        predict = findViewById(R.id.recommender_btn);
//        output = findViewById(R.id.output_text);
//        details = findViewById(R.id.get_details);
//
//        // Corrected Azure API URL
//        String baseUrl = "https://crop-recommendation-model-jfmzl.centralus.inference.ml.azure.com/";
//
//        // OkHttpClient Setup
//        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .connectTimeout(30, TimeUnit.SECONDS) // Increase connection timeout
//                .readTimeout(30, TimeUnit.SECONDS)    // Increase read timeout
//                .writeTimeout(30, TimeUnit.SECONDS)
//                .addInterceptor(chain -> {
//                    Request original = chain.request();
//                    Request.Builder requestBuilder = original.newBuilder()
//                            .header("Authorization", "Bearer YdOviL9BY5oAMUETnAGBZUpzUCqLFm8R")
//                            .header("Content-Type", "application/json");
//                    return chain.proceed(requestBuilder.build());
//                })
//                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
//                .build();
//
//        // Retrofit Setup
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(baseUrl)
//                .addConverterFactory(ScalarsConverterFactory.create())
//                .client(okHttpClient)
//                .build();
//
//        CropRecommendationAPI api = retrofit.create(CropRecommendationAPI.class);
//
//        predict.setOnClickListener(v -> {
//            try {
//                // Collect input values from EditText fields
//                float nitrogenValue = Float.parseFloat(nitrogen.getText().toString());
//                float phosphorusValue = Float.parseFloat(phosphorus.getText().toString());
//                float potassiumValue = Float.parseFloat(potassium.getText().toString());
//                float temperatureValue = Float.parseFloat(temperature.getText().toString());
//                float humidityValue = Float.parseFloat(humidity.getText().toString());
//                float phValue = Float.parseFloat(ph.getText().toString());
//                float rainfallValue = Float.parseFloat(rainfall.getText().toString());
//
//                // Create JSON Request Body
//                String jsonRequest = "{\n" +
//                        "  \"input_data\": {\n" +
//                        "    \"columns\": [\n" +
//                        "      \"N\",\n" +
//                        "      \"P\",\n" +
//                        "      \"K\",\n" +
//                        "      \"temperature\",\n" +
//                        "      \"humidity\",\n" +
//                        "      \"ph\",\n" +
//                        "      \"rainfall\"\n" +
//                        "    ],\n" +
//                        "    \"index\": [0],\n" +
//                        "    \"data\": [[" + nitrogenValue + "," + phosphorusValue + "," + potassiumValue + "," + temperatureValue + "," + humidityValue + "," + phValue + "," + rainfallValue + "]]\n" +
//                        "  }\n" +
//                        "}";
//
//                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonRequest);
//
//                // Make API Call
//                Call<ResponseBody> call = api.getRecommendation(requestBody);
//                call.enqueue(new Callback<ResponseBody>() {
//                    @Override
//                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                        if (response.isSuccessful()) {
//                            try {
//                                String result = response.body().string();
//                                // Clean the response to get only the crop name, remove the [" and "]
//                                predictedCrop = result.replace("[", "").replace("]", "").replace("\"", "");
//                                output.setText("Predicted Crop: " + predictedCrop);  // Update UI with the clean crop name
//                            } catch (IOException e) {
//                                Log.e(TAG, "Error parsing response", e);
//                            }
//                        } else {
//                            output.setText("Prediction failed: " + response.code());
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Call<ResponseBody> call, Throwable t) {
//                        Log.e(TAG, "API call failed", t);
//                        output.setText("API call failed: " + t.getMessage());
//                    }
//                });
//
//            } catch (NumberFormatException e) {
//                output.setText("Please enter valid numbers in all fields.");
//            }
//        });
//
//        details.setOnClickListener(v -> {
//            if (!predictedCrop.isEmpty()) {
//                Log.d(TAG, "Predicted crop: " + predictedCrop);
//                Intent intent = new Intent(CropRecommenderActivity.this, CropDetailActivity.class);
//                intent.putExtra("cropName", predictedCrop);  // Pass the cleaned crop name
//                startActivity(intent);
//            } else {
//                output.setText("Please predict a crop first.");
//            }
//        });
//
//
//        ImageView back = findViewById(R.id.back_btn_crop_recommender);
//        back.setOnClickListener(v -> {
//            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//            startActivity(intent);
//            finish();
//        });
//
//        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_crop);
//        bottomNavigationView.setOnItemSelectedListener(item -> {
//            int id = item.getItemId();
//            if (id == R.id.navigation_home) {
//                startActivity(new Intent(getApplicationContext(), MainActivity.class));
//                return true;
//            } else if (id == R.id.navigation_news) {
//                startActivity(new Intent(getApplicationContext(), NewsActivity.class));
//                return true;
//            } else if (id == R.id.navigation_mandi) {
//                startActivity(new Intent(CropRecommenderActivity.this, MandiActivity.class));
//                return true;
//            }
//            return false;
//        });
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == R.id.action_logout) {
//            return logoutUser();
//        }
//        if (id == R.id.action_settings) {
//            return settings();
//        }
//        if (id == R.id.action_help) {
//            Intent intent = new Intent(getApplicationContext(), HelpActivity.class);
//            startActivity(intent);
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    private boolean logoutUser() {
//        FirebaseAuth.getInstance().signOut();
//        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
//        startActivity(intent);
//        finish();
//        return true;
//    }
//
//    private boolean settings() {
//        Intent intent = new Intent(getApplicationContext(), SettingsPage.class);
//        startActivity(intent);
//        return true;
//    }
//}
package com.example.agriautomationhub;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.agriautomationhub.ml.CropRecommendationModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class CropRecommenderActivity extends AppCompatActivity {
    private static final String TAG = "CropRecommenderActivity";
    EditText nitrogen, phosporus, potassium, temprature, humidity, ph, rainfall;
    Button predict;
    TextView output, details;
    String predictedCrop = ""; // Declare predictedCrop as a member variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_recommender);

        nitrogen = findViewById(R.id.N_input);
        phosporus = findViewById(R.id.P_input);
        potassium = findViewById(R.id.K_input);
        temprature = findViewById(R.id.temperature_input);
        humidity = findViewById(R.id.humidity_input);
        ph = findViewById(R.id.ph_input);
        rainfall = findViewById(R.id.rainfall_input);
        predict = findViewById(R.id.recommender_btn);
        output = findViewById(R.id.output_text);
        details = findViewById(R.id.get_details);

        String[] labels = {"apple","banana","blackgram","chickpea","coconut","coffee", "cotton",
                "grapes", "jute","kidneybeans","lentil","maize", "mango", "mothbeans",
                "mungbean", "muskmelon","orange","papaya","pigeonpeas","pomegranate",
                "rice","watermelon"};  // Replace with your actual crop labels

        predict.setOnClickListener(v -> {
            CropRecommendationModel model = null;
            try {
                model = CropRecommendationModel.newInstance(this);

                // Get input values
                float nitrogenValue = Float.parseFloat(nitrogen.getText().toString());
                float phosporusValue = Float.parseFloat(phosporus.getText().toString());
                float potassiumValue = Float.parseFloat(potassium.getText().toString());
                float tempratureValue = Float.parseFloat(temprature.getText().toString());
                float humidityValue = Float.parseFloat(humidity.getText().toString());
                float phValue = Float.parseFloat(ph.getText().toString());
                float rainfallValue = Float.parseFloat(rainfall.getText().toString());

                // Creates inputs for reference.
                TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 7}, DataType.FLOAT32);
                ByteBuffer byteBuffer = ByteBuffer.allocateDirect(7 * 4);
                byteBuffer.order(ByteOrder.nativeOrder());

                // Add values to the byte buffer
                byteBuffer.putFloat(nitrogenValue);
                byteBuffer.putFloat(phosporusValue);
                byteBuffer.putFloat(potassiumValue);
                byteBuffer.putFloat(tempratureValue);
                byteBuffer.putFloat(humidityValue);
                byteBuffer.putFloat(phValue);
                byteBuffer.putFloat(rainfallValue);

                inputFeature0.loadBuffer(byteBuffer);

                // Runs model inference and gets result.
                CropRecommendationModel.Outputs outputs = model.process(inputFeature0);
                TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

                // Get the output array
                float[] prediction = outputFeature0.getFloatArray();

                // Find the index of the maximum value
                int maxIndex = -1;
                float maxProbability = Float.NEGATIVE_INFINITY;
                for (int i = 0; i < prediction.length; i++) {
                    if (prediction[i] > maxProbability) {
                        maxProbability = prediction[i];
                        maxIndex = i;
                    }
                }

                // Get the label for the predicted class
                predictedCrop = labels[maxIndex]; // Set the predictedCrop value

                // Display the predicted crop
                output.setText(predictedCrop);

            } catch (IOException e) {
                Log.e("CropRecommenderActivity", "Error loading model", e);
            } catch (NumberFormatException e) {
                output.setText("Please enter valid numbers in all fields.");
            } finally {
                if (model != null) {
                    model.close();
                }
            }
        });

        details.setOnClickListener(v -> {
            if (!predictedCrop.isEmpty()) { // Check if predictedCrop is not empty
                Log.d(TAG, "Predicted crop: " + predictedCrop);
                Intent intent = new Intent(CropRecommenderActivity.this, CropDetailActivity.class);
                intent.putExtra("cropName", predictedCrop);
                startActivity(intent);
            } else {
                output.setText("Please predict a crop first.");
            }
        });

        ImageView back = findViewById(R.id.back_btn_crop_recommender);
        back.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_crop);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                return true;
            }else if (id == R.id.navigation_news) {
                // Handle News navigation
                startActivity(new Intent(getApplicationContext(), NewsActivity.class));
                return true;
            } else if (id == R.id.navigation_mandi) {
                startActivity(new Intent(CropRecommenderActivity.this, MandiActivity.class));
                return true;
            }
            return false;
        });
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
        if (id == R.id.action_settings) {
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
        // Redirect to login screen or any other desired activity
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
        return true;
    }

    private boolean settings() {
        Intent intent = new Intent(getApplicationContext(), SettingsPage.class);
        startActivity(intent);
        return true;
    }
}
