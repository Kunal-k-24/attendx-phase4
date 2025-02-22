package com.example.attendx;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText loginEmail, loginPassword;
    private Button buttonLogin;
    private TextView registerOption;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private SharedPreferences sharedPreferences;

    private static final String PREFS_NAME = "AttendX_Prefs";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_ROLE = "userRole";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Link UI elements
        loginEmail = findViewById(R.id.loginEmail);
        loginPassword = findViewById(R.id.loginPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        registerOption = findViewById(R.id.registerOption);

        // Auto-login if session exists
        String savedUserId = sharedPreferences.getString(KEY_USER_ID, null);
        String savedUserRole = sharedPreferences.getString(KEY_USER_ROLE, null);
        if (savedUserId != null && savedUserRole != null) {
            navigateToDashboard(savedUserRole);
        }

        // Login Button Click Listener
        buttonLogin.setOnClickListener(v -> {
            String email = loginEmail.getText().toString().trim();
            String password = loginPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(email, password);
            }
        });

        // Register Option Click Listener (Only for students)
        registerOption.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String userId = mAuth.getCurrentUser().getUid();

                // Fetch role from Firebase
                databaseReference.child(userId).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String role = snapshot.getValue(String.class);

                            // Save session
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(KEY_USER_ID, userId);
                            editor.putString(KEY_USER_ROLE, role);
                            editor.apply();

                            navigateToDashboard(role);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(LoginActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(LoginActivity.this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToDashboard(String role) {
        if ("teacher".equals(role)) {
            startActivity(new Intent(LoginActivity.this, TeacherDashboardActivity.class));
        } else {
            startActivity(new Intent(LoginActivity.this, StudentDashboardActivity.class));
        }
        finish(); // Close LoginActivity
    }
}
