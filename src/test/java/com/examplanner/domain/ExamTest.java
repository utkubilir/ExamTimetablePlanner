package com.examplanner.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class ExamTest {

    private Course course;
    private Classroom classroom;
    private ExamSlot slot;

    @BeforeEach
    void setUp() {
        course = new Course("CS101", "Programming", 120);
        classroom = new Classroom("A101", "Lecture Hall", 100);
        slot = new ExamSlot(LocalDate.of(2024, 12, 20), LocalTime.of(9, 0), LocalTime.of(11, 0));
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create exam with all parameters")
        void shouldCreateExamWithAllParameters() {
            Exam exam = new Exam(course, classroom, slot);

            assertEquals(course, exam.getCourse());
            assertEquals(classroom, exam.getClassroom());
            assertEquals(slot, exam.getSlot());
        }

        @Test
        @DisplayName("Should create exam with only course")
        void shouldCreateExamWithOnlyCourse() {
            Exam exam = new Exam(course);

            assertEquals(course, exam.getCourse());
            assertNull(exam.getClassroom());
            assertNull(exam.getSlot());
        }

        @Test
        @DisplayName("Should throw exception for null course in simple constructor")
        void shouldThrowExceptionForNullCourseSimple() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new Exam(null));
        }

        @Test
        @DisplayName("Should throw exception for null course in full constructor")
        void shouldThrowExceptionForNullCourseFull() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new Exam(null, classroom, slot));
        }

        @Test
        @DisplayName("Should throw exception for null classroom")
        void shouldThrowExceptionForNullClassroom() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new Exam(course, null, slot));
        }

        @Test
        @DisplayName("Should throw exception for null slot")
        void shouldThrowExceptionForNullSlot() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new Exam(course, classroom, null));
        }
    }

    @Nested
    @DisplayName("Setter Tests")
    class SetterTests {

        @Test
        @DisplayName("Should set classroom")
        void shouldSetClassroom() {
            Exam exam = new Exam(course);
            Classroom newClassroom = new Classroom("B202", "Lab", 50);

            exam.setClassroom(newClassroom);

            assertEquals(newClassroom, exam.getClassroom());
        }

        @Test
        @DisplayName("Should set slot")
        void shouldSetSlot() {
            Exam exam = new Exam(course);
            ExamSlot newSlot = new ExamSlot(LocalDate.of(2024, 12, 21), LocalTime.of(14, 0), LocalTime.of(16, 0));

            exam.setSlot(newSlot);

            assertEquals(newSlot, exam.getSlot());
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should return formatted string")
        void shouldReturnFormattedString() {
            Exam exam = new Exam(course, classroom, slot);
            String result = exam.toString();

            assertTrue(result.contains("CS101"));
            assertTrue(result.contains("A101") || result.contains("Lecture Hall"));
        }
    }
}
