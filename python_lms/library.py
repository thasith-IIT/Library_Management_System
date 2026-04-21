import csv
import os
import re
from datetime import datetime

BOOK_FILE = "book.csv"
STUDENT_FILE = "student.csv"
TRANSACTION_FILE = "transactions.csv"


# Validations

def validate_book_id(book_id):
    """Book ID: exactly 2 letters followed by 2 digits (e.g. AB12)."""
    return bool(re.fullmatch(r'[A-Za-z]{2}\d{2}', book_id))


def validate_isbn13(isbn):
    """Validate ISBN-13 check digit using the standard algorithm."""
    isbn = isbn.replace("-", "").replace(" ", "")
    if len(isbn) != 13 or not isbn.isdigit():
        return False
    total = 0
    for i, digit in enumerate(isbn[:12]):
        weight = 1 if i % 2 == 0 else 3
        total += int(digit) * weight
    check = (10 - (total % 10)) % 10
    return check == int(isbn[12])


def validate_student_id(student_id):
    # Student ID: exactly 8 digits
    return bool(re.fullmatch(r'\d{8}', student_id))


def validate_date(date_str):
    # Date must be in DD/MM/YYYY format
    if not re.fullmatch(r'\d{2}/\d{2}/\d{4}', date_str):
        return False
    try:
        datetime.strptime(date_str, "%d/%m/%Y")
        return True
    except ValueError:
        return False


def validate_title(title):
    # Title: letters and spaces only, max 20 characters
    return bool(re.fullmatch(r'[A-Za-z ]{1,20}', title))


def validate_first_name(name):
    # First name: letters only, max 10 characters
    return bool(re.fullmatch(r'[A-Za-z]{1,10}', name))


# CSV Files

def load_books():
    # Load books from book.csv
    books = []
    if not os.path.exists(BOOK_FILE):
        return books
    with open(BOOK_FILE, newline='') as f:
        reader = csv.DictReader(f)
        for row in reader:
            books.append(row)
    return books


def save_books(books):
    # Save all books to book.csv
    fieldnames = ["bookID", "title", "author", "isbn13", "copies", "available", "price"]
    with open(BOOK_FILE, 'w', newline='') as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(books)


def load_students():
    # Load students from student.csv
    students = []
    if not os.path.exists(STUDENT_FILE):
        return students
    with open(STUDENT_FILE, newline='') as f:
        reader = csv.DictReader(f)
        for row in reader:
            students.append(row)
    return students


def save_students(students):
    # Save all students to student.csv
    fieldnames = ["studentID", "firstName"]
    with open(STUDENT_FILE, 'w', newline='') as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(students)


def load_transactions():
    # Load transactions from transactions.csv
    transactions = []
    if not os.path.exists(TRANSACTION_FILE):
        return transactions
    with open(TRANSACTION_FILE, newline='') as f:
        reader = csv.DictReader(f)
        for row in reader:
            transactions.append(row)
    return transactions


def save_transactions(transactions):
    # Save all transactions to transactions.csv
    fieldnames = ["date", "bookID", "studentID", "type"]
    with open(TRANSACTION_FILE, 'w', newline='') as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(transactions)


def append_transaction(date, book_id, student_id, t_type):
    # Append a single transaction record
    fieldnames = ["date", "bookID", "studentID", "type"]
    file_exists = os.path.exists(TRANSACTION_FILE)
    with open(TRANSACTION_FILE, 'a', newline='') as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        if not file_exists or os.path.getsize(TRANSACTION_FILE) == 0:
            writer.writeheader()
        writer.writerow({"date": date, "bookID": book_id,
                         "studentID": student_id, "type": t_type})


# Menu Functions

def print_header(title):
    print("\n" + "=" * 50)
    print(f"  {title}")
    print("=" * 50)


# 1. Add / Edit Book

def add_book():
    print_header("ADD NEW BOOK")
    books = load_books()

    # Book ID
    while True:
        book_id = input("Enter Book ID (2 letters + 2 digits, e.g. AB12): ").strip().upper()
        if not validate_book_id(book_id):
            print("  [!] Invalid Book ID format. Must be 2 letters followed by 2 digits.")
            continue
        if any(b["bookID"].upper() == book_id for b in books):
            print("  [!] Book ID already exists.")
            continue
        break

    # ISBN-13
    while True:
        isbn = input("Enter ISBN-13 (13 digits): ").strip()
        if not validate_isbn13(isbn):
            print("  [!] Invalid ISBN-13. Check digit validation failed.")
            continue
        break

    # Title
    while True:
        title = input("Enter Title (letters/spaces, max 20 chars): ").strip()
        if not validate_title(title):
            print("  [!] Invalid title. Letters and spaces only, max 20 characters.")
            continue
        break

    # Author
    while True:
        author = input("Enter Author name (letters/spaces, max 20 chars): ").strip()
        if not re.fullmatch(r'[A-Za-z ]{1,20}', author):
            print("  [!] Invalid author name. Letters and spaces only, max 20 characters.")
            continue
        break

    # Copies
    while True:
        copies_str = input("Enter number of copies (1 or 2): ").strip()
        if copies_str not in ("1", "2"):
            print("  [!] Copies must be 1 or 2.")
            continue
        copies = int(copies_str)
        break

    # Price
    while True:
        price_str = input("Enter Price (e.g. 12.99): ").strip()
        try:
            price = round(float(price_str), 2)
            if price < 0:
                raise ValueError
            break
        except ValueError:
            print("  [!] Invalid price. Enter a positive number with up to 2 decimal places.")

    book = {
        "bookID": book_id,
        "title": title,
        "author": author,
        "isbn13": isbn,
        "copies": str(copies),
        "available": str(copies),
        "price": f"{price:.2f}"
    }
    books.append(book)
    save_books(books)
    print(f"\n  [✓] Book '{title}' added successfully!")


def edit_book():
    print_header("EDIT / UPDATE BOOK")
    books = load_books()
    if not books:
        print("  No books found.")
        return

    book_id = input("Enter Book ID to edit: ").strip().upper()
    match = next((b for b in books if b["bookID"].upper() == book_id), None)
    if not match:
        print("  [!] Book ID not found.")
        return

    print(f"\n  Current record: {match}")
    print("  Press ENTER to keep current value.\n")

    # Title
    new_title = input(f"  Title [{match['title']}]: ").strip()
    if new_title:
        if validate_title(new_title):
            match["title"] = new_title
        else:
            print("  [!] Invalid title. Keeping original.")

    # Author
    new_author = input(f"  Author [{match['author']}]: ").strip()
    if new_author:
        if re.fullmatch(r'[A-Za-z ]{1,20}', new_author):
            match["author"] = new_author
        else:
            print("  [!] Invalid author. Keeping original.")

    # ISBN
    new_isbn = input(f"  ISBN-13 [{match['isbn13']}]: ").strip()
    if new_isbn:
        if validate_isbn13(new_isbn):
            match["isbn13"] = new_isbn
        else:
            print("  [!] Invalid ISBN-13. Keeping original.")

    # Copies
    new_copies = input(f"  Copies [{match['copies']}]: ").strip()
    if new_copies:
        if new_copies in ("1", "2"):
            match["copies"] = new_copies
            # Adjust available if needed
            if int(match["available"]) > int(new_copies):
                match["available"] = new_copies
        else:
            print("  [!] Invalid copies. Keeping original.")

    # Price
    new_price = input(f"  Price [{match['price']}]: ").strip()
    if new_price:
        try:
            price = round(float(new_price), 2)
            if price >= 0:
                match["price"] = f"{price:.2f}"
            else:
                print("  [!] Invalid price. Keeping original.")
        except ValueError:
            print("  [!] Invalid price. Keeping original.")

    save_books(books)
    print("  [✓] Book updated successfully!")


def view_books():
    print_header("VIEW ALL BOOKS")
    books = load_books()
    if not books:
        print("  No books found.")
        return
    print(f"  {'ID':<6} {'Title':<22} {'Author':<20} {'ISBN-13':<15} {'Copies':<8} {'Avail':<7} {'Price'}")
    print("  " + "-" * 85)
    for b in books:
        print(f"  {b['bookID']:<6} {b['title']:<22} {b['author']:<20} {b['isbn13']:<15} "
              f"{b['copies']:<8} {b['available']:<7} £{b['price']}")


def search_books():
    print_header("SEARCH BOOKS BY TITLE")
    keyword = input("Enter title keyword to search: ").strip().lower()
    books = load_books()
    results = [b for b in books if keyword in b["title"].lower()]
    if not results:
        print("  No matching books found.")
        return
    print(f"\n  Found {len(results)} result(s):")
    for b in results:
        print(f"  {b['bookID']} | {b['title']} | {b['author']} | Available: {b['available']}/{b['copies']}")


# 2. Add Student

def add_student():
    print_header("ADD STUDENT")
    students = load_students()

    while True:
        student_id = input("Enter Student ID (8 digits): ").strip()
        if not validate_student_id(student_id):
            print("  [!] Invalid Student ID. Must be exactly 8 digits.")
            continue
        if any(s["studentID"] == student_id for s in students):
            print("  [!] Student ID already exists.")
            continue
        break

    while True:
        first_name = input("Enter First Name (letters only, max 10 chars): ").strip()
        if not validate_first_name(first_name):
            print("  [!] Invalid name. Letters only, max 10 characters.")
            continue
        break

    students.append({"studentID": student_id, "firstName": first_name})
    save_students(students)
    print(f"  [✓] Student '{first_name}' added successfully!")


def view_students():
    print_header("VIEW ALL STUDENTS")
    students = load_students()
    if not students:
        print("  No students found.")
        return
    print(f"  {'Student ID':<12} {'First Name'}")
    print("  " + "-" * 25)
    for s in students:
        print(f"  {s['studentID']:<12} {s['firstName']}")


# 3. Issue Book

def issue_book():
    print_header("ISSUE BOOK")
    books = load_books()
    students = load_students()

    # Book ID input
    book_id = input("Enter Book ID: ").strip().upper()
    book = next((b for b in books if b["bookID"].upper() == book_id), None)
    if not book:
        print("  [!] Invalid Book ID.")
        return

    if int(book["available"]) <= 0:
        print("  [!] Book Already Issued (no copies available).")
        return

    # Student ID input
    student_id = input("Enter Student ID: ").strip()
    student = next((s for s in students if s["studentID"] == student_id), None)
    if not student:
        print("  [!] Invalid Student ID.")
        return

    # Date input
    while True:
        date = input("Enter Issue Date (DD/MM/YYYY): ").strip()
        if not validate_date(date):
            print("  [!] Invalid date format. Use DD/MM/YYYY.")
            continue
        break

    # Update availability
    book["available"] = str(int(book["available"]) - 1)
    save_books(books)

    # Record transaction (type 1 = issue)
    append_transaction(date, book_id, student_id, "1")
    print(f"  [✓] Book '{book['title']}' issued to {student['firstName']} on {date}.")


# 4. Return Book

def return_book():
    print_header("RETURN BOOK")
    books = load_books()
    students = load_students()

    book_id = input("Enter Book ID: ").strip().upper()
    book = next((b for b in books if b["bookID"].upper() == book_id), None)
    if not book:
        print("  [!] Invalid Book ID.")
        return

    student_id = input("Enter Student ID: ").strip()
    student = next((s for s in students if s["studentID"] == student_id), None)
    if not student:
        print("  [!] Invalid Student ID.")
        return

    # Check if book is actually issued (available < copies)
    if int(book["available"]) >= int(book["copies"]):
        print("  [!] Book Already Returned (availability equals total copies).")
        return

    while True:
        date = input("Enter Return Date (DD/MM/YYYY): ").strip()
        if not validate_date(date):
            print("  [!] Invalid date format. Use DD/MM/YYYY.")
            continue
        break

    book["available"] = str(int(book["available"]) + 1)
    save_books(books)

    # Record transaction (type 2 = return)
    append_transaction(date, book_id, student_id, "2")
    print(f"  [✓] Book '{book['title']}' returned by {student['firstName']} on {date}.")


# 5. Trend Graph

def show_trend_graph():
    print_header("BOOKS ISSUED BY DATE - TREND GRAPH")
    transactions = load_transactions()

    if not transactions:
        print("  No transaction data available.")
        return

    # Filter only issue transactions (type == "1")
    issue_txns = [t for t in transactions if t["type"] == "1"]
    if not issue_txns:
        print("  No issue transactions found.")
        return

    try:
        import pandas as pd
        import plotly.express as px

        df = pd.DataFrame(issue_txns)
        df["date"] = pd.to_datetime(df["date"], format="%d/%m/%Y")
        count_by_date = df.groupby("date").size().reset_index(name="Books Issued")
        count_by_date = count_by_date.sort_values("date")

        fig = px.bar(
            count_by_date,
            x="date",
            y="Books Issued",
            title="Books Issued by Date",
            labels={"date": "Date", "Books Issued": "Number of Books Issued"},
            color_discrete_sequence=["#4A90D9"]
        )
        fig.update_layout(xaxis_tickformat="%d/%m/%Y")
        fig.show()
        print("  [✓] Graph displayed in browser.")

    except ImportError as e:
        print(f"  [!] Required library not installed: {e}")
        print("  Please install: pip install pandas plotly")
        # Fallback: text-based summary
        from collections import Counter
        date_counts = Counter(t["date"] for t in issue_txns)
        print("\n  Date-based issue summary (text fallback):")
        for d in sorted(date_counts):
            bar = "█" * date_counts[d]
            print(f"  {d}: {bar} ({date_counts[d]})")


# Main Menu

def main_menu():
    while True:
        print_header("LIBRARY MANAGEMENT SYSTEM")
        print("  1. Add Book")
        print("  2. Edit / Update Book")
        print("  3. View All Books")
        print("  4. Search Books by Title")
        print("  5. Add Student")
        print("  6. View All Students")
        print("  7. Issue Book")
        print("  8. Return Book")
        print("  9. View Trend Graph (Books Issued by Date)")
        print("  0. Exit")
        print("-" * 50)
        choice = input("  Enter your choice: ").strip()

        if choice == "1":
            add_book()
        elif choice == "2":
            edit_book()
        elif choice == "3":
            view_books()
        elif choice == "4":
            search_books()
        elif choice == "5":
            add_student()
        elif choice == "6":
            view_students()
        elif choice == "7":
            issue_book()
        elif choice == "8":
            return_book()
        elif choice == "9":
            show_trend_graph()
        elif choice == "0":
            print("\n  Goodbye!\n")
            break
        else:
            print("  [!] Invalid choice. Please enter 0-9.")


if __name__ == "__main__":
    main_menu()
