package com.example.agriautomationhub;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SettingsPage extends AppCompatActivity {

    private ImageView logout, back, profileImage;
    private RelativeLayout languageSelector;
    private TextView usernameText, emailText;

    // Cropper launcher
    private final ActivityResultLauncher<CropImageContractOptions> cropImage =
            registerForActivityResult(new CropImageContract(), result -> {
                if (result.isSuccessful()) {
                    Uri croppedImageUri = result.getUriContent();
                    if (croppedImageUri != null) {
                        uploadProfileImage(croppedImageUri);
                    }
                } else {
                    Toast.makeText(this, "Crop failed", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.setLocale(this); // Set locale
        setContentView(R.layout.activity_settings);

        logout = findViewById(R.id.action_logout);
        back = findViewById(R.id.back_btn);
//        languageSelector = findViewById(R.id.language_selector);
        profileImage = findViewById(R.id.profile_image);
        usernameText = findViewById(R.id.user_name);
        emailText = findViewById(R.id.user_email);
        Button editProfileButton = findViewById(R.id.edit_btn);

//        editProfileButton.setOnClickListener(v -> {
//            startActivity(new Intent(SettingsPage.this, EditProfileActivity.class));
//        });


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            usernameText.setText(user.getDisplayName() != null ? user.getDisplayName() : "Username");
            emailText.setText(user.getEmail());
            if (user.getPhotoUrl() != null) {
                Glide.with(this).load(user.getPhotoUrl()).circleCrop().into(profileImage);
            } else {
                profileImage.setImageResource(R.drawable.ic_profile_icon); // Fallback image
            }
        }

        logout.setOnClickListener(v -> logoutUser());

        back.setOnClickListener(v -> {
            startActivity(new Intent(SettingsPage.this, MainActivity.class));
            finish();
        });

//        languageSelector.setOnClickListener(v -> showLanguageSelectionDialog());

        profileImage.setOnClickListener(v -> launchImageCropper());
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish();
    }

    private void showLanguageSelectionDialog() {
        String[] languages = {"English", "Hindi"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Select Language")
                .setItems(languages, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            LocaleHelper.setLocale(this, "en");
                            break;
                        case 1:
                            LocaleHelper.setLocale(this, "hi");
                            break;
                    }
                })
                .show();
    }

    private void launchImageCropper() {
        CropImageOptions cropImageOptions = new CropImageOptions();
        cropImageOptions.guidelines = CropImageView.Guidelines.ON;
        cropImageOptions.cropShape = CropImageView.CropShape.OVAL;
        cropImageOptions.aspectRatioX = 1;
        cropImageOptions.aspectRatioY = 1;

        cropImage.launch(new CropImageContractOptions(null, cropImageOptions));
    }


    private void uploadProfileImage(Uri imageUri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(imageUri)  // Local file Uri
                .build();

        user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Glide.with(SettingsPage.this).load(imageUri).circleCrop().into(profileImage);
                Toast.makeText(SettingsPage.this, "Profile photo set!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SettingsPage.this, "Failed to update photo", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
