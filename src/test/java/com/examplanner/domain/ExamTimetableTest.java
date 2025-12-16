package com.examplanner.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class ExamTimetableTest {

    private Course course1;
    private Course course2;
    private Course course3;
    private Classroom classroom;
    private Student student1;
    private Student student2;

    @BeforeEach
    void setUp() {
        course1 = new Course("CS101", "Programming", 60);
        course2 = new Course("CS102", "Data Structures", 60);
        course3 = new Course("CS103", "Algorithms", 60);
        classroom = new Classroom("A101", "Hall", 100);
        student1 = new Student("S001", "Ali");
        student2 = new Student("S002", "Ayse");
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create empty timetable")
        void shouldCreateEmptyTimetable() {
            ExamTimetable timetable = new ExamTimetable();

            assertNotNull(timetable.getExams());
            assertTrue(timetable.getExams().isEmpty());
        }

        @Test
        @DisplayName("Should create timetable with exams list")
        void shouldCreateTimetableWithExamsList() {
            Exam exam = new Exam(course1, classroom,
                    new ExamSlot(LocalDate.of(2024, 12, 20), LocalTime.of(9, 0), LocalTime.of(10, 0)));
            List<Exam> exams = new ArrayList<>();
            exams.add(exam);

            ExamTimetable timetable = new ExamTimetable(exams);

            assertEquals(1, timetable.getExams().size());
        }

        @Test
        @DisplayName("Should create timetable with exams and enrollments")
        void shouldCreateTimetableWithExamsAndEnrollments() {
            Exam exam = new Exam(course1, classroom,
                    new ExamSlot(LocalDate.of(2024, 12, 20), LocalTime.of(9, 0), LocalTime.of(10, 0)));
            List<Exam> exams = new ArrayList<>();
            exams.add(exam);
            List<Enrollment> enrollments = List.of(new Enrollment(student1, course1));

            ExamTimetable timetable = new ExamTimetable(exams, enrollments);

            assertEquals(1, timetable.getExams().size());
        }
    }

    @Nested
    @DisplayName("Add Exam Tests")
    class AddExamTests {

        @Test
        @DisplayName("Should add exam to timetable")
        void shouldAddExamToTimetable() {
            ExamTimetable timetable = new ExamTimetable();
            Exam exam = new Exam(course1, classroom,
                    new ExamSlot(LocalDate.of(2024, 12, 20), LocalTime.of(9, 0), LocalTime.of(10, 0)));

            timetable.addExam(exam);

            assertEquals(1, timetable.getExams().size());
            assertEquals(exam, timetable.getExams().get(0));
        }

        @Test
        @DisplayName("Should add multiple exams")
        void shouldAddMultipleExams() {
            ExamTimetable timetable = new ExamTimetable();
            Exam exam1 = new Exam(course1, classroom,
                    new ExamSlot(LocalDate.of(2024, 12, 20), LocalTime.of(9, 0), LocalTime.of(10, 0)));
            Exam exam2 = new Exam(course2, classroom,
                    new ExamSlot(LocalDate.of(2024, 12, 20), LocalTime.of(11, 0), LocalTime.of(12, 0)));

            timetable.addExam(exam1);
            timetable.addExam(exam2);

            assertEquals(2, timetable.getExams().size());
        }
    }

    @Nested
    @DisplayName("Get Exams For Course Tests")
    class GetExamsForCourseTests {

        @Test
        @DisplayName("Should return exams for specific course")
        void shouldReturnExamsForSpecificCourse() {
            Exam exam1 = new Exam(course1, classroom,
                    new ExamSlot(LocalDate.of(2024, 12, 20), LocalTime.of(9, 0), LocalTime.of(10, 0)));
            Exam exam2 = new Exam(course2, classroom,
                    new ExamSlot(LocalDate.of(2024, 12, 20), LocalTime.of(11, 0), LocalTime.of(12, 0)));
            List<Exam> exams = new ArrayList<>();
            exams.add(exam1);
            exams.add(exam2);

            ExamTimetable timetable = new ExamTimetable(exams);

            List<Exam> course1Exams = timetable.getExamsForCourse(course1);

            assertEquals(1, course1Exams.size());
            assertEquals("CS101", course1Exams.get(0).getCourse().getCode());
        }

        @Test
        @DisplayName("Should return empty list for course with no exams")
        void shouldReturnEmptyListForCourseWithNoExams() {
            Exam exam = new Exam(course1, classroom,
                    new ExamSlot(LocalDate.of(2024, 12, 20), LocalTime.of(9, 0), LocalTime.of(10, 0)));
            List<Exam> exams = new ArrayList<>();
            exams.add(exam);

            ExamTimetable timetable = new ExamTimetable(exams);

            List<Exam> course2Exams = timetable.getExamsForCourse(course2);

            assertTrue(course2Exams.isEmpty());
        }
    }

    @Nested
    @DisplayName("Get Exams For Student Tests")
    class GetExamsForStudentTests {

        @Test
        @DisplayName("Should return exams for enrolled student")
        void shouldReturnExamsForEnrolledStudent() {
            Exam exam1 = new Exam(course1, classroom,
                    new ExamSlot(LocalDate.of(2024, 12, 20), LocalTime.of(9, 0), LocalTime.of(10, 0)));
            Exam exam2 = new Exam(course2, classroom,
                    new ExamSlot(LocalDate.of(2024, 12, 20), LocalTime.of(11, 0), LocalTime.of(12, 0)));
            Exam exam3 = new Exam(course3, classroom,
                    new ExamSlot(LocalDate.of(2024, 12, 20), LocalTime.of(14, 0), LocalTime.of(15, 0)));

            List<Exam> exams = new ArrayList<>();
            exams.add(exam1);
            exams.add(exam2);
            exams.add(exam3);

            // Student1 is enrolled in course1 and course2, not course3
            List<Enrollment> enrollments = List.of(
                    new Enrollment(student1, course1),
                    new Enrollment(student1, course2),
                    new Enrollment(student2, course3));

            ExamTimetable timetable = new ExamTimetable(exams, enrollments);

            List<Exam> student1Exams = timetable.getExamsForStudent(student1);

            assertEquals(2, student1Exams.size());
        }

        @Test
        @DisplayName("Should return empty list for student with no enrollments")
        void shouldReturnEmptyListForStudentWithNoEnrollments() {
            Exam exam = new Exam(course1, classroom,
                    new ExamSlot(LocalDate.of(2024, 12, 20), LocalTime.of(9, 0), LocalTime.of(10, 0)));

            List<Exam> exams = new ArrayList<>();
            exams.add(exam);
            List<Enrollment> enrollments = new ArrayList<>();

            ExamTimetable timetable = new ExamTimetable(exams, enrollments);

            List<Exam> studentExams = timetable.getExamsForStudent(student1);

            assertTrue(studentExams.isEmpty());
        }
    }

    @Nested
    @DisplayName("Set Exams Tests")
    class SetExamsTests {

        @Test
        @DisplayName("Should replace exams list")
        void shouldReplaceExamsList() {
            ExamTimetable timetable = new ExamTimetable();
            Exam exam1 = new Exam(course1, classroom,
                    new ExamSlot(LocalDate.of(2024, 12, 20), LocalTime.of(9, 0), LocalTime.of(10, 0)));
            timetable.addExam(exam1);

            Exam exam2 = new Exam(course2, classroom,
                    new ExamSlot(LocalDate.of(2024, 12, 21), LocalTime.of(9, 0), LocalTime.of(10, 0)));
            List<Exam> newExams = new ArrayList<>();
            newExams.add(exam2);

            timetable.setExams(newExams);

            assertEquals(1, timetable.getExams().size());
            assertEquals("CS102", timetable.getExams().get(0).getCourse().getCode());
        }
    }
}
