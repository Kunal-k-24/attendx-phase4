package com.example.attendx;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class StudentDashboardActivity extends AppCompatActivity {

    private CardView cardScanQR, cardAttendanceHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        cardScanQR = findViewById(R.id.cardScanQR);
        cardAttendanceHistory = findViewById(R.id.cardAttendanceHistory);

        // Open QR Scanner
        cardScanQR.setOnClickListener(v -> {
            Intent intent = new Intent(StudentDashboardActivity.this, QRScannerActivity.class);
            startActivity(intent);
        });

        // Open Attendance History
        cardAttendanceHistory.setOnClickListener(v -> {
            Intent intent = new Intent(StudentDashboardActivity.this, AttendanceHistoryActivity.class);
            startActivity(intent);
        });
    }
}
