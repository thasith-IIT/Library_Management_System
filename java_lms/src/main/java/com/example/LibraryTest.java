package com.example;

import org.junit.Test;
import org.junit.jupiter.api.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

/**
 * JUnit 5 Unit Tests for Library Management System - Java Module
 * CM1601 - Library Management System
 */
public class LibraryTest {

    // Book.validateISBN Tests

    @Test
    @DisplayName("Valid ISBN-13 should return true")
    public void testValidISBN13() {
        assertTrue(Book.validateISBN("9780306406157"));
    }

    @Test
    @DisplayName("Valid ISBN-13 with dashes should return true")
    public void testValidISBNWithDashes() {
        assertTrue(Book.validateISBN("978-0-306-40615-7"));
    }

    @Test
    @DisplayName("ISBN-13 with wrong check digit should return false")
    public void testInvalidISBNCheckDigit() {
        assertFalse(Book.validateISBN("9780306406158"));
    }

    @Test
    @DisplayName("ISBN-13 with wrong length should return false")
    public void testInvalidISBNLength() {
        assertFalse(Book.validateISBN("978030640"));
    }

    @Test
    @DisplayName("ISBN-13 with non-digit characters should return false")
    public void testInvalidISBNNonDigit() {
        assertFalse(Book.validateISBN("978030640615X"));
    }

    @Test
    @DisplayName("Another valid ISBN-13 should return true")
    public void testAnotherValidISBN() {
        assertTrue(Book.validateISBN("9781861972712"));
    }

    // Student.validateStudentID Tests

    @Test
    @DisplayName("Valid 8-digit student ID should return true")
    public void testValidStudentID() {
        assertTrue(Student.validateStudentID("12345678"));
    }

    @Test
    @DisplayName("Student ID with fewer than 8 digits should return false")
    public void testStudentIDTooShort() {
        assertFalse(Student.validateStudentID("1234567"));
    }

    @Test
    @DisplayName("Student ID with more than 8 digits should return false")
    public void testStudentIDTooLong() {
        assertFalse(Student.validateStudentID("123456789"));
    }

    @Test
    @DisplayName("Student ID with letters should return false")
    public void testStudentIDWithLetters() {
        assertFalse(Student.validateStudentID("1234567A"));
    }

    @Test
    @DisplayName("Null student ID should return false")
    public void testNullStudentID() {
        assertFalse(Student.validateStudentID(null));
    }

    // Transaction.validateDate Tests

    @Test
    @DisplayName("Valid date DD/MM/YYYY should return true")
    public void testValidDate() {
        assertTrue(Transaction.validateDate("01/04/2026"));
    }

    @Test
    @DisplayName("Date with dashes instead of slashes should return false")
    public void testInvalidDateFormat() {
        assertFalse(Transaction.validateDate("01-04-2026"));
    }

    @Test
    @DisplayName("Invalid day (32) should return false")
    public void testInvalidDay() {
        assertFalse(Transaction.validateDate("32/04/2026"));
    }

    @Test
    @DisplayName("Invalid month (13) should return false")
    public void testInvalidMonth() {
        assertFalse(Transaction.validateDate("01/13/2026"));
    }

    @Test
    @DisplayName("Null date should return false")
    public void testNullDate() {
        assertFalse(Transaction.validateDate(null));
    }

    // LibraryManager - Business Logic Tests

    @Test
    @DisplayName("calculateAverageCost returns 0 when no books loaded")
    public void testAverageCostEmpty() {
        LibraryManager lm = new LibraryManager();
        assertEquals(0.0, lm.calculateAverageCost(), 0.001);
    }

    @Test
    @DisplayName("calculateAverageCost returns correct average")
    public void testAverageCostCorrect() {
        LibraryManager lm = new LibraryManager();
        lm.getBooks().add(new Book("AB12", "Title One", "Author", "9780306406157", 2, 2, 20.00));
        lm.getBooks().add(new Book("CD34", "Title Two", "Author", "9781861972712", 1, 1, 40.00));
        assertEquals(30.00, lm.calculateAverageCost(), 0.001);
    }

    @Test
    @DisplayName("searchByTitle finds exact match")
    public void testSearchByTitleExact() {
        LibraryManager lm = new LibraryManager();
        lm.getBooks().add(new Book("AB12", "Java Basics", "Author", "9780306406157", 2, 2, 20.00));
        lm.getBooks().add(new Book("CD34", "Python Guide", "Author", "9781861972712", 1, 1, 30.00));
        List<Book> results = lm.searchByTitle("Java Basics");
        assertEquals(1, results.size());
        assertEquals("AB12", results.get(0).getBookID());
    }

    @Test
    @DisplayName("searchByTitle with wildcard finds matching books")
    public void testSearchByTitleWildcard() {
        LibraryManager lm = new LibraryManager();
        lm.getBooks().add(new Book("AB12", "Java Basics",    "Author", "9780306406157", 2, 2, 20.00));
        lm.getBooks().add(new Book("CD34", "Java Advanced",  "Author", "9781861972712", 1, 1, 30.00));
        lm.getBooks().add(new Book("EF56", "Python Guide",   "Author", "9780132350884", 2, 2, 25.00));
        List<Book> results = lm.searchByTitle("Java*");
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("searchByTitle returns empty list for no match")
    public void testSearchByTitleNoMatch() {
        LibraryManager lm = new LibraryManager();
        lm.getBooks().add(new Book("AB12", "Java Basics", "Author", "9780306406157", 2, 2, 20.00));
        List<Book> results = lm.searchByTitle("Haskell");
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("getBooksIssuedByDate returns correct transactions")
    public void testGetBooksIssuedByDate() {
        LibraryManager lm = new LibraryManager();
        lm.getTransactions().add(new Transaction("10/04/2026", "AB12", "12345678", 1));
        lm.getTransactions().add(new Transaction("10/04/2026", "CD34", "87654321", 1));
        lm.getTransactions().add(new Transaction("11/04/2026", "EF56", "11223344", 1));
        lm.getTransactions().add(new Transaction("10/04/2026", "AB12", "12345678", 2)); // return
        List<Transaction> results = lm.getBooksIssuedByDate("10/04/2026");
        assertEquals(2, results.size()); // only type=1
    }
}
