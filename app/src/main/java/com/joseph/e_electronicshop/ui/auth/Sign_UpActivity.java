package com.joseph.e_electronicshop.ui.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.joseph.e_electronicshop.MainActivity;
import com.joseph.e_electronicshop.R;

public class Sign_UpActivity extends AppCompatActivity {

    private TextInputEditText nameInput, emailInput, passwordInput, confirmPasswordInput;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        if (mAuth == null) {
            Toast.makeText(this, "Firebase Auth not available", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initialize views
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        TextView signInLink = findViewById(R.id.signInLink);

        // Initialize progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating account...");
        progressDialog.setCancelable(false);

        // Set click listeners
        findViewById(R.id.signUpButton).setOnClickListener(v -> registerUser());
        signInLink.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    private void registerUser() {
        try {
            // Verify views are initialized
            if (nameInput == null || emailInput == null ||
                    passwordInput == null || confirmPasswordInput == null) {
                Toast.makeText(this, "UI elements not initialized", Toast.LENGTH_SHORT).show();
                return;
            }

            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();

            // Input validation
            if (TextUtils.isEmpty(name)) {
                nameInput.setError("Name is required");
                return;
            }

            if (TextUtils.isEmpty(email)) {
                emailInput.setError("Email is required");
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

            if (!password.equals(confirmPassword)) {
                confirmPasswordInput.setError("Passwords don't match");
                return;
            }

            showProgressDialog();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        hideProgressDialog();
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                navigateToMain();
                            }
                        } else {
                            handleRegistrationError(task.getException());
                        }
                    });
        } catch (Exception e) {
            Log.e("SIGNUP_ERROR", "Registration failed", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void handleRegistrationError(Exception exception) {
        try {
            if (exception == null) {
                Toast.makeText(this, "Unknown error occurred", Toast.LENGTH_SHORT).show();
                return;
            }

            throw exception;
        } catch (FirebaseAuthUserCollisionException e) {
            emailInput.setError("Email already in use");
            emailInput.requestFocus();
        } catch (Exception e) {
            Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void showProgressDialog() {
        try {
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Creating account...");
                progressDialog.setCancelable(false);
            }
            if (!progressDialog.isShowing()) {
                progressDialog.show();
            }
        } catch (Exception e) {
            Log.e("PROGRESS_DIALOG", "Error showing dialog", e);
        }
    }

    private void hideProgressDialog() {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            Log.e("PROGRESS_DIALOG", "Error hiding dialog", e);
        }
    }
}