package com.example.attendx;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.*;
import java.util.*;

public class GetAttendanceActivity extends AppCompatActivity {

    private Button btnSelectDate, btnFetchAttendance;
    private TextView tvSelectedDate, tvStudentCount;
    private Spinner spinnerSubject, spinnerLectureType;
    private ListView listViewAttendance;
    private ArrayAdapter<String> subjectAdapter, lectureTypeAdapter;
    private List<String> studentList = new ArrayList<>();
    private ArrayAdapter<String> studentAdapter;
    private String selectedDate = "", selectedSubject = "", selectedLectureType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_attendance);

        // Initialize UI components
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnFetchAttendance = findViewById(R.id.btnFetchAttendance);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvStudentCount = findViewById(R.id.tvStudentCount);
        spinnerSubject = findViewById(R.id.spinnerSubject);
        spinnerLectureType = findViewById(R.id.spinnerLectureType);
        listViewAttendance = findViewById(R.id.listViewAttendance);

        // Initialize adapters for dropdowns
        subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList("JPR", "DCN", "MIC", "EES", "UID", "MML"));
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubject.setAdapter(subjectAdapter);

        lectureTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Arrays.asList("Lecture", "Practical", "Special Lecture"));
        lectureTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLectureType.setAdapter(lectureTypeAdapter);

        studentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, studentList);
        listViewAttendance.setAdapter(studentAdapter);

        // Date Picker
        btnSelectDate.setOnClickListener(v -> openDatePicker());

        // Fetch Attendance
        btnFetchAttendance.setOnClickListener(v -> fetchAttendance());
    }

    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            selectedDate = selectedYear + "-" + String.format("%02d", (selectedMonth + 1)) + "-" + String.format("%02d", selectedDay);
            tvSelectedDate.setText(selectedDate);
        }, year, month, day);

        // Disable future dates
        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    private void fetchAttendance() {
        selectedSubject = spinnerSubject.getSelectedItem().toString();
        selectedLectureType = spinnerLectureType.getSelectedItem().toString();

        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference attendanceRef = FirebaseDatabase.getInstance().getReference("attendance")
                .child(selectedDate)
                .child(selectedSubject)
                .child(selectedLectureType);

        attendanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentList.clear();
                int count = 0;

                for (DataSnapshot sessionSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot studentSnapshot : sessionSnapshot.child("students").getChildren()) {
                        String enrollment = studentSnapshot.child("enrollment").getValue(String.class);
                        String name = studentSnapshot.child("name").getValue(String.class);

                        if (enrollment != null && name != null) {
                            studentList.add(name + " (" + enrollment + ")");
                            count++;
                        }
                    }
                }

                tvStudentCount.setText("Total Present: " + count);
                tvStudentCount.setVisibility(View.VISIBLE);
                studentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GetAttendanceActivity.this, "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
