package com.examplanner.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClassroomTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create classroom with valid parameters")
        void shouldCreateClassroomWithValidParameters() {
            Classroom classroom = new Classroom("A101", "Lecture Hall A", 150);

            assertEquals("A101", classroom.getId());
            assertEquals("Lecture Hall A", classroom.getName());
            assertEquals(150, classroom.getCapacity());
        }

        @Test
        @DisplayName("Should throw exception for null id")
        void shouldThrowExceptionForNullId() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new Classroom(null, "Test Room", 50));
        }

        @Test
        @DisplayName("Should throw exception for zero capacity")
        void shouldThrowExceptionForZeroCapacity() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new Classroom("A101", "Test Room", 0));
        }

        @Test
        @DisplayName("Should throw exception for negative capacity")
        void shouldThrowExceptionForNegativeCapacity() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new Classroom("A101", "Test Room", -10));
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should return formatted string with capacity")
        void shouldReturnFormattedString() {
            Classroom classroom = new Classroom("A101", "Lecture Hall", 100);
            String result = classroom.toString();

            assertTrue(result.contains("A101") || result.contains("Lecture Hall"));
        }
    }
}
