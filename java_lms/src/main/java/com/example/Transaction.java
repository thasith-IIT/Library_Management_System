package com.example;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Represents a Transaction entity.
 * type 1 = issue, type 2 = return
 * CM1601 - Library Management System
 */
public class Transaction {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private String date;
    private String bookID;
    private String studentID;
    private int    type;   // 1 = issue, 2 = return

    public Transaction(String date, String bookID, String studentID, int type) {
        this.date      = date;
        this.bookID    = bookID;
        this.studentID = studentID;
        this.type      = type;
    }

    // ── Getters ──────────────────────────────
    public String getDate()      { return date; }
    public String getBookID()    { return bookID; }
    public String getStudentID() { return studentID; }
    public int    getType()      { return type; }

    // ── Setters ──────────────────────────────
    public void setDate(String date)           { this.date      = date; }
    public void setBookID(String bookID)       { this.bookID    = bookID; }
    public void setStudentID(String studentID) { this.studentID = studentID; }
    public void setType(int type)              { this.type      = type; }

    /**
     * Validate date string in DD/MM/YYYY format.
     */
    public static boolean validateDate(String date) {
        if (date == null || !date.matches("\\d{2}/\\d{2}/\\d{4}")) return false;
        try {
            LocalDate.parse(date, FMT);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return date + "," + bookID + "," + studentID + "," + type;
    }
}
