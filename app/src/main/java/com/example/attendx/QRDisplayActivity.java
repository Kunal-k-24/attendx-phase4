package com.example.attendx;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class QRDisplayActivity extends AppCompatActivity {

    private ImageView imageViewQR;
    private Button btnSubmitAttendance;
    private String qrData, date, subject, lectureType, sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_display);

        imageViewQR = findViewById(R.id.imageViewQR);
        btnSubmitAttendance = findViewById(R.id.btnSubmitAttendance);

        // ✅ Get data from Intent
        Intent intent = getIntent();
        qrData = intent.getStringExtra("qrData");
        date = intent.getStringExtra("date");
        subject = intent.getStringExtra("subject");
        lectureType = intent.getStringExtra("lectureType");
        sessionId = intent.getStringExtra("sessionId");

        if (qrData == null || sessionId == null || date == null || subject == null || lectureType == null) {
            Log.e("QRDisplayActivity", "Missing necessary data!");
            Toast.makeText(this, "Error: Missing data for QR generation!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        generateQRCode(qrData);

        btnSubmitAttendance.setOnClickListener(v -> {
            Intent submitIntent = new Intent(QRDisplayActivity.this, SubmitAttendanceActivity.class);
            submitIntent.putExtra("date", date);
            submitIntent.putExtra("subject", subject);
            submitIntent.putExtra("lectureType", lectureType);
            submitIntent.putExtra("sessionId", sessionId);
            startActivity(submitIntent);
        });
    }

    private void generateQRCode(String data) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap qrBitmap = barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, 400, 400);
            imageViewQR.setImageBitmap(qrBitmap);
        } catch (WriterException e) {
            Log.e("QRDisplayActivity", "Error generating QR Code", e);
            Toast.makeText(this, "Failed to generate QR Code", Toast.LENGTH_SHORT).show();
        }
    }
}
