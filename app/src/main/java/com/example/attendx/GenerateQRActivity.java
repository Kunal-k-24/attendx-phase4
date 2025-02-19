package com.example.attendx;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GenerateQRActivity extends AppCompatActivity {

    private Spinner spinnerLecture, spinnerLectureType, firstTimeSlotSpinner, secondTimeSlotSpinner;
    private LinearLayout timeSlotContainer;
    private ImageButton btnAddTimeSlot, btnRemoveTimeSlot;
    private Button buttonGenerateQR;
    private EditText editTextNumStudents;
    private TextView secondTimeSlotLabel;

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
        secondTimeSlotLabel = new TextView(this);

        // Set up dropdowns
        spinnerLecture.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, LECTURE_OPTIONS));
        spinnerLectureType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, LECTURE_TYPES));

        // Add first time slot dropdown
        addFirstTimeSlotDropdown();

        // Button Click Listeners
        btnAddTimeSlot.setOnClickListener(v -> addSecondTimeSlotDropdown());
        btnRemoveTimeSlot.setOnClickListener(v -> removeSecondTimeSlot());

        buttonGenerateQR.setOnClickListener(v -> generateQRCode());
    }

    private void generateQRCode() {
        String selectedLecture = spinnerLecture.getSelectedItem().toString();
        String selectedType = spinnerLectureType.getSelectedItem().toString();
        String numStudents = editTextNumStudents.getText().toString();

        if (numStudents.isEmpty()) {
            Toast.makeText(this, "Please enter the number of students", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> selectedTimeSlots = new ArrayList<>();
        selectedTimeSlots.add(firstTimeSlotSpinner.getSelectedItem().toString());

        if (secondTimeSlotSpinner != null && secondTimeSlotSpinner.getVisibility() == View.VISIBLE) {
            selectedTimeSlots.add(secondTimeSlotSpinner.getSelectedItem().toString());
        }

        String sessionId = UUID.randomUUID().toString();
        String timeSlotString = String.join(",", selectedTimeSlots);

        String currentDate = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());

        try {
            Intent intent = new Intent(this, QRDisplayActivity.class);
            intent.putExtra("sessionId", sessionId);
            intent.putExtra("date", currentDate);
            intent.putExtra("subject", selectedLecture);
            intent.putExtra("lectureType", selectedType);
            intent.putExtra("timeSlot", timeSlotString);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Error generating QR Code", Toast.LENGTH_SHORT).show();
        }
    }

    private void addFirstTimeSlotDropdown() {
        firstTimeSlotSpinner = new Spinner(this);
        firstTimeSlotSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, TIME_SLOTS));
        timeSlotContainer.addView(firstTimeSlotSpinner);

        // Automatically update the second time slot when first slot changes
        firstTimeSlotSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateSecondTimeSlot(); // Update second slot when first one is changed
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void addSecondTimeSlotDropdown() {
        if (secondTimeSlotSpinner == null) {
            secondTimeSlotLabel.setText("Second Time Slot (Next Available Hour)");
            timeSlotContainer.addView(secondTimeSlotLabel);

            secondTimeSlotSpinner = new Spinner(this);
            timeSlotContainer.addView(secondTimeSlotSpinner);
        }

        updateSecondTimeSlot();
    }

    private void updateSecondTimeSlot() {
        if (secondTimeSlotSpinner == null) return;

        int nextIndex = getNextTimeSlotIndex(firstTimeSlotSpinner.getSelectedItem().toString());

        if (nextIndex != -1) {
            String[] nextSlotArray = {TIME_SLOTS[nextIndex]};
            secondTimeSlotSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, nextSlotArray));
            secondTimeSlotSpinner.setVisibility(View.VISIBLE);
            btnRemoveTimeSlot.setVisibility(View.VISIBLE);
        } else {
            String[] noSlotArray = {"No time slot available"};
            secondTimeSlotSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, noSlotArray));
            secondTimeSlotSpinner.setVisibility(View.VISIBLE);
            btnRemoveTimeSlot.setVisibility(View.GONE);
        }
    }

    private void removeSecondTimeSlot() {
        if (secondTimeSlotSpinner != null) {
            timeSlotContainer.removeView(secondTimeSlotSpinner);
            timeSlotContainer.removeView(secondTimeSlotLabel);
            secondTimeSlotSpinner = null;
            btnRemoveTimeSlot.setVisibility(View.GONE);
        }
    }

    private int getNextTimeSlotIndex(String currentSlot) {
        for (int i = 0; i < TIME_SLOTS.length - 1; i++) {
            if (TIME_SLOTS[i].equals(currentSlot)) {
                return i + 1; // Next time slot
            }
        }
        return -1; // No available slot
    }
}
