package com.example.attendx;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Button logoutButton, attendanceButton, viewAttendanceButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            SharedPreferences prefs = getSharedPreferences("AttendXPrefs", MODE_PRIVATE);
            boolean isFirstTime = prefs.getBoolean("isFirstTime", true);

            if (isFirstTime) {
                startActivity(new Intent(MainActivity.this, LoginSelectionActivity.class));
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("isFirstTime", false);
                editor.apply();
            } else {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        logoutButton = findViewById(R.id.buttonLogout);
        attendanceButton = findViewById(R.id.buttonAttendance);
        viewAttendanceButton = findViewById(R.id.buttonViewAttendance);

        attendanceButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AttendanceActivity.class);
            startActivity(intent);
        });

        viewAttendanceButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ViewAttendanceActivity.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(MainActivity.this, "Logged out!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });
    }
}
