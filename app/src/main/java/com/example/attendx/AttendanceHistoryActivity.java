package com.example.attendx;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AttendanceHistoryActivity extends AppCompatActivity {

    private ListView listViewAttendance;
    private ArrayList<String> attendanceList;
    private ArrayAdapter<String> adapter;
    private DatabaseReference attendanceRef;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_history);

        listViewAttendance = findViewById(R.id.listViewAttendance);
        attendanceList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, attendanceList);
        listViewAttendance.setAdapter(adapter);

        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String userId = user.getUid();
            attendanceRef = FirebaseDatabase.getInstance().getReference("Attendance").child(userId);

            attendanceRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    attendanceList.clear();
                    for (DataSnapshot data : snapshot.getChildren()) {
                        String record = data.getValue(String.class);
                        attendanceList.add(record);
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Handle error
                }
            });
        }
    }
}
