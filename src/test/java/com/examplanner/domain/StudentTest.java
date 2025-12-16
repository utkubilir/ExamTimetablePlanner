package com.examplanner.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class StudentTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create student with valid parameters")
        void shouldCreateStudentWithValidParameters() {
            Student student = new Student("2021001", "Ali Yılmaz");

            assertEquals("2021001", student.getId());
            assertEquals("Ali Yılmaz", student.getName());
        }

        @Test
        @DisplayName("Should throw exception for null id")
        void shouldThrowExceptionForNullId() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new Student(null, "Test Student"));
        }

        @Test
        @DisplayName("Should throw exception for empty id")
        void shouldThrowExceptionForEmptyId() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new Student("", "Test Student"));
        }

        @Test
        @DisplayName("Should throw exception for null name")
        void shouldThrowExceptionForNullName() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new Student("123", null));
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should return formatted string")
        void shouldReturnFormattedString() {
            Student student = new Student("2021001", "Ali Yılmaz");
            String result = student.toString();

            assertTrue(result.contains("2021001"));
            assertTrue(result.contains("Ali Yılmaz"));
        }
    }
}
