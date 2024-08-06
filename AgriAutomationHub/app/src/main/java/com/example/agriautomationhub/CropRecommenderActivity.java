package com.example.agriautomationhub;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.agriautomationhub.ml.CropRecommendationModel;

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
        temprature = findViewById(R.id.temprature_input);
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
    }
}
