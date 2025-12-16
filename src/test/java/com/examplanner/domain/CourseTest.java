package com.examplanner.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CourseTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create course with valid parameters")
        void shouldCreateCourseWithValidParameters() {
            Course course = new Course("CS101", "Introduction to Programming", 120);

            assertEquals("CS101", course.getCode());
            assertEquals("Introduction to Programming", course.getName());
            assertEquals(120, course.getExamDurationMinutes());
        }

        @Test
        @DisplayName("Should trim whitespace from code and name")
        void shouldTrimWhitespace() {
            Course course = new Course("  CS101  ", "  Intro  ", 60);

            assertEquals("CS101", course.getCode());
            assertEquals("Intro", course.getName());
        }

        @Test
        @DisplayName("Should throw exception for null code")
        void shouldThrowExceptionForNullCode() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Course(null, "Test Course", 60));
            assertTrue(exception.getMessage().contains("code"));
        }

        @Test
        @DisplayName("Should throw exception for empty code")
        void shouldThrowExceptionForEmptyCode() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Course("", "Test Course", 60));
            assertTrue(exception.getMessage().contains("code"));
        }

        @Test
        @DisplayName("Should throw exception for whitespace-only code")
        void shouldThrowExceptionForWhitespaceCode() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Course("   ", "Test Course", 60));
            assertTrue(exception.getMessage().contains("code"));
        }

        @Test
        @DisplayName("Should throw exception for null name")
        void shouldThrowExceptionForNullName() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Course("CS101", null, 60));
            assertTrue(exception.getMessage().contains("name"));
        }

        @Test
        @DisplayName("Should throw exception for empty name")
        void shouldThrowExceptionForEmptyName() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Course("CS101", "", 60));
            assertTrue(exception.getMessage().contains("name"));
        }

        @Test
        @DisplayName("Should throw exception for zero duration")
        void shouldThrowExceptionForZeroDuration() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Course("CS101", "Test", 0));
            assertTrue(exception.getMessage().contains("duration"));
        }

        @Test
        @DisplayName("Should throw exception for negative duration")
        void shouldThrowExceptionForNegativeDuration() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Course("CS101", "Test", -30));
            assertTrue(exception.getMessage().contains("duration"));
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should return code and name when different")
        void shouldReturnCodeAndNameWhenDifferent() {
            Course course = new Course("CS101", "Programming", 60);
            assertEquals("CS101 - Programming", course.toString());
        }

        @Test
        @DisplayName("Should return only code when name equals code")
        void shouldReturnOnlyCodeWhenNameEqualsCode() {
            Course course = new Course("CS101", "CS101", 60);
            assertEquals("CS101", course.toString());
        }
    }
}
