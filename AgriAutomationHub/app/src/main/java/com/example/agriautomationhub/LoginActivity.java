package com.example.agriautomationhub;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Editable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText usernameInput, passwordInput;
    Button loginButton;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView registerLink, forgotPasswordText;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_btn);
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);
        registerLink = findViewById(R.id.register_text);
        forgotPasswordText = findViewById(R.id.forgot_password_text);

        registerLink.setOnClickListener(view -> {
                Intent Intent = new Intent(getApplicationContext(), Register.class);
                startActivity(Intent);
                finish();
        });

        forgotPasswordText.setOnClickListener(view -> {
            Editable editable = usernameInput.getText();
            if (editable == null || TextUtils.isEmpty(editable.toString())) {
                Toast.makeText(LoginActivity.this, "Please enter your email to reset password", Toast.LENGTH_SHORT).show();
                return;
            }

            String email = editable.toString();
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Failed to send reset email", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        loginButton.setOnClickListener(view -> {
                progressBar.setVisibility(View.VISIBLE);
                String email, password;
                email = String.valueOf(usernameInput.getText());
                password = String.valueOf(passwordInput.getText());

                if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
                    Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }
                        });
        });
    }
}
