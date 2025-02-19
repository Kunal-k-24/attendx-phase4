package com.example.attendx;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.zxing.Result;
import me.dm7.barcodescanner.zxing.ZXingScannerView;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.text.SimpleDateFormat;
import java.util.Date;

public class QRScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView scannerView;
    private DatabaseReference usersRef, attendanceRef;
    private String userId, studentName, enrollmentNo;
    private boolean isStudentDataLoaded = false;
    private int failedAttempts = 0;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        fetchStudentData();
        setupBiometricAuth();
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

    private void setupBiometricAuth() {
        Executor executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                checkIfAlreadyMarked();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                failedAttempts++;
                if (failedAttempts >= 4) {
                    Toast.makeText(QRScannerActivity.this, "Invalid fingerprint. Attendance not marked.", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Fingerprint Authentication")
                .setSubtitle("Use your fingerprint to mark attendance")
                .setNegativeButtonText("Cancel")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .build();
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

            checkBiometricSupport();

        } catch (Exception e) {
            finish();
        }
    }

    private void checkBiometricSupport() {
        BiometricManager biometricManager = BiometricManager.from(this);
        if (biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
            biometricPrompt.authenticate(promptInfo);
        } else {
            Toast.makeText(QRScannerActivity.this, "Please mark attendance manually.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void checkIfAlreadyMarked() {
        attendanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(QRScannerActivity.this, "Attendance already marked.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    markAttendance();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                finish();
            }
        });
    }

    private void markAttendance() {
        HashMap<String, Object> studentData = new HashMap<>();
        studentData.put("name", studentName);
        studentData.put("enrollment", enrollmentNo);
        studentData.put("timestamp", new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()));

        attendanceRef.setValue(studentData)
                .addOnSuccessListener(unused -> Toast.makeText(QRScannerActivity.this, "Attendance Marked!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(QRScannerActivity.this, "Failed to mark attendance.", Toast.LENGTH_SHORT).show());

        finish();
    }
}
