package com.example.attendx;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AttendanceActivity extends AppCompatActivity {

    private Button markAttendanceButton;
    private DatabaseReference attendanceRef;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        // Initialize Firebase
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            attendanceRef = FirebaseDatabase.getInstance().getReference("Attendance").child(user.getUid());
        }

        // Initialize button
        markAttendanceButton = findViewById(R.id.buttonMarkAttendance);

        // Mark attendance when button is clicked
        markAttendanceButton.setOnClickListener(v -> markAttendance());
    }

    private void markAttendance() {
        if (user != null) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

            // Store attendance in Firebase
            DatabaseReference newEntry = attendanceRef.push();
            newEntry.child("timestamp").setValue(timestamp);
            newEntry.child("status").setValue("Present")
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(AttendanceActivity.this, "Attendance Marked Successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AttendanceActivity.this, "Failed to mark attendance!", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(AttendanceActivity.this, "User not logged in!", Toast.LENGTH_SHORT).show();
        }
    }
}
