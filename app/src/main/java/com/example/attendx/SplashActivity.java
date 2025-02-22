package com.example.attendx;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 2000; // 2 seconds
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private SharedPreferences sharedPreferences;

    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_USER_ID = "userId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Get stored login session
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedUserId = sharedPreferences.getString(KEY_USER_ID, null);

        new Handler().postDelayed(() -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();

            // Check if user session exists
            if (currentUser != null && savedUserId != null) {
                navigateToDashboard(savedUserId);
            } else {
                // No user session, go to login selection
                startActivity(new Intent(SplashActivity.this, LoginSelectionActivity.class));
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

    private void navigateToDashboard(String userId) {
        databaseReference.child(userId).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.getValue(String.class);
                    if ("teacher".equals(role)) {
                        startActivity(new Intent(SplashActivity.this, TeacherDashboardActivity.class));
                    } else {
                        startActivity(new Intent(SplashActivity.this, StudentDashboardActivity.class));
                    }
                    finish(); // Close SplashActivity after redirect
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                startActivity(new Intent(SplashActivity.this, LoginSelectionActivity.class));
                finish();
            }
        });
    }
}
