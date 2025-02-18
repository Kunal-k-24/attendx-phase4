package com.example.attendx;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class StudentDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);


        Button buttonScanQR = findViewById(R.id.buttonScanQR);
        buttonScanQR.setOnClickListener(v -> {
            Intent intent = new Intent(StudentDashboardActivity.this, QRScannerActivity.class);
            startActivity(intent);
        });
    }
}
