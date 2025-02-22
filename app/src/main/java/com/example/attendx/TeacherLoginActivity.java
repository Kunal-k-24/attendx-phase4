package com.example.attendx;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

public class TeacherLoginActivity extends AppCompatActivity {
    private EditText staffIdField, passwordField;
    private Button loginButton;
    private SharedPreferences sharedPreferences;

    private static final String PREFS_NAME = "AttendX_Prefs";
    private static final String KEY_USER_ROLE = "userRole";

    private final HashMap<String, String> teacherCredentials = new HashMap<String, String>() {{
        put("teacher1", "pass123");
        put("teacher2", "secure456");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_login);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        staffIdField = findViewById(R.id.staffId);
        passwordField = findViewById(R.id.password);
        loginButton = findViewById(R.id.buttonLogin);

        // Auto-login if teacher session exists
        String savedUserRole = sharedPreferences.getString(KEY_USER_ROLE, null);
        if ("teacher".equals(savedUserRole)) {
            startActivity(new Intent(TeacherLoginActivity.this, TeacherDashboardActivity.class));
            finish();
        }

        loginButton.setOnClickListener(v -> {
            String staffId = staffIdField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (teacherCredentials.containsKey(staffId) && teacherCredentials.get(staffId).equals(password)) {
                // Save session
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(KEY_USER_ROLE, "teacher");
                editor.apply();

                startActivity(new Intent(TeacherLoginActivity.this, TeacherDashboardActivity.class));
                finish();
            } else {
                Toast.makeText(TeacherLoginActivity.this, "Invalid Staff ID or Password!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
