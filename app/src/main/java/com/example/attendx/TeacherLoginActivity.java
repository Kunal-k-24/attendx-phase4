package com.example.attendx;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

public class TeacherLoginActivity extends AppCompatActivity {
    private EditText staffIdField, passwordField;
    private Button loginButton;

    // Predefined Staff IDs and Passwords
    private final HashMap<String, String> teacherCredentials = new HashMap<String, String>() {{
        put("teacher1", "pass123");
        put("teacher2", "secure456");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_login);

        staffIdField = findViewById(R.id.staffId);
        passwordField = findViewById(R.id.password);
        loginButton = findViewById(R.id.buttonLogin);

        loginButton.setOnClickListener(v -> {
            String staffId = staffIdField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (teacherCredentials.containsKey(staffId) && teacherCredentials.get(staffId).equals(password)) {
                startActivity(new Intent(TeacherLoginActivity.this, TeacherDashboardActivity.class));
                finish();
            } else {
                Toast.makeText(TeacherLoginActivity.this, "Invalid Staff ID or Password!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
