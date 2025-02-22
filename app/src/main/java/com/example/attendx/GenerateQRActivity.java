package com.example.attendx;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.*;

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

    private boolean isSecondTimeSlotAdded = false;

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

        // Set up spinners
        spinnerLecture.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, LECTURE_OPTIONS));
        spinnerLectureType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, LECTURE_TYPES));

        addFirstTimeSlotDropdown();

        btnAddTimeSlot.setOnClickListener(v -> addSecondTimeSlotDropdown());
        btnRemoveTimeSlot.setOnClickListener(v -> removeSecondTimeSlot());
        buttonGenerateQR.setOnClickListener(v -> generateQRCode());
    }

    private void generateQRCode() {
        String sessionId = UUID.randomUUID().toString();
        String currentDate = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());

        Intent intent = new Intent(this, QRDisplayActivity.class);
        intent.putExtra("sessionId", sessionId);
        intent.putExtra("date", currentDate);
        intent.putExtra("subject", spinnerLecture.getSelectedItem().toString());
        intent.putExtra("lectureType", spinnerLectureType.getSelectedItem().toString());
        intent.putExtra("timeSlot", firstTimeSlotSpinner.getSelectedItem().toString());

        if (isSecondTimeSlotAdded) {
            intent.putExtra("secondTimeSlot", secondTimeSlotSpinner.getSelectedItem().toString());
        }

        startActivity(intent);
    }

    private void addFirstTimeSlotDropdown() {
        firstTimeSlotSpinner = new Spinner(this);
        firstTimeSlotSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, TIME_SLOTS));
        timeSlotContainer.addView(firstTimeSlotSpinner);

        firstTimeSlotSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isSecondTimeSlotAdded) {
                    updateSecondTimeSlot(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void addSecondTimeSlotDropdown() {
        if (isSecondTimeSlotAdded) return; // Prevent adding more than one second time slot

        int selectedIndex = firstTimeSlotSpinner.getSelectedItemPosition();
        if (selectedIndex >= TIME_SLOTS.length - 1) {
            Toast.makeText(this, "No available next time slot!", Toast.LENGTH_SHORT).show();
            return;
        }

        secondTimeSlotLabel.setText("Next Time Slot:");
        secondTimeSlotSpinner = new Spinner(this);
        updateSecondTimeSlot(selectedIndex);

        timeSlotContainer.addView(secondTimeSlotLabel);
        timeSlotContainer.addView(secondTimeSlotSpinner);

        btnRemoveTimeSlot.setVisibility(View.VISIBLE);
        isSecondTimeSlotAdded = true;
    }

    private void updateSecondTimeSlot(int firstSlotIndex) {
        if (firstSlotIndex < TIME_SLOTS.length - 1) {
            ArrayAdapter<String> secondSlotAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                    Collections.singletonList(TIME_SLOTS[firstSlotIndex + 1]));
            secondTimeSlotSpinner.setAdapter(secondSlotAdapter);
        }
    }

    private void removeSecondTimeSlot() {
        if (!isSecondTimeSlotAdded) return;

        timeSlotContainer.removeView(secondTimeSlotLabel);
        timeSlotContainer.removeView(secondTimeSlotSpinner);
        secondTimeSlotSpinner = null;

        btnRemoveTimeSlot.setVisibility(View.GONE);
        isSecondTimeSlotAdded = false;
    }
}
