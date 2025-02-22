package com.example.attendx;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.Result;
import me.dm7.barcodescanner.zxing.ZXingScannerView;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;

public class QRScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView scannerView;
    private DatabaseReference attendanceRef;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private String scannedData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            startQRScanner();
        }

        setupBiometricAuth();
    }

    private void startQRScanner() {
        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        scannedData = rawResult.getText();  // Store scanned data for attendance marking
        biometricPrompt.authenticate(promptInfo);
    }

    private void setupBiometricAuth() {
        biometricPrompt = new BiometricPrompt(this, ContextCompat.getMainExecutor(this), new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                markAttendance();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(QRScannerActivity.this, "Invalid fingerprint. Attendance not marked.", Toast.LENGTH_SHORT).show();
                restartScanner();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Fingerprint Authentication")
                .setNegativeButtonText("Cancel")
                .build();
    }

    private void markAttendance() {
        try {
            JSONObject qrData = new JSONObject(scannedData);
            String sessionId = qrData.getString("sessionId");
            String subject = qrData.getString("subject");
            String lectureType = qrData.getString("lectureType");
            String date = qrData.getString("date");

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

            attendanceRef = FirebaseDatabase.getInstance().getReference("attendance")
                    .child(date).child(subject).child(lectureType).child(sessionId).child("students");

            DatabaseReference userRef = attendanceRef.child(userId);
            userRef.child("timestamp").setValue(timestamp);

            Toast.makeText(this, "Attendance Marked!", Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Invalid QR Code!", Toast.LENGTH_SHORT).show();
        }

        restartScanner();
    }

    private void restartScanner() {
        scannerView.resumeCameraPreview(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scannerView.stopCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startQRScanner();
        } else {
            Toast.makeText(this, "Camera permission is required!", Toast.LENGTH_SHORT).show();
        }
    }
}
