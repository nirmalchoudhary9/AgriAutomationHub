package com.example.agriautomationhub;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private EditText nameEditText;
    private Button saveButton;
    private Uri selectedImageUri = null;

    private final ActivityResultLauncher<CropImageContractOptions> cropLauncher =
            registerForActivityResult(new CropImageContract(), result -> {
                if (result.isSuccessful() && result.getUriContent() != null) {
                    selectedImageUri = result.getUriContent();
                    Glide.with(this).load(selectedImageUri).circleCrop().into(profileImageView);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        profileImageView = findViewById(R.id.edit_profile_image);
        nameEditText = findViewById(R.id.edit_name);
        saveButton = findViewById(R.id.save_profile_button);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            nameEditText.setText(user.getDisplayName());
            if (user.getPhotoUrl() != null) {
                Glide.with(this).load(user.getPhotoUrl()).circleCrop().into(profileImageView);
            }
        }

        profileImageView.setOnClickListener(v -> {
            CropImageOptions options = new CropImageOptions();
            options.guidelines = CropImageView.Guidelines.ON;
            options.cropShape = CropImageView.CropShape.OVAL;
            options.aspectRatioX = 1;
            options.aspectRatioY = 1;

            cropLauncher.launch(new CropImageContractOptions(null, options));
        });

        saveButton.setOnClickListener(v -> updateProfile());
    }

    private void updateProfile() {
        String newName = nameEditText.getText().toString().trim();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder();
        builder.setDisplayName(newName);
        if (selectedImageUri != null) builder.setPhotoUri(selectedImageUri);

        user.updateProfile(builder.build()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                finish(); // Go back to settings
            } else {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
