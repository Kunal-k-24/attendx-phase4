package com.example.attendx;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;
import java.util.Arrays;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameField, enrollmentField, collegeEmailField, rollField, personalEmailField, passwordField;
    private Button registerButton;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register); // Ensure this is the correct XML file

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Link UI elements correctly
        nameField = findViewById(R.id.editTextName);
        enrollmentField = findViewById(R.id.editTextEnrollment);
        collegeEmailField = findViewById(R.id.editTextCollegeEmail);
        rollField = findViewById(R.id.editTextRollNo); // FIXED: Changed to match XML
        personalEmailField = findViewById(R.id.editTextPersonalEmail);
        passwordField = findViewById(R.id.editTextPassword);
        registerButton = findViewById(R.id.buttonRegister);

        // Register Button Click Event
        registerButton.setOnClickListener(v -> {
            String name = nameField.getText().toString().trim();
            String enrollment = enrollmentField.getText().toString().trim();
            String collegeEmail = collegeEmailField.getText().toString().trim();
            String rollNumber = rollField.getText().toString().trim();
            String personalEmail = personalEmailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (name.isEmpty() || enrollment.isEmpty() || collegeEmail.isEmpty() || rollNumber.isEmpty() || personalEmail.isEmpty() || password.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
            } else {
                registerUser(name, enrollment, collegeEmail, rollNumber, personalEmail, password);
            }
        });
    }

    private void registerUser(String name, String enrollment, String collegeEmail, String rollNumber, String personalEmail, String password) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        auth.createUserWithEmailAndPassword(collegeEmail, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String userId = auth.getCurrentUser().getUid();

                // Check if email belongs to a teacher
                List<String> allowedTeacherEmails = Arrays.asList(
                        "teacher1@college.edu",
                        "teacher2@college.edu"
                );
                String role = allowedTeacherEmails.contains(collegeEmail) ? "teacher" : "student";

                // Save user data with role
                HashMap<String, Object> userMap = new HashMap<>();
                userMap.put("name", name);
                userMap.put("enrollment", enrollment);
                userMap.put("collegeEmail", collegeEmail);
                userMap.put("rollNumber", rollNumber);
                userMap.put("personalEmail", personalEmail);
                userMap.put("role", role);  // Store role

                databaseReference.child(userId).setValue(userMap).addOnCompleteListener(saveTask -> {
                    if (saveTask.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Failed to save user data!", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(RegisterActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}
