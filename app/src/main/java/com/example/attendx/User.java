package com.example.attendx;

public class User {
    public String userId, name, enrollment, collegeEmail, roll, personalEmail;

    // Empty constructor required for Firebase
    public User() { }

    public User(String userId, String name, String enrollment, String collegeEmail, String roll, String personalEmail) {
        this.userId = userId;
        this.name = name;
        this.enrollment = enrollment;
        this.collegeEmail = collegeEmail;
        this.roll = roll;
        this.personalEmail = personalEmail;
    }
}
