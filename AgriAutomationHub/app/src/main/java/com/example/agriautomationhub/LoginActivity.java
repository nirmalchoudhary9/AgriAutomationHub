package com.example.agriautomationhub;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.example.agriautomationhub.utils.PrefsManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText usernameInput, passwordInput;
    Button loginButton;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView registerLink, forgotPasswordText;

    FirebaseFirestore db;
    PrefsManager prefs;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User already logged in, go to MainActivity
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameInput = findViewById(R.id.login_email_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_btn);
        progressBar = findViewById(R.id.progressBar);
        registerLink = findViewById(R.id.register_text);
        forgotPasswordText = findViewById(R.id.forgot_password_text);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Use default instance
        prefs = new PrefsManager(this);

        // Go to Register screen
        registerLink.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), Register.class);
            startActivity(intent);
            finish();
        });

        // Forgot password flow
        forgotPasswordText.setOnClickListener(view -> {
            Editable editable = usernameInput.getText();
            if (editable == null || TextUtils.isEmpty(editable.toString())) {
                usernameInput.setError("Enter email to reset password");
                return;
            }

            String email = editable.toString();
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    "Failed to send reset email: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Login button
        loginButton.setOnClickListener(view -> {
            String email = String.valueOf(usernameInput.getText()).trim();
            String password = String.valueOf(passwordInput.getText()).trim();

            if (TextUtils.isEmpty(email)) {
                usernameInput.setError("Email is required");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                passwordInput.setError("Password is required");
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Fetch user profile from Firestore "users" collection
                                DocumentReference docRef = db.collection("users").document(user.getUid());
                                docRef.get().addOnSuccessListener(document -> {
                                    progressBar.setVisibility(View.GONE);
                                    if (document.exists()) {
                                        String pName = document.getString("name");
                                        String pPhone = document.getString("phone");
                                        String pEmail = document.getString("email");
                                        String photoUrl = document.getString("photoUrl");

                                        // ✅ Save to cache immediately
                                        prefs.saveUser(pName, pPhone, pEmail, photoUrl);

                                        Toast.makeText(LoginActivity.this,
                                                "Welcome back " + (pName != null ? pName : ""),
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Profile not found in 'users' collection",
                                                Toast.LENGTH_LONG)
                                                .show();
                                    }

                                    // Go to MainActivity
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }).addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(LoginActivity.this, "Error loading profile: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                    finish();
                                });
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this,
                                    "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
        });
    }
}
