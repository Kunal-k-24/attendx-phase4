package com.example.attendx;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import org.json.JSONException;
import org.json.JSONObject;

public class QRDisplayActivity extends AppCompatActivity {
    private ImageView imageViewQR;
    private Button btnSubmitAttendance;
    private String sessionId, date, subject, lectureType, timeSlot;
    private Handler qrHandler = new Handler();
    private boolean isActive = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_display);

        imageViewQR = findViewById(R.id.imageViewQR);
        btnSubmitAttendance = findViewById(R.id.btnSubmitAttendance);

        Intent intent = getIntent();
        sessionId = intent.getStringExtra("sessionId");
        date = intent.getStringExtra("date");
        subject = intent.getStringExtra("subject");
        lectureType = intent.getStringExtra("lectureType");
        timeSlot = intent.getStringExtra("timeSlot");

        startQRRefresh();

        btnSubmitAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSubmitAttendanceScreen();
            }
        });
    }

    private void startQRRefresh() {
        qrHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isActive) {
                    updateQRCode();
                    qrHandler.postDelayed(this, 5000);
                }
            }
        });
    }

    private void updateQRCode() {
        try {
            JSONObject qrJson = new JSONObject();
            qrJson.put("sessionId", sessionId);
            qrJson.put("subject", subject);
            qrJson.put("lectureType", lectureType);
            qrJson.put("timeSlot", timeSlot);
            qrJson.put("date", date);
            qrJson.put("token", System.currentTimeMillis());

            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap qrBitmap = barcodeEncoder.encodeBitmap(qrJson.toString(), BarcodeFormat.QR_CODE, 400, 400);
            imageViewQR.setImageBitmap(qrBitmap);
        } catch (WriterException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void openSubmitAttendanceScreen() {
        Intent intent = new Intent(QRDisplayActivity.this, SubmitAttendanceActivity.class);
        intent.putExtra("sessionId", sessionId);
        intent.putExtra("date", date);
        intent.putExtra("subject", subject);
        intent.putExtra("lectureType", lectureType);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActive = false;
        qrHandler.removeCallbacksAndMessages(null);
    }
}
