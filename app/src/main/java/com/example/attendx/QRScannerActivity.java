package com.example.attendx;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.zxing.Result;
import me.dm7.barcodescanner.zxing.ZXingScannerView;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class QRScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView scannerView;
    private DatabaseReference usersRef, attendanceRef;
    private String userId, studentName, enrollmentNo;
    private boolean isStudentDataLoaded = false; // ✅ Ensures data is fetched before scanning

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        // ✅ Fetch student details before allowing scanning
        fetchStudentData();
    }

    private void fetchStudentData() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    studentName = snapshot.child("name").getValue(String.class);
                    enrollmentNo = snapshot.child("enrollment").getValue(String.class);  // ✅ FIXED (changed to "enrollment")

                    Log.d("FirebaseDebug", "Fetched Name: " + studentName + ", Enrollment: " + enrollmentNo);

                    if (studentName != null && enrollmentNo != null) {
                        isStudentDataLoaded = true; // ✅ Mark as loaded
                        Toast.makeText(QRScannerActivity.this, "Student Found: " + studentName, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(QRScannerActivity.this, "Error: Student data incomplete!", Toast.LENGTH_SHORT).show();
                        Log.e("FirebaseError", "Missing student data for userId: " + userId);
                        finish();
                    }
                } else {
                    Toast.makeText(QRScannerActivity.this, "Error: Student not found in Users!", Toast.LENGTH_SHORT).show();
                    Log.e("FirebaseError", "UserId not found in Users: " + userId);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(QRScannerActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        if (!isStudentDataLoaded || studentName == null || enrollmentNo == null) {
            Toast.makeText(this, "Error: Student details not loaded!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            JSONObject qrData = new JSONObject(rawResult.getText());
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            String subject = qrData.getString("subject");
            String lectureType = qrData.getString("lectureType");
            String timeSlot = qrData.getString("timeSlot");
            String sessionId = qrData.getString("sessionId");

            // ✅ Save attendance in Firebase
            attendanceRef = FirebaseDatabase.getInstance().getReference("attendance")
                    .child(date)
                    .child(subject)
                    .child(lectureType)
                    .child(sessionId)
                    .child("students")
                    .child(userId);

            HashMap<String, String> studentData = new HashMap<>();
            studentData.put("name", studentName);
            studentData.put("enrollment", enrollmentNo);  // ✅ FIXED (changed to "enrollment")
            studentData.put("timestamp", new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()));

            attendanceRef.setValue(studentData)
                    .addOnSuccessListener(unused -> Toast.makeText(QRScannerActivity.this, "Attendance Marked Successfully!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(QRScannerActivity.this, "Failed to mark attendance!", Toast.LENGTH_SHORT).show());

        } catch (Exception e) {
            Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}
