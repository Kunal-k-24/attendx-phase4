package com.example.attendx;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameField, enrollmentField, collegeEmailField, rollField, personalEmailField, passwordField;
    private Button registerButton;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Link UI elements
        nameField = findViewById(R.id.registerFullName);
        enrollmentField = findViewById(R.id.registerEnrollment);
        collegeEmailField = findViewById(R.id.registerCollegeEmail);
        rollField = findViewById(R.id.registerRollNo);
        personalEmailField = findViewById(R.id.registerPersonalEmail);
        passwordField = findViewById(R.id.registerPassword);
        registerButton = findViewById(R.id.buttonRegister);

        // Register Button Click Event
        registerButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String name = nameField.getText().toString().trim();
        String enrollment = enrollmentField.getText().toString().trim();
        String collegeEmail = collegeEmailField.getText().toString().trim();
        String rollNumber = rollField.getText().toString().trim();
        String personalEmail = personalEmailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        // Validate Input Fields
        if (name.isEmpty() || enrollment.isEmpty() || collegeEmail.isEmpty() ||
                rollNumber.isEmpty() || personalEmail.isEmpty() || password.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase Authentication - Create User
        mAuth.createUserWithEmailAndPassword(collegeEmail, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                if (firebaseUser != null) {
                    String userId = firebaseUser.getUid();

                    // Save user data to Firebase Database
                    HashMap<String, Object> userMap = new HashMap<>();
                    userMap.put("name", name);
                    userMap.put("enrollment", enrollment);
                    userMap.put("collegeEmail", collegeEmail);
                    userMap.put("rollNumber", rollNumber);
                    userMap.put("personalEmail", personalEmail);
                    userMap.put("role", "student");  // Only students can register

                    databaseReference.child(userId).setValue(userMap).addOnCompleteListener(saveTask -> {
                        if (saveTask.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Failed to save user data!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                Toast.makeText(RegisterActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
