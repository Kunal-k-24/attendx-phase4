package com.example.attendx;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class TeacherDashboardActivity extends AppCompatActivity {

    private Button generateQRButton, getAttendanceButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard); // ✅ Make sure this matches the XML file name

        // ✅ Correct IDs (must match XML)
        generateQRButton = findViewById(R.id.buttonGenerateQR);
        getAttendanceButton = findViewById(R.id.buttonGetAttendance);

        // ✅ Null check before using buttons
        if (generateQRButton != null) {
            generateQRButton.setOnClickListener(v -> {
                Intent intent = new Intent(TeacherDashboardActivity.this, GenerateQRActivity.class);
                startActivity(intent);
            });
        } else {
            throw new RuntimeException("buttonGenerateQR is NULL. Check your XML file!");
        }

        if (getAttendanceButton != null) {
            getAttendanceButton.setOnClickListener(v -> {
                Intent intent = new Intent(TeacherDashboardActivity.this, GetAttendanceActivity.class);
                startActivity(intent);
            });
        } else {
            throw new RuntimeException("buttonGetAttendance is NULL. Check your XML file!");
        }
    }
}
