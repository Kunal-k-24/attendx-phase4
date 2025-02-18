package com.example.attendx;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MarkAttendanceActivity extends AppCompatActivity {
    private DatabaseReference attendanceRef, studentRef, scannedCountRef;
    private FirebaseUser currentUser;
    private String userId, studentName;
    private String lectureId, date, time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String qrData = getIntent().getStringExtra("qrData");

        if (qrData == null || !qrData.contains("lectureId")) {
            Toast.makeText(this, "Invalid QR Code Data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String[] parts = qrData.split("&");
        lectureId = parts[0].split("=")[1];
        date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        time = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userId = currentUser.getUid();
        studentRef = FirebaseDatabase.getInstance().getReference("Users").child("Students").child(userId);

        // Fetch Student Name from Firebase before marking attendance
        studentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    studentName = snapshot.child("name").getValue(String.class);
                    markAttendance();
                } else {
                    Toast.makeText(MarkAttendanceActivity.this, "Student record not found!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MarkAttendanceActivity.this, "Error fetching student data!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void markAttendance() {
        if (studentName == null) {
            Toast.makeText(this, "Student Name not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        attendanceRef = FirebaseDatabase.getInstance().getReference("Attendance").child(lectureId).child(date);
        scannedCountRef = FirebaseDatabase.getInstance().getReference("Attendance").child(lectureId).child("scannedCount");

        // Check if student already marked attendance
        attendanceRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(MarkAttendanceActivity.this, "Attendance Already Marked!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    saveAttendance();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MarkAttendanceActivity.this, "Error checking attendance!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void saveAttendance() {
        AttendanceRecord record = new AttendanceRecord(userId, studentName, date, time);
        attendanceRef.child(userId).setValue(record).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                updateScannedCount();
                Toast.makeText(MarkAttendanceActivity.this, "Attendance Marked!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MarkAttendanceActivity.this, "Failed to mark attendance", Toast.LENGTH_SHORT).show();
            }
            finish();
        });
    }

    private void updateScannedCount() {
        scannedCountRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int scannedStudents = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
                scannedCountRef.setValue(scannedStudents + 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to update scanned count: " + error.getMessage());
            }
        });
    }
}
