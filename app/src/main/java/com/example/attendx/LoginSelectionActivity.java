package com.example.attendx;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class LoginSelectionActivity extends AppCompatActivity {
    private Button studentLoginButton, teacherLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is already logged in
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userRole = sharedPreferences.getString("userRole", null);

        if (userRole != null) {
            if (userRole.equals("teacher")) {
                startActivity(new Intent(this, TeacherDashboardActivity.class));
            } else {
                startActivity(new Intent(this, StudentDashboardActivity.class));
            }
            finish(); // Close this activity
            return;
        }

        setContentView(R.layout.activity_login_selection);

        studentLoginButton = findViewById(R.id.buttonStudentLogin);
        teacherLoginButton = findViewById(R.id.buttonTeacherLogin);

        studentLoginButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginSelectionActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        teacherLoginButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginSelectionActivity.this, TeacherLoginActivity.class);
            startActivity(intent);
        });
    }
}
