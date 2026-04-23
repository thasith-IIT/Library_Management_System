package com.example;

/**
 * Represents a Book entity.
 * CM1601 - Library Management System
 */
public class Book {

    private String bookID;
    private String title;
    private String author;
    private String isbn13;
    private int copies;
    private int available;
    private double price;

    public Book(String bookID, String title, String author, String isbn13,
                int copies, int available, double price) {
        this.bookID    = bookID;
        this.title     = title;
        this.author    = author;
        this.isbn13    = isbn13;
        this.copies    = copies;
        this.available = available;
        this.price     = price;
    }

    // ── Getters ──────────────────────────────
    public String getBookID()    { return bookID; }
    public String getTitle()     { return title; }
    public String getAuthor()    { return author; }
    public String getIsbn13()    { return isbn13; }
    public int    getCopies()    { return copies; }
    public int    getAvailable() { return available; }
    public double getPrice()     { return price; }

    // ── Setters ──────────────────────────────
    public void setBookID(String bookID)       { this.bookID    = bookID; }
    public void setTitle(String title)         { this.title     = title; }
    public void setAuthor(String author)       { this.author    = author; }
    public void setIsbn13(String isbn13)       { this.isbn13    = isbn13; }
    public void setCopies(int copies)          { this.copies    = copies; }
    public void setAvailable(int available)    { this.available = available; }
    public void setPrice(double price)         { this.price     = price; }

    /**
     * Validate ISBN-13 check digit using the standard algorithm.
     */
    public static boolean validateISBN(String isbn) {
        isbn = isbn.replace("-", "").replace(" ", "");
        if (isbn.length() != 13) return false;
        for (char c : isbn.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        int total = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(isbn.charAt(i));
            total += (i % 2 == 0) ? digit : digit * 3;
        }
        int check = (10 - (total % 10)) % 10;
        return check == Character.getNumericValue(isbn.charAt(12));
    }

    @Override
    public String toString() {
        return bookID + "," + title + "," + author + "," + isbn13 + ","
                + copies + "," + available + "," + String.format("%.2f", price);
    }
}
