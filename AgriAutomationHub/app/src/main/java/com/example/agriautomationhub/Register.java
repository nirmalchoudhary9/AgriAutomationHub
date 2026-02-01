package com.example.agriautomationhub;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.example.agriautomationhub.utils.PrefsManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    TextInputEditText nameInput, emailInput, phoneInput, passwordInput;
    Button registerButton;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView loginLink;
    ImageView profileImage;

    Uri selectedImageUri = null;
    FirebaseFirestore db;
    StorageReference storageRef;
    PrefsManager prefs;

    // Image picker launcher
    ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Default instance matching Login
        storageRef = FirebaseStorage.getInstance().getReference("profile_pics");
        prefs = new PrefsManager(this);

        nameInput = findViewById(R.id.name_input);
        emailInput = findViewById(R.id.register_email_input);
        phoneInput = findViewById(R.id.phone_input);
        passwordInput = findViewById(R.id.password_input);
        registerButton = findViewById(R.id.register_btn);
        progressBar = findViewById(R.id.progressBar);
        loginLink = findViewById(R.id.login_text);
        profileImage = findViewById(R.id.profile_image);

        // Setup image picker
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        profileImage.setImageURI(uri);
                    }
                });

        profileImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        loginLink.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        });

        registerButton.setOnClickListener(view -> {
            String name = String.valueOf(nameInput.getText()).trim();
            String email = String.valueOf(emailInput.getText()).trim();
            String phone = String.valueOf(phoneInput.getText()).trim();
            String password = String.valueOf(passwordInput.getText()).trim();

            // Validation
            if (TextUtils.isEmpty(name)) {
                nameInput.setError("Name is required");
                return;
            }
            if (TextUtils.isEmpty(email)) {
                emailInput.setError("Email is required");
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.setError("Invalid email address");
                return;
            }
            if (TextUtils.isEmpty(phone)) {
                phoneInput.setError("Phone number is required");
                return;
            }
            if (phone.length() < 10) {
                phoneInput.setError("Phone number must be at least 10 digits");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                passwordInput.setError("Password is required");
                return;
            }
            if (password.length() < 6) {
                passwordInput.setError("Password must be at least 6 characters");
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            // Create account in FirebaseAuth
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                if (selectedImageUri != null) {
                                    uploadProfileImageAndSave(user.getUid(), name, email, phone);
                                } else {
                                    saveUserToFirestore(user.getUid(), name, email, phone, null);
                                }
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(Register.this, "Authentication failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    private void uploadProfileImageAndSave(String uid, String name, String email, String phone) {
        StorageReference fileRef = storageRef.child(uid + ".jpg");
        fileRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String photoUrl = uri.toString();
                    saveUserToFirestore(uid, name, email, phone, photoUrl);
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(Register.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    saveUserToFirestore(uid, name, email, phone, null);
                });
    }

    private void saveUserToFirestore(String uid, String name, String email, String phone, String photoUrl) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("email", email);
        userMap.put("phone", phone);
        if (photoUrl != null)
            userMap.put("photoUrl", photoUrl);

        db.collection("users").document(uid).set(userMap)
                .addOnSuccessListener(aVoid -> {
                    // ✅ Save to cache immediately
                    prefs.saveUser(name, phone, email, photoUrl);

                    Toast.makeText(Register.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast
                        .makeText(Register.this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_SHORT)
                        .show());
    }
}
