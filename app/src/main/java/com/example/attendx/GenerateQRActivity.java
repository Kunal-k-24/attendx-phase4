package com.example.attendx;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GenerateQRActivity extends AppCompatActivity {

    private Spinner spinnerLecture, spinnerLectureType;
    private LinearLayout timeSlotContainer;
    private ImageButton btnAddTimeSlot, btnRemoveTimeSlot;
    private Button buttonGenerateQR;
    private EditText editTextNumStudents;
    private List<Spinner> timeSlotSpinners = new ArrayList<>();

    // Dropdown options
    private static final String[] LECTURE_OPTIONS = {"JPR", "DCN", "MIC", "MML", "EES", "UID"};
    private static final String[] LECTURE_TYPES = {"Lecture", "Practical", "Special Lecture"};
    private static final String[] TIME_SLOTS = {
            "10:15 AM - 11:15 AM", "11:15 AM - 12:15 PM",
            "1:00 PM - 2:00 PM", "2:00 PM - 3:00 PM",
            "3:15 PM - 4:15 PM", "4:15 PM - 5:15 PM"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_qr);

        // Initialize UI elements
        spinnerLecture = findViewById(R.id.spinnerLecture);
        spinnerLectureType = findViewById(R.id.spinnerLectureType);
        timeSlotContainer = findViewById(R.id.timeSlotContainer);
        btnAddTimeSlot = findViewById(R.id.btnAddTimeSlot);
        btnRemoveTimeSlot = findViewById(R.id.btnRemoveTimeSlot);
        buttonGenerateQR = findViewById(R.id.buttonGenerateQR);
        editTextNumStudents = findViewById(R.id.editTextNumStudents);

        // Set up Dropdowns
        spinnerLecture.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, LECTURE_OPTIONS));
        spinnerLectureType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, LECTURE_TYPES));

        // Add first time slot dropdown
        addTimeSlotDropdown();

        // Button Click Listeners
        btnAddTimeSlot.setOnClickListener(v -> {
            if (timeSlotSpinners.size() < 2) addTimeSlotDropdown();
            else Toast.makeText(this, "Maximum 2 time slots allowed", Toast.LENGTH_SHORT).show();
        });

        btnRemoveTimeSlot.setOnClickListener(v -> {
            if (timeSlotSpinners.size() > 1) removeTimeSlot();
        });

        buttonGenerateQR.setOnClickListener(v -> generateQRCode());
    }

    private void generateQRCode() {
        String selectedLecture = spinnerLecture.getSelectedItem().toString();
        String selectedType = spinnerLectureType.getSelectedItem().toString();
        String numStudents = editTextNumStudents.getText().toString();
        List<String> selectedTimeSlots = new ArrayList<>();

        for (Spinner spinner : timeSlotSpinners) {
            selectedTimeSlots.add(spinner.getSelectedItem().toString());
        }

        if (numStudents.isEmpty()) {
            Toast.makeText(this, "Please enter the number of students", Toast.LENGTH_SHORT).show();
            return;
        }

        String sessionId = UUID.randomUUID().toString();
        String timeSlotString = String.join(",", selectedTimeSlots);

        // Get today's date in "YYYY-MM-DD" format
        String currentDate = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());

        try {
            JSONObject qrDataJson = new JSONObject();
            qrDataJson.put("sessionId", sessionId);
            qrDataJson.put("subject", selectedLecture);
            qrDataJson.put("lectureType", selectedType);
            qrDataJson.put("timeSlot", timeSlotString);
            qrDataJson.put("date", currentDate);

            String qrData = qrDataJson.toString();
            Log.d("GenerateQRActivity", "QR Data: " + qrData);  // ✅ Debugging

            Intent intent = new Intent(this, QRDisplayActivity.class);
            intent.putExtra("qrData", qrData);
            intent.putExtra("sessionId", sessionId);
            intent.putExtra("date", currentDate);  // ✅ Add date
            intent.putExtra("subject", selectedLecture);
            intent.putExtra("lectureType", selectedType);
            intent.putExtra("timeSlot", timeSlotString);

            startActivity(intent);

        } catch (Exception e) {
            Toast.makeText(this, "Error generating QR Code", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    private void addTimeSlotDropdown() {
        Spinner timeSlotSpinner = new Spinner(this);
        timeSlotSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, TIME_SLOTS));

        timeSlotContainer.addView(timeSlotSpinner);
        timeSlotSpinners.add(timeSlotSpinner);

        btnRemoveTimeSlot.setVisibility(timeSlotSpinners.size() > 1 ? View.VISIBLE : View.GONE);
    }

    private void removeTimeSlot() {
        if (!timeSlotSpinners.isEmpty()) {
            timeSlotContainer.removeView(timeSlotSpinners.remove(timeSlotSpinners.size() - 1));
        }
        btnRemoveTimeSlot.setVisibility(timeSlotSpinners.size() > 1 ? View.VISIBLE : View.GONE);
    }
}
