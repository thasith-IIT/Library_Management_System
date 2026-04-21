import unittest
from library import (
    validate_book_id,
    validate_isbn13,
    validate_student_id,
    validate_date,
    validate_title,
    validate_first_name,
)


class TestValidateBookID(unittest.TestCase):
    def test_valid_book_id(self):
        self.assertTrue(validate_book_id("AB12"))

    def test_valid_book_id_lowercase(self):
        self.assertTrue(validate_book_id("ab12"))

    def test_invalid_all_digits(self):
        self.assertFalse(validate_book_id("1234"))

    def test_invalid_all_letters(self):
        self.assertFalse(validate_book_id("ABCD"))

    def test_invalid_too_short(self):
        self.assertFalse(validate_book_id("A1"))

    def test_invalid_too_long(self):
        self.assertFalse(validate_book_id("AB123"))

    def test_invalid_wrong_order(self):
        self.assertFalse(validate_book_id("12AB"))


class TestValidateISBN13(unittest.TestCase):
    def test_valid_isbn(self):
        # Known valid ISBN-13
        self.assertTrue(validate_isbn13("9780306406157"))

    def test_valid_isbn_with_dashes(self):
        self.assertTrue(validate_isbn13("978-0-306-40615-7"))

    def test_invalid_check_digit(self):
        self.assertFalse(validate_isbn13("9780306406158"))

    def test_invalid_length(self):
        self.assertFalse(validate_isbn13("97803064"))

    def test_invalid_non_numeric(self):
        self.assertFalse(validate_isbn13("978030640615X"))

    def test_another_valid_isbn(self):
        self.assertTrue(validate_isbn13("9781861972712"))


class TestValidateStudentID(unittest.TestCase):
    def test_valid_student_id(self):
        self.assertTrue(validate_student_id("12345678"))

    def test_invalid_too_short(self):
        self.assertFalse(validate_student_id("1234567"))

    def test_invalid_too_long(self):
        self.assertFalse(validate_student_id("123456789"))

    def test_invalid_letters(self):
        self.assertFalse(validate_student_id("1234567A"))

    def test_invalid_empty(self):
        self.assertFalse(validate_student_id(""))


class TestValidateDate(unittest.TestCase):
    def test_valid_date(self):
        self.assertTrue(validate_date("01/01/2024"))

    def test_invalid_format_dashes(self):
        self.assertFalse(validate_date("01-01-2024"))

    def test_invalid_day(self):
        self.assertFalse(validate_date("32/01/2024"))

    def test_invalid_month(self):
        self.assertFalse(validate_date("01/13/2024"))

    def test_invalid_no_leading_zero(self):
        self.assertFalse(validate_date("1/1/2024"))

    def test_invalid_empty(self):
        self.assertFalse(validate_date(""))


class TestValidateTitle(unittest.TestCase):
    def test_valid_title(self):
        self.assertTrue(validate_title("Python Basics"))

    def test_valid_single_word(self):
        self.assertTrue(validate_title("Java"))

    def test_invalid_numbers(self):
        self.assertFalse(validate_title("Book 101"))

    def test_invalid_too_long(self):
        self.assertFalse(validate_title("A" * 21))

    def test_valid_max_length(self):
        self.assertTrue(validate_title("A" * 20))

    def test_invalid_empty(self):
        self.assertFalse(validate_title(""))


class TestValidateFirstName(unittest.TestCase):
    def test_valid_name(self):
        self.assertTrue(validate_first_name("Alice"))

    def test_invalid_with_space(self):
        self.assertFalse(validate_first_name("Alice Bob"))

    def test_invalid_with_number(self):
        self.assertFalse(validate_first_name("Alice1"))

    def test_invalid_too_long(self):
        self.assertFalse(validate_first_name("A" * 11))

    def test_valid_max_length(self):
        self.assertTrue(validate_first_name("A" * 10))

    def test_invalid_empty(self):
        self.assertFalse(validate_first_name(""))


if __name__ == "__main__":
    unittest.main(verbosity=2)
