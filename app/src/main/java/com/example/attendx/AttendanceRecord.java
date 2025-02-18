package com.example.attendx;

public class AttendanceRecord {
    public String studentID, studentName, lectureName, timeSlot;

    public AttendanceRecord() {
        // Default constructor required for Firebase
    }

    public AttendanceRecord(String studentID, String studentName, String lectureName, String timeSlot) {
        this.studentID = studentID;
        this.studentName = studentName;
        this.lectureName = lectureName;
        this.timeSlot = timeSlot;
    }
}
