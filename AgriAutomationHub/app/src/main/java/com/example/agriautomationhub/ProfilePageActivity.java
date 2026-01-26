package com.example.agriautomationhub;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.example.agriautomationhub.utils.PrefsManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ProfilePageActivity extends AppCompatActivity {
    private ImageView profileImage;
    private EditText usernameText, emailText, phoneText;
    private TextView headerUserName, headerUserEmail;
    private Uri selectedImageUri = null;

    private FirebaseUser user;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private PrefsManager prefs;

    private static final int CAMERA_PERMISSION_CODE = 101;

    // Pick from gallery
    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            launchImageCropper(uri);
                        }
                    });

    // Take photo from camera
    private final ActivityResultLauncher<Uri> takePhoto =
            registerForActivityResult(new ActivityResultContracts.TakePicture(),
                    success -> {
                        if (success && selectedImageUri != null) {
                            launchImageCropper(selectedImageUri);
                        }
                    });

    // Crop image
    private final ActivityResultLauncher<CropImageContractOptions> cropImage =
            registerForActivityResult(new CropImageContract(), result -> {
                if (result.isSuccessful() && result.getUriContent() != null) {
                    selectedImageUri = result.getUriContent();
                    Glide.with(this).load(selectedImageUri).circleCrop().into(profileImage);
                } else if (result.getError() != null) {
                    Toast.makeText(this, "Image selection failed", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.setLocale(this);
        setContentView(R.layout.activity_profile_page);

        profileImage = findViewById(R.id.profile_image);
        usernameText = findViewById(R.id.editTextName);
        emailText = findViewById(R.id.editTextEmail);
        phoneText = findViewById(R.id.editTextPhone);
        headerUserName = findViewById(R.id.header_user_name);
        headerUserEmail = findViewById(R.id.header_user_email);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance("profile-data");
        storageRef = FirebaseStorage.getInstance().getReference("profile_pics/");
        prefs = new PrefsManager(this);

        findViewById(R.id.back_btn_profile).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        // Load from cache first (instant UI)
        loadFromCache();

        if (user != null) {
            loadUserProfile();
        }

        Button saveButton = findViewById(R.id.btnUpdate);
        saveButton.setOnClickListener(v -> updateProfile());
        profileImage.setOnClickListener(v -> showImageSourceDialog());

        Button changePassword = findViewById(R.id.btnChangePassword);
        changePassword.setOnClickListener(view -> {
            String email = emailText.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(ProfilePageActivity.this, "Please enter your email to reset password", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ProfilePageActivity.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ProfilePageActivity.this, "Failed to send reset email", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        findViewById(R.id.btn_logout_profile).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Logout", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(ProfilePageActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_profile);
        bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            } else if (id == R.id.navigation_news) {
                startActivity(new Intent(getApplicationContext(), NewsActivity.class));
                finish();
            } else if (id == R.id.navigation_mandi) {
                startActivity(new Intent(getApplicationContext(), StatewiseMandiActivity.class));
                finish();
            }
            return false;
        });
    }

    private void loadFromCache() {
        usernameText.setText(prefs.getName());
        emailText.setText(prefs.getEmail());
        phoneText.setText(prefs.getPhone());
        headerUserName.setText(prefs.getName());
        headerUserEmail.setText(prefs.getEmail());

        String imageUrl = prefs.getImageUrl();
        if (!imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).circleCrop().into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.ic_profile);
        }
    }

    private void loadUserProfile() {
        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");
                        String phone = documentSnapshot.getString("phone");
                        String imageUrl = documentSnapshot.getString("photoUrl");

                        usernameText.setText(name);
                        emailText.setText(email);
                        phoneText.setText(phone);
                        headerUserName.setText(name);
                        headerUserEmail.setText(email);

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this).load(imageUrl).circleCrop().into(profileImage);
                        }

                        // ✅ Save to local cache
                        prefs.saveUser(name, phone, email, imageUrl);
                    }
                });
    }

    private void saveUserData(DocumentReference userRef, String name, String email, String phone, String imageUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("email", email);
        updates.put("phone", phone);
        if (imageUrl != null) updates.put("photoUrl", imageUrl);

        userRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    // ✅ Update cache instantly
                    prefs.saveUser(name, phone, email, imageUrl);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                );
    }

    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Select Image")
                .setItems(options, (DialogInterface dialog, int which) -> {
                    if (which == 0) { // Camera
                        checkPermissionsAndOpenCamera();
                    } else { // Gallery
                        pickImage.launch("image/*");
                    }
                }).show();
    }

    private void checkPermissionsAndOpenCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        selectedImageUri = Uri.fromFile(new java.io.File(getCacheDir(), "temp_profile_pic.jpg"));
        takePhoto.launch(selectedImageUri);
    }

    private void launchImageCropper(Uri sourceUri) {
        CropImageOptions cropImageOptions = new CropImageOptions();
        cropImageOptions.guidelines = CropImageView.Guidelines.ON;
        cropImageOptions.cropShape = CropImageView.CropShape.OVAL;
        cropImageOptions.aspectRatioX = 1;
        cropImageOptions.aspectRatioY = 1;
        cropImageOptions.autoZoomEnabled = true;
        cropImageOptions.fixAspectRatio = true;
        cropImageOptions.allowFlipping = false;
        cropImageOptions.allowRotation = true;
        cropImageOptions.toolbarColor = getColor(R.color.primary_green_dark);
        cropImageOptions.activityBackgroundColor = getColor(android.R.color.black);

        cropImage.launch(new CropImageContractOptions(sourceUri, cropImageOptions));
    }

    private void updateProfile() {
        String newName = usernameText.getText().toString().trim();
        String newPhone = phoneText.getText().toString().trim();

        if (TextUtils.isEmpty(newName) || TextUtils.isEmpty(newPhone)) {
            Toast.makeText(this, "Name and phone cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (user == null) return;

        DocumentReference userRef = db.collection("users").document(user.getUid());

        if (selectedImageUri != null) {
            StorageReference imgRef = storageRef.child(user.getUid() + ".jpg");
            imgRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        saveUserData(userRef, newName, user.getEmail(), newPhone, uri.toString());
                    }))
                    .addOnFailureListener(e -> Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show());
        } else {
            saveUserData(userRef, newName, user.getEmail(), newPhone, null);
        }
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // close current
    }
}
