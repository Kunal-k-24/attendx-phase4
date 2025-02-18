package com.example.attendx;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText emailField, passwordField;
    private Button loginButton, registerButton;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailField = findViewById(R.id.editTextEmail);
        passwordField = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.buttonLogin);
        registerButton = findViewById(R.id.buttonRegister);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Check if user is already logged in
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            checkCachedRole(user.getUid());
        }

        // ✅ Fix: Pass email and password correctly
        loginButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                loginUser(email, password); // Pass values correctly
            } else {
                Toast.makeText(LoginActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            }
        });

        registerButton.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    // Check if the user role is cached in SharedPreferences
    private void checkCachedRole(String userId) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String cachedRole = prefs.getString("userRole", null);

        if (cachedRole != null) {
            openDashboard(cachedRole);
        } else {
            fetchUserRole(userId);
        }
    }

    // Fetch user role from Firebase if not cached
    private void fetchUserRole(String userId) {
        databaseReference.child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                String role = task.getResult().child("role").getValue(String.class);

                if (role != null) {
                    cacheUserRole(role);
                    openDashboard(role);
                } else {
                    Toast.makeText(LoginActivity.this, "User role missing in database!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(LoginActivity.this, "Failed to retrieve user role.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Cache the user role in SharedPreferences
    private void cacheUserRole(String role) {
        SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
        editor.putString("userRole", role);
        editor.apply();
    }

    // Redirect user to the correct dashboard
    private void openDashboard(String role) {
        if ("teacher".equals(role)) {
            startActivity(new Intent(LoginActivity.this, TeacherDashboardActivity.class));
        } else {
            startActivity(new Intent(LoginActivity.this, StudentDashboardActivity.class));
        }
        finish();
    }

    // Handle user login
    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    fetchUserRole(user.getUid()); // Get role from DB
                } else {
                    Toast.makeText(LoginActivity.this, "User authentication failed!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(LoginActivity.this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
