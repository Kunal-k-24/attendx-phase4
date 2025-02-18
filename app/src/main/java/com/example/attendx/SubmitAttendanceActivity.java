package com.example.attendx;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SubmitAttendanceActivity extends AppCompatActivity {
    private ListView studentListView;
    private Button submitButton, addStudentButton;
    private CheckBox selectAllCheckbox;
    private StudentAdapter studentAdapter;
    private DatabaseReference attendanceRef;
    private List<Student> studentList = new ArrayList<>();
    private String date, subject, lectureType, sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_attendance);

        studentListView = findViewById(R.id.studentListView);
        submitButton = findViewById(R.id.btnSubmitFinal);
        addStudentButton = findViewById(R.id.btnAddStudent);
        selectAllCheckbox = findViewById(R.id.checkBoxSelectAll);

        // ✅ Get data from Intent with null checks
        date = getIntent().getStringExtra("date");
        subject = getIntent().getStringExtra("subject");
        lectureType = getIntent().getStringExtra("lectureType");
        sessionId = getIntent().getStringExtra("sessionId");

        if (date == null || subject == null || lectureType == null || sessionId == null) {
            Toast.makeText(this, "Error: Missing required data!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // ✅ Initialize Firebase reference
        attendanceRef = FirebaseDatabase.getInstance().getReference("attendance")
                .child(date)
                .child(subject)
                .child(lectureType)
                .child(sessionId)
                .child("students");

        // ✅ Initialize adapter
        studentAdapter = new StudentAdapter(this, studentList);
        studentListView.setAdapter(studentAdapter);

        fetchScannedStudents();

        // ✅ Select/Deselect all checkbox functionality
        selectAllCheckbox.setOnClickListener(v -> {
            boolean isChecked = selectAllCheckbox.isChecked();
            for (Student student : studentList) {
                student.setSelected(isChecked);
            }
            studentAdapter.notifyDataSetChanged();
        });

        // ✅ Submit attendance button
        submitButton.setOnClickListener(v -> submitFinalAttendance());

        // ✅ Add student button
        addStudentButton.setOnClickListener(v -> showAddStudentDialog());
    }

    private void fetchScannedStudents() {
        attendanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                studentList.clear();
                for (DataSnapshot studentSnapshot : dataSnapshot.getChildren()) {
                    String studentId = studentSnapshot.getKey();
                    String studentName = studentSnapshot.child("name").getValue(String.class);
                    String enrollmentNo = studentSnapshot.child("enrollment").getValue(String.class); // ✅ Fix: Use "enrollment"

                    if (studentId != null && studentName != null && enrollmentNo != null) {
                        studentList.add(new Student(studentId, studentName, enrollmentNo, true));
                    }
                }

                if (studentList.isEmpty()) {
                    Toast.makeText(SubmitAttendanceActivity.this, "No students scanned yet!", Toast.LENGTH_SHORT).show();
                }

                studentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SubmitAttendanceActivity.this, "Failed to fetch attendance", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitFinalAttendance() {
        HashMap<String, Object> finalAttendanceList = new HashMap<>();

        for (Student student : studentList) {
            if (student.isSelected()) {
                HashMap<String, String> studentData = new HashMap<>();
                studentData.put("name", student.getName());
                studentData.put("enrollment", student.getEnrollmentNo());
                finalAttendanceList.put(student.getId(), studentData);
            }
        }

        // ✅ Remove deselected students and update Firebase
        attendanceRef.setValue(finalAttendanceList)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SubmitAttendanceActivity.this, "Attendance submitted successfully!", Toast.LENGTH_SHORT).show();

                    // ✅ Redirect to Teacher Dashboard
                    Intent intent = new Intent(SubmitAttendanceActivity.this, TeacherDashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish(); // ✅ Close current activity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SubmitAttendanceActivity.this, "Failed to submit attendance!", Toast.LENGTH_SHORT).show();
                });
    }


    private void showAddStudentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_student, null);
        builder.setView(dialogView);

        EditText editTextName = dialogView.findViewById(R.id.editTextStudentName);
        EditText editTextEnrollment = dialogView.findViewById(R.id.editTextStudentEnrollment);

        builder.setTitle("Add Student");
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = editTextName.getText().toString().trim();
                String enrollment = editTextEnrollment.getText().toString().trim();

                if (name.isEmpty() || enrollment.isEmpty()) {
                    Toast.makeText(SubmitAttendanceActivity.this, "All fields are required!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Generate a unique ID for manually added students
                String studentId = "manual_" + System.currentTimeMillis();
                Student newStudent = new Student(studentId, name, enrollment, true);
                studentList.add(newStudent);
                studentAdapter.notifyDataSetChanged();

                // ✅ Add student to Firebase
                HashMap<String, String> studentData = new HashMap<>();
                studentData.put("name", name);
                studentData.put("enrollment", enrollment);
                attendanceRef.child(studentId).setValue(studentData);

                Toast.makeText(SubmitAttendanceActivity.this, "Student added!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
