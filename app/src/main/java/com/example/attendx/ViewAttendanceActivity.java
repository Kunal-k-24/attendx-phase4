package com.example.attendx;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewAttendanceActivity extends AppCompatActivity {
    private ListView listViewAttendance;
    private DatabaseReference attendanceRef;
    private FirebaseUser currentUser;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> attendanceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attendance);

        listViewAttendance = findViewById(R.id.listViewAttendance);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        attendanceRef = FirebaseDatabase.getInstance().getReference("Attendance").child(currentUser.getUid());

        attendanceList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, attendanceList);
        listViewAttendance.setAdapter(adapter);

        fetchAttendance();
    }

    private void fetchAttendance() {
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("ViewAttendance", "Fetching attendance for User ID: " + currentUser.getUid());

        attendanceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                attendanceList.clear();

                if (!snapshot.exists()) {
                    Toast.makeText(ViewAttendanceActivity.this, "No attendance records found.", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (DataSnapshot recordSnapshot : snapshot.getChildren()) {
                    try {
                        String date = recordSnapshot.child("date").getValue(String.class);
                        String time = recordSnapshot.child("time").getValue(String.class);
                        String status = recordSnapshot.child("status").getValue(String.class);

                        if (date != null && time != null && status != null) {
                            String recordText = date + " at " + time + ": " + status;
                            Log.d("ViewAttendance", "Record: " + recordText);
                            attendanceList.add(recordText);
                        } else {
                            Log.e("ViewAttendance", "Invalid record format in database: " + recordSnapshot.toString());
                        }
                    } catch (Exception e) {
                        Log.e("ViewAttendance", "Error parsing record: " + e.getMessage());
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ViewAttendance", "Failed to load attendance: " + error.getMessage());
                Toast.makeText(ViewAttendanceActivity.this, "Failed to load attendance", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
