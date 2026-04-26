package com.example;

public class Student {

    private String studentID;
    private String firstName;

    public Student(String studentID, String firstName) {
        this.studentID = studentID;
        this.firstName = firstName;
    }

    // Getters
    public String getStudentID()  { return studentID; }
    public String getFirstName()  { return firstName; }

    // Setters
    public void setStudentID(String studentID) { this.studentID = studentID; }
    public void setFirstName(String firstName) { this.firstName = firstName; }


    public static boolean validateStudentID(String id) {
        return id != null && id.matches("\\d{8}");
    }

    @Override
    public String toString() {
        return studentID + "," + firstName;
    }
}
