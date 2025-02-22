package com.example.attendx;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import com.google.zxing.Result;
import com.google.firebase.database.*;
import me.dm7.barcodescanner.zxing.ZXingScannerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;

public class QRScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView scannerView;
    private DatabaseReference attendanceRef;
    private String studentID = "12345";
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);

        attendanceRef = FirebaseDatabase.getInstance().getReference("attendance");

        setupBiometricAuthentication();
    }

    @Override
    public void handleResult(Result rawResult) {
        String scannedData = rawResult.getText();
        Log.d("QRScanner", "Scanned Data: " + scannedData);

        if (validateQRCode(scannedData)) {
            authenticateWithFingerprint(scannedData);
        } else {
            Toast.makeText(this, "Invalid QR Code!", Toast.LENGTH_SHORT).show();
            scannerView.resumeCameraPreview(this);
        }
    }

    private boolean validateQRCode(String qrData) {
        try {
            String[] qrParts = qrData.split(";");
            String sessionId = qrParts[0];
            String deviceID = qrParts[1];
            long timestamp = Long.parseLong(qrParts[2]);

            String currentDeviceID = android.provider.Settings.Secure.getString(getContentResolver(),
                    android.provider.Settings.Secure.ANDROID_ID);

            long currentTime = System.currentTimeMillis();
            if (!deviceID.equals(currentDeviceID) || (currentTime - timestamp) > 7000) {
                return false; // QR code expired or generated from another device
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void setupBiometricAuthentication() {
        BiometricManager biometricManager = BiometricManager.from(this);
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG
                | BiometricManager.Authenticators.DEVICE_CREDENTIAL) != BiometricManager.BIOMETRIC_SUCCESS) {
            Toast.makeText(this, "Biometric authentication not available", Toast.LENGTH_LONG).show();
            return;
        }

        Executor executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(QRScannerActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                Toast.makeText(QRScannerActivity.this, "Authentication successful", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailed() {
                Toast.makeText(QRScannerActivity.this, "Fingerprint not recognized", Toast.LENGTH_SHORT).show();
                scannerView.resumeCameraPreview(QRScannerActivity.this);
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Fingerprint Authentication")
                .setSubtitle("Verify your identity to submit attendance")
                .setNegativeButtonText("Cancel")
                .build();
    }

    private void authenticateWithFingerprint(String qrData) {
        biometricPrompt.authenticate(promptInfo);
    }

    private void markAttendance(String qrData) {
        String[] qrParts = qrData.split(";");
        String sessionId = qrParts[0];

        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String subject = "Unknown"; // Fetch from session

        DatabaseReference studentRef = attendanceRef.child(date).child(subject).child("students").child(studentID);
        studentRef.child("timestamp").setValue(System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(QRScannerActivity.this, "Attendance Marked Successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(QRScannerActivity.this, "Error Marking Attendance!", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onResume() {
        super.onResume();
        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }
}
