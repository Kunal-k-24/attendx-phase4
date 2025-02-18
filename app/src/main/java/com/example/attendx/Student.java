package com.example.attendx;

public class Student {
    private String id;
    private String name;
    private String enrollmentNo;
    private boolean isSelected;

    public Student() {
        // Default constructor required for Firebase
    }

    public Student(String id, String name, String enrollmentNo, boolean isSelected) {
        this.id = id;
        this.name = name;
        this.enrollmentNo = enrollmentNo;
        this.isSelected = isSelected;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEnrollmentNo() {
        return enrollmentNo;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
