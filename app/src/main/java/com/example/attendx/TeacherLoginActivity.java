package com.example.attendx;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashMap;

public class TeacherLoginActivity extends AppCompatActivity {
    private EditText staffIdEditText, passwordEditText;
    private Button loginButton;

    // Predefined teacher credentials
    private final HashMap<String, String> teacherAccounts = new HashMap<String, String>() {{
        put("teacher1", "password123");
        put("teacher2", "securepass456");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_login);

        staffIdEditText = findViewById(R.id.editTextStaffID);
        passwordEditText = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.buttonLogin);

        loginButton.setOnClickListener(v -> {
            String staffId = staffIdEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (validateTeacher(staffId, password)) {
                // ✅ Save login state
                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("userRole", "teacher"); // Save role
                editor.apply();

                Toast.makeText(TeacherLoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(TeacherLoginActivity.this, TeacherDashboardActivity.class));
                finish();
            } else {
                Toast.makeText(TeacherLoginActivity.this, "Invalid Credentials!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateTeacher(String staffId, String password) {
        return teacherAccounts.containsKey(staffId) && teacherAccounts.get(staffId).equals(password);
    }
}
