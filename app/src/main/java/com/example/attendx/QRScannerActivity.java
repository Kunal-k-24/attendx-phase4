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
    private boolean isStudentDataLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        fetchStudentData();
    }

    private void fetchStudentData() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    studentName = snapshot.child("name").getValue(String.class);
                    enrollmentNo = snapshot.child("enrollment").getValue(String.class);
                    isStudentDataLoaded = (studentName != null && enrollmentNo != null);
                } else {
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
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
        if (!isStudentDataLoaded) {
            finish();
            return;
        }

        try {
            JSONObject qrData = new JSONObject(rawResult.getText());
            String date = qrData.getString("date");
            String subject = qrData.getString("subject");
            String lectureType = qrData.getString("lectureType");
            String sessionId = qrData.getString("sessionId");

            attendanceRef = FirebaseDatabase.getInstance().getReference("attendance")
                    .child(date)
                    .child(subject)
                    .child(lectureType)
                    .child(sessionId)
                    .child("students")
                    .child(userId);

            attendanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(QRScannerActivity.this, "Attendance already marked!", Toast.LENGTH_SHORT).show();
                    } else {
                        HashMap<String, Object> studentData = new HashMap<>();
                        studentData.put("name", studentName);
                        studentData.put("enrollment", enrollmentNo);
                        studentData.put("timestamp", new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()));

                        attendanceRef.setValue(studentData)
                                .addOnSuccessListener(unused -> Toast.makeText(QRScannerActivity.this, "Attendance Marked!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> {});
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });

        } catch (Exception e) {}

        finish();
    }
}
