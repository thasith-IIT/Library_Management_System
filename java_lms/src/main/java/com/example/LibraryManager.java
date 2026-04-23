package com.example;

import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core business logic for the Library Management System.
 * Handles data loading, searching, reporting, and exporting.
 * CM1601 - Library Management System
 */
public class LibraryManager {

    private List<Book>        books        = new ArrayList<>();
    private List<Student>     students     = new ArrayList<>();
    private List<Transaction> transactions = new ArrayList<>();

    // ══════════════════════════════════════════════
    // IMPORT CSV FILES
    // ══════════════════════════════════════════════

    /**
     * Import all three CSV files. Returns a list of validation error messages.
     */
    public List<String> importCSVFiles(String bookPath, String studentPath, String transPath) {
        List<String> errors = new ArrayList<>();
        books.clear();
        students.clear();
        transactions.clear();

        errors.addAll(loadBooks(bookPath));
        errors.addAll(loadStudents(studentPath));
        errors.addAll(loadTransactions(transPath));

        return errors;
    }

    private List<String> loadBooks(String path) {
        List<String> errors = new ArrayList<>();
        File f = new File(path);
        if (!f.exists()) { errors.add("Book file not found: " + path); return errors; }

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String header = br.readLine(); // skip header
            String line;
            int lineNum = 1;
            while ((line = br.readLine()) != null) {
                lineNum++;
                String[] parts = line.split(",", -1);
                if (parts.length < 7) {
                    errors.add("Book line " + lineNum + ": insufficient columns -> " + line);
                    continue;
                }
                String bookID    = parts[0].trim();
                String title     = parts[1].trim();
                String author    = parts[2].trim();
                String isbn13    = parts[3].trim();
                String copiesStr = parts[4].trim();
                String availStr  = parts[5].trim();
                String priceStr  = parts[6].trim();

                // Validate Student ID format in book file (N/A), validate ISBN-13
                if (!Book.validateISBN(isbn13)) {
                    errors.add("Book line " + lineNum + ": Invalid ISBN-13 [" + isbn13 + "] for book " + bookID);
                }

                int copies = 0, avail = 0;
                double price = 0.0;
                try { copies = Integer.parseInt(copiesStr); } catch (NumberFormatException e) {
                    errors.add("Book line " + lineNum + ": Invalid copies value [" + copiesStr + "]");
                    continue;
                }
                try { avail = Integer.parseInt(availStr); } catch (NumberFormatException e) {
                    errors.add("Book line " + lineNum + ": Invalid availability value [" + availStr + "]");
                    continue;
                }
                try { price = Double.parseDouble(priceStr); } catch (NumberFormatException e) {
                    errors.add("Book line " + lineNum + ": Invalid price value [" + priceStr + "]");
                    continue;
                }

                if (copies < 0 || copies > 2) {
                    errors.add("Book line " + lineNum + ": Invalid copies count [" + copies + "] — must be 0-2");
                }
                if (avail < 0) {
                    errors.add("Book line " + lineNum + ": Invalid availability [" + avail + "] — cannot be negative");
                }
                if (avail > copies) {
                    errors.add("Book line " + lineNum + ": Availability [" + avail + "] exceeds copies [" + copies + "]");
                }

                books.add(new Book(bookID, title, author, isbn13, copies, avail, price));
            }
        } catch (IOException e) {
            errors.add("Error reading book file: " + e.getMessage());
        }
        return errors;
    }

    private List<String> loadStudents(String path) {
        List<String> errors = new ArrayList<>();
        File f = new File(path);
        if (!f.exists()) { errors.add("Student file not found: " + path); return errors; }

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            br.readLine(); // skip header
            String line;
            int lineNum = 1;
            while ((line = br.readLine()) != null) {
                lineNum++;
                String[] parts = line.split(",", -1);
                if (parts.length < 2) {
                    errors.add("Student line " + lineNum + ": insufficient columns -> " + line);
                    continue;
                }
                String id   = parts[0].trim();
                String name = parts[1].trim();
                if (!Student.validateStudentID(id)) {
                    errors.add("Student line " + lineNum + ": Invalid Student ID [" + id + "]");
                }
                students.add(new Student(id, name));
            }
        } catch (IOException e) {
            errors.add("Error reading student file: " + e.getMessage());
        }
        return errors;
    }

    private List<String> loadTransactions(String path) {
        List<String> errors = new ArrayList<>();
        File f = new File(path);
        if (!f.exists()) { errors.add("Transaction file not found: " + path); return errors; }

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            br.readLine(); // skip header
            String line;
            int lineNum = 1;
            while ((line = br.readLine()) != null) {
                lineNum++;
                String[] parts = line.split(",", -1);
                if (parts.length < 4) {
                    errors.add("Transaction line " + lineNum + ": insufficient columns -> " + line);
                    continue;
                }
                String date      = parts[0].trim();
                String bookID    = parts[1].trim();
                String studentID = parts[2].trim();
                String typeStr   = parts[3].trim();

                if (!Transaction.validateDate(date)) {
                    errors.add("Transaction line " + lineNum + ": Invalid date [" + date + "]");
                }
                int type = 0;
                try { type = Integer.parseInt(typeStr); } catch (NumberFormatException e) {
                    errors.add("Transaction line " + lineNum + ": Invalid type [" + typeStr + "]");
                    continue;
                }
                transactions.add(new Transaction(date, bookID, studentID, type));
            }
        } catch (IOException e) {
            errors.add("Error reading transaction file: " + e.getMessage());
        }
        return errors;
    }

    // ══════════════════════════════════════════════
    // REPORTS
    // ══════════════════════════════════════════════

    /**
     * Returns list of transactions (type=1, issue) for a given date.
     */
    public List<Transaction> getBooksIssuedByDate(String date) {
        return transactions.stream()
                .filter(t -> t.getType() == 1 && t.getDate().equals(date))
                .collect(Collectors.toList());
    }

    /**
     * Calculates the average price of all books.
     */
    public double calculateAverageCost() {
        if (books.isEmpty()) return 0.0;
        return books.stream().mapToDouble(Book::getPrice).average().orElse(0.0);
    }

    /**
     * Search books by title. Supports wildcard '*' at the end.
     * e.g. "Java*" matches any title starting with "Java"
     */
    public List<Book> searchByTitle(String query) {
        String q = query.trim().toLowerCase();
        if (q.endsWith("*")) {
            String prefix = q.substring(0, q.length() - 1);
            return books.stream()
                    .filter(b -> b.getTitle().toLowerCase().startsWith(prefix))
                    .collect(Collectors.toList());
        }
        return books.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    /**
     * Export search results to a text file.
     */
    public void exportSearchResults(List<Book> results, String fileName) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            bw.write("BookID,Title,Author,ISBN-13,Copies,Available,Price");
            bw.newLine();
            for (Book b : results) {
                bw.write(b.toString());
                bw.newLine();
            }
        }
    }

    // ── Accessors ─────────────────────────────
    public List<Book>        getBooks()        { return books; }
    public List<Student>     getStudents()     { return students; }
    public List<Transaction> getTransactions() { return transactions; }

    /**
     * Update a book record in the list (used after inline editing during import).
     */
    public void updateBook(int index, Book updated) {
        if (index >= 0 && index < books.size()) {
            books.set(index, updated);
        }
    }

    /**
     * Update a student record in the list.
     */
    public void updateStudent(int index, Student updated) {
        if (index >= 0 && index < students.size()) {
            students.set(index, updated);
        }
    }
}
