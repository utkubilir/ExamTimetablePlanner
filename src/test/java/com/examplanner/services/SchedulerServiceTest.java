package com.examplanner.services;

import com.examplanner.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SchedulerService.
 */
class SchedulerServiceTest {

    private SchedulerService schedulerService;

    @BeforeEach
    void setUp() {
        schedulerService = new SchedulerService();
    }

    @Nested
    @DisplayName("Input Validation Tests")
    class InputValidationTests {

        @Test
        @DisplayName("Should throw exception for null courses")
        void shouldThrowExceptionForNullCourses() {
            List<Classroom> classrooms = createClassrooms(5);
            List<Enrollment> enrollments = new ArrayList<>();
            LocalDate startDate = LocalDate.now().plusDays(1);

            assertThrows(
                    IllegalArgumentException.class,
                    () -> schedulerService.generateTimetable(null, classrooms, enrollments, startDate));
        }

        @Test
        @DisplayName("Should throw exception for empty courses")
        void shouldThrowExceptionForEmptyCourses() {
            List<Course> courses = new ArrayList<>();
            List<Classroom> classrooms = createClassrooms(5);
            List<Enrollment> enrollments = new ArrayList<>();
            LocalDate startDate = LocalDate.now().plusDays(1);

            assertThrows(
                    IllegalArgumentException.class,
                    () -> schedulerService.generateTimetable(courses, classrooms, enrollments, startDate));
        }

        @Test
        @DisplayName("Should throw exception for null classrooms")
        void shouldThrowExceptionForNullClassrooms() {
            List<Course> courses = createCourses(5);
            List<Enrollment> enrollments = new ArrayList<>();
            LocalDate startDate = LocalDate.now().plusDays(1);

            assertThrows(
                    IllegalArgumentException.class,
                    () -> schedulerService.generateTimetable(courses, null, enrollments, startDate));
        }

        @Test
        @DisplayName("Should throw exception for empty classrooms")
        void shouldThrowExceptionForEmptyClassrooms() {
            List<Course> courses = createCourses(5);
            List<Classroom> classrooms = new ArrayList<>();
            List<Enrollment> enrollments = new ArrayList<>();
            LocalDate startDate = LocalDate.now().plusDays(1);

            assertThrows(
                    IllegalArgumentException.class,
                    () -> schedulerService.generateTimetable(courses, classrooms, enrollments, startDate));
        }

        @Test
        @DisplayName("Should throw exception for null enrollments")
        void shouldThrowExceptionForNullEnrollments() {
            List<Course> courses = createCourses(5);
            List<Classroom> classrooms = createClassrooms(5);
            LocalDate startDate = LocalDate.now().plusDays(1);

            assertThrows(
                    IllegalArgumentException.class,
                    () -> schedulerService.generateTimetable(courses, classrooms, null, startDate));
        }

        @Test
        @DisplayName("Should throw exception for empty enrollments")
        void shouldThrowExceptionForEmptyEnrollments() {
            List<Course> courses = createCourses(5);
            List<Classroom> classrooms = createClassrooms(5);
            List<Enrollment> enrollments = new ArrayList<>();
            LocalDate startDate = LocalDate.now().plusDays(1);

            assertThrows(
                    IllegalArgumentException.class,
                    () -> schedulerService.generateTimetable(courses, classrooms, enrollments, startDate));
        }

        @Test
        @DisplayName("Should throw exception for null start date")
        void shouldThrowExceptionForNullStartDate() {
            List<Course> courses = createCourses(5);
            List<Classroom> classrooms = createClassrooms(5);
            List<Student> students = createStudents(10);
            List<Enrollment> enrollments = createEnrollments(courses, students);

            assertThrows(
                    IllegalArgumentException.class,
                    () -> schedulerService.generateTimetable(courses, classrooms, enrollments, null));
        }
    }

    @Nested
    @DisplayName("Simple Scheduling Tests")
    class SimpleSchedulingTests {

        @Test
        @DisplayName("Should schedule single course successfully")
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        void shouldScheduleSingleCourse() {
            List<Course> courses = List.of(new Course("CS101", "Programming", 60));
            List<Classroom> classrooms = List.of(new Classroom("A101", "Hall", 100));
            List<Student> students = List.of(new Student("S001", "Ali"));
            List<Enrollment> enrollments = List.of(new Enrollment(students.get(0), courses.get(0)));
            LocalDate startDate = LocalDate.now().plusDays(1);

            ExamTimetable result = schedulerService.generateTimetable(courses, classrooms, enrollments, startDate);

            assertNotNull(result);
            assertEquals(1, result.getExams().size());

            Exam exam = result.getExams().get(0);
            assertEquals("CS101", exam.getCourse().getCode());
            assertNotNull(exam.getClassroom());
            assertNotNull(exam.getSlot());
        }

        @Test
        @DisplayName("Should schedule multiple courses with no conflicts")
        @Timeout(value = 30, unit = TimeUnit.SECONDS)
        void shouldScheduleMultipleCoursesWithNoConflicts() {
            List<Course> courses = createCourses(5);
            List<Classroom> classrooms = createClassrooms(3);
            List<Student> students = createStudents(20);
            List<Enrollment> enrollments = createEnrollments(courses, students);
            LocalDate startDate = LocalDate.now().plusDays(1);

            ExamTimetable result = schedulerService.generateTimetable(courses, classrooms, enrollments, startDate);

            assertNotNull(result);
            assertEquals(5, result.getExams().size());

            // Verify no classroom conflicts
            for (int i = 0; i < result.getExams().size(); i++) {
                for (int j = i + 1; j < result.getExams().size(); j++) {
                    Exam exam1 = result.getExams().get(i);
                    Exam exam2 = result.getExams().get(j);

                    if (exam1.getClassroom().getId().equals(exam2.getClassroom().getId())) {
                        assertFalse(exam1.getSlot().overlaps(exam2.getSlot()),
                                "Exams in same classroom should not overlap");
                    }
                }
            }
        }
    }

    @Nested
    @DisplayName("Constraint Verification Tests")
    class ConstraintVerificationTests {

        @Test
        @DisplayName("Should respect working hours constraint")
        @Timeout(value = 30, unit = TimeUnit.SECONDS)
        void shouldRespectWorkingHoursConstraint() {
            List<Course> courses = createCourses(3);
            List<Classroom> classrooms = createClassrooms(2);
            List<Student> students = createStudents(10);
            List<Enrollment> enrollments = createEnrollments(courses, students);
            LocalDate startDate = LocalDate.now().plusDays(1);

            ExamTimetable result = schedulerService.generateTimetable(courses, classrooms, enrollments, startDate);

            assertNotNull(result);
            for (Exam exam : result.getExams()) {
                assertTrue(exam.getSlot().getStartTime().getHour() >= 9,
                        "Exam should start at or after 9:00");
                assertTrue(
                        exam.getSlot().getEndTime().getHour() < 19 ||
                                (exam.getSlot().getEndTime().getHour() == 18
                                        && exam.getSlot().getEndTime().getMinute() <= 30),
                        "Exam should end by 18:30");
            }
        }

        @Test
        @DisplayName("Should respect classroom capacity constraint")
        @Timeout(value = 30, unit = TimeUnit.SECONDS)
        void shouldRespectClassroomCapacityConstraint() {
            // Create a course with many students
            Course bigCourse = new Course("BIG101", "Big Class", 60);
            List<Student> students = createStudents(80);
            List<Enrollment> enrollments = students.stream()
                    .map(s -> new Enrollment(s, bigCourse))
                    .toList();

            // Only provide a classroom that can fit them
            Classroom bigRoom = new Classroom("BIG", "Big Hall", 100);
            Classroom smallRoom = new Classroom("SMALL", "Small Room", 30);

            ExamTimetable result = schedulerService.generateTimetable(
                    List.of(bigCourse),
                    List.of(bigRoom, smallRoom),
                    enrollments,
                    LocalDate.now().plusDays(1));

            assertNotNull(result);
            assertEquals(1, result.getExams().size());
            assertEquals("BIG", result.getExams().get(0).getClassroom().getId(),
                    "Should assign to big room due to capacity constraint");
        }
    }

    // Helper methods

    private List<Course> createCourses(int count) {
        List<Course> courses = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            courses.add(new Course("CS" + (100 + i), "Course " + i, 60 + (i % 3) * 30));
        }
        return courses;
    }

    private List<Classroom> createClassrooms(int count) {
        List<Classroom> classrooms = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            classrooms.add(new Classroom("R" + (100 + i), "Room " + i, 50 + i * 20));
        }
        return classrooms;
    }

    private List<Student> createStudents(int count) {
        List<Student> students = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            students.add(new Student("S" + String.format("%03d", i), "Student " + i));
        }
        return students;
    }

    private List<Enrollment> createEnrollments(List<Course> courses, List<Student> students) {
        List<Enrollment> enrollments = new ArrayList<>();
        // Each student enrolls in all courses
        for (Student student : students) {
            for (Course course : courses) {
                enrollments.add(new Enrollment(student, course));
            }
        }
        return enrollments;
    }
}
