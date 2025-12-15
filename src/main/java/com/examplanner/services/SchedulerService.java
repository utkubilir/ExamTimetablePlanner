package com.examplanner.services;

import com.examplanner.domain.Classroom;
import com.examplanner.domain.Course;
import com.examplanner.domain.Enrollment;
import com.examplanner.domain.Exam;
import com.examplanner.domain.ExamSlot;
import com.examplanner.domain.ExamTimetable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SchedulerService {

    private ConstraintChecker constraintChecker;

    public SchedulerService() {
        this.constraintChecker = new ConstraintChecker();
    }

    public ExamTimetable generateTimetable(List<Course> courses, List<Classroom> classrooms,
            List<Enrollment> enrollments, LocalDate startDate, boolean useStrictConstraints,
            List<LocalDate> blackoutDates) {

        long minGap = useStrictConstraints ? 180 : 180; // 180 mins (3h) per requirements

        System.out.println("Applying Constraints: MinGap=" + minGap + "m, MaxExams=2");
        constraintChecker.setMinGapMinutes(minGap);
        constraintChecker.setMaxExamsPerDay(2); // Always 2 for now based on requirements

        System.out.println("\n=== SCHEDULER SERVICE: Starting generation ===");
        System.out.println("Courses to schedule: " + courses.size());
        System.out.println("Available classrooms: " + classrooms.size());
        System.out.println("Total enrollments: " + enrollments.size());

        // Sort courses by difficulty (e.g. number of students enrolled) to fail fast
        // Or just by duration. Let's sort by number of students descending.
        // OPTIMIZATION: Pre-calculate course enrollment counts for sorting
        Map<String, Long> enrollmentCounts = enrollments.stream()
                .collect(Collectors.groupingBy(e -> e.getCourse().getCode(), Collectors.counting()));

        // Sort courses by difficulty (number of students enrolled)
        List<Course> sortedCourses = new ArrayList<>(courses);
        sortedCourses.sort(
                Comparator.comparingLong((Course c) -> enrollmentCounts.getOrDefault(c.getCode(), 0L)).reversed());

        // Calculate a smart lower bound for the number of days
        int days = calculateMinDaysNeeded(courses, classrooms, enrollments);
        System.out.println("Computed heuristic starting days: " + days);

        // Adjust max attempts based on problem size
        int maxAttemptsPerDay = courses.size() * classrooms.size() * 2000; // Increased from 500 for 10k scale
        System.out.println("Max attempts per day configuration: " + maxAttemptsPerDay);

        // OPTIMIZATION: Sort Classrooms by Capacity ASC (Best Fit Heuristic)
        // This ensures we try the smallest sufficient room first, saving larger rooms
        // for larger classes.
        List<Classroom> sortedClassrooms = new ArrayList<>(classrooms);
        sortedClassrooms.sort(Comparator.comparingInt(Classroom::getCapacity));

        // Pre-compute lookup maps for optimization
        Map<String, List<com.examplanner.domain.Student>> courseStudentsMap = enrollments.stream()
                .collect(Collectors.groupingBy(e -> e.getCourse().getCode(),
                        Collectors.mapping(Enrollment::getStudent, Collectors.toList())));

        int low = days; // Heuristic lower bound
        int high = 50; // Safe upper bound
        int optimalDays = -1;
        ExamTimetable bestResult = null;

        System.out.println("Starting Binary Search for optimal days (" + low + " - " + high + ")...");

        while (low <= high) {
            int mid = low + (high - low) / 2;
            System.out.println("\n>>> Trying " + mid + " days (Range: " + low + " - " + high + ")");

            ExamTimetable result = attemptSchedule(mid, sortedCourses, sortedClassrooms, enrollments, startDate,
                    maxAttemptsPerDay, courseStudentsMap, blackoutDates);

            if (result != null) {
                System.out.println(">>> Success with " + mid + " days! Trying fewer...");
                optimalDays = mid;
                bestResult = result;
                high = mid - 1; // Try to minimize days
            } else {
                System.out.println(">>> Failed (or timed out) with " + mid + " days! Need more...");
                low = mid + 1;
            }
        }

        if (bestResult != null) {
            System.out.println("\n✓ OPTIMAL SCHEDULE FOUND: " + optimalDays + " days.");
            return bestResult;
        } else {
            throw new RuntimeException("Could not find a schedule even with " + 50 + " days.");
        }
    }

    private ExamTimetable attemptSchedule(int days, List<Course> sortedCourses, List<Classroom> classrooms,
            List<Enrollment> enrollments, LocalDate startDate, int maxAttemptsPerDay,
            Map<String, List<com.examplanner.domain.Student>> courseStudentsMap,
            List<LocalDate> blackoutDates) {

        System.out.println("=== ATTEMPTING SCHEDULE WITH " + days + " DAY(S) ===");

        // Initialize Optimized State
        ScheduleState state = new ScheduleState(courseStudentsMap);

        long startTime = System.currentTimeMillis();
        long timeoutMs = 5000; // 5 seconds timeout per attempt

        java.util.concurrent.atomic.AtomicInteger attemptCounter = new java.util.concurrent.atomic.AtomicInteger(0);

        boolean success = backtrackWithLimit(0, sortedCourses, state, classrooms, startDate, days,
                maxAttemptsPerDay, attemptCounter, startTime, timeoutMs, blackoutDates);
        long elapsed = System.currentTimeMillis() - startTime;

        if (success) {
            System.out.println("✓ SUCCESS! Scheduled all exams in " + days + " day(s)");
            System.out.println("Total exams scheduled: " + state.getExams().size());
            System.out.println("Time taken: " + elapsed + "ms");
            return new ExamTimetable(new ArrayList<>(state.getExams()), enrollments);
        }

        System.out.println("✗ Failed (or Timed Out) with " + days + " day(s) (tried for " + elapsed + "ms)");
        return null; // Failed
    }

    private boolean backtrackWithLimit(int index, List<Course> courses, ScheduleState state,
            List<Classroom> classrooms,
            LocalDate startDate, int maxDays, int maxAttempts,
            java.util.concurrent.atomic.AtomicInteger attemptCounter,
            long startTime, long timeoutMs,
            List<LocalDate> blackoutDates) {

        if (System.currentTimeMillis() - startTime > timeoutMs) {
            if (attemptCounter.get() % 5000 == 0)
                System.out.println("  [TIMEOUT reached after " + timeoutMs + "ms]");
            return false;
        }

        if (index == courses.size()) {
            System.out.println("✓ All " + courses.size() + " courses successfully scheduled!");
            return true; // All courses scheduled
        }

        if (attemptCounter.get() >= maxAttempts) {
            // Log only occasionally to avoid spamming I/O
            if (attemptCounter.get() % 10000 == 0) {
                System.out.println("  [Reached max attempts limit (" + maxAttempts + ") for " + maxDays + " day(s)]");
            }
            return false;
        }

        Course course = courses.get(index);
        attemptCounter.incrementAndGet(); // We can increment here directly

        // Retrieve students ONCE for this course
        List<com.examplanner.domain.Student> students = state.getStudentsForCourse(course.getCode());

        // Try all days, all classrooms, all time slots
        for (int d = 0; d < maxDays; d++) {
            LocalDate date = startDate.plusDays(d);

            if (blackoutDates != null && blackoutDates.contains(date)) {
                // SKIP BLACKOUT DATE
                continue;
            }

            // PRUNING: Check if ANY enrolled student already has max exams on this day.
            boolean dayForbidden = false;
            for (com.examplanner.domain.Student s : students) {
                if (state.getExamsCountForStudentDate(s.getId(), date) >= 2) {
                    dayForbidden = true;
                    break;
                }
            }
            if (dayForbidden)
                continue;

            for (Classroom classroom : classrooms) {
                // Check Capacity Constraint (O(1))
                // Note: state.getStudentsForCourse(course.getCode()) is fast O(1)
                int size = state.getStudentsForCourse(course.getCode()).size();
                if (size > classroom.getCapacity()) {
                    continue;
                }

                // Try start times. 09:00 to 18:30 - duration
                LocalTime startTimeSlot = LocalTime.of(9, 0);
                LocalTime maxStart = LocalTime.of(18, 30).minusMinutes(course.getExamDurationMinutes());

                while (!startTimeSlot.isAfter(maxStart)) {
                    LocalTime endTime = startTimeSlot.plusMinutes(course.getExamDurationMinutes());
                    ExamSlot slot = new ExamSlot(date, startTimeSlot, endTime);
                    Exam candidate = new Exam(course, classroom, slot);

                    if (constraintChecker.checkAll(candidate, state)) {
                        state.add(candidate);

                        if (backtrackWithLimit(index + 1, courses, state, classrooms, startDate,
                                maxDays, maxAttempts, attemptCounter, startTime, timeoutMs, blackoutDates)) {
                            return true;
                        }

                        state.removeLast(); // Backtrack
                    }

                    startTimeSlot = startTimeSlot.plusMinutes(30); // Increment by 30 mins
                }
            }
        }

        return false;
    }

    private int calculateMinDaysNeeded(List<Course> courses, List<Classroom> classrooms, List<Enrollment> enrollments) {
        // 1. Capacity Constraint: Total Exam Hours / Total Classroom Hours Available
        double totalExamMinutes = courses.stream().mapToDouble(Course::getExamDurationMinutes).sum();
        // Assuming 9:00 to 18:30 operating hours (9.5 hours = 570 minutes)
        double dailyClassroomMinutes = classrooms.size() * 570.0;

        int minDaysForCapacity = (int) Math.ceil(totalExamMinutes / dailyClassroomMinutes);

        // 2. Student Load Constraint: Max Exams per Student / Max Exams per Day (2)
        Map<String, Long> examsPerStudent = enrollments.stream()
                .collect(Collectors.groupingBy(e -> e.getStudent().getId(), Collectors.counting()));

        long maxExamsForSingleStudent = examsPerStudent.values().stream().mapToLong(l -> l).max().orElse(0);
        int minDaysForStudents = (int) Math.ceil((double) maxExamsForSingleStudent / 2.0);

        // 3. Take basic max
        int minDays = Math.max(minDaysForCapacity, minDaysForStudents);

        // 4. Heuristic: Add buffer. If constraints are tight, opt fails often.
        // If minDays is small (e.g. 1-2), add a buffer of +1 or +2 to be safe and fast.
        // For larger days, maybe +20%.
        // Let's stick to a simple buffer: if < 3, add 1. If >= 3, add 1.
        // Almost always better to over-estimate start day to avoid waiting.

        return Math.max(1, minDays + 1);
    }
}
