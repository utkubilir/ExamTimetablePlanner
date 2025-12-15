package com.examplanner.services;

import com.examplanner.domain.Classroom;
import com.examplanner.domain.Course;
import com.examplanner.domain.Enrollment;
import com.examplanner.domain.Exam;
import com.examplanner.domain.ExamSlot;
import com.examplanner.domain.Student;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ConstraintChecker {

    private static final LocalTime MIN_START_TIME = LocalTime.of(9, 0);
    private static final LocalTime MAX_END_TIME = LocalTime.of(18, 30);
    private long minGapMinutes = 30; // Default 30 mins
    private int maxExamsPerDay = 2; // Default 2

    public void setMinGapMinutes(long minGapMinutes) {
        this.minGapMinutes = minGapMinutes;
    }

    public void setMaxExamsPerDay(int maxExamsPerDay) {
        this.maxExamsPerDay = maxExamsPerDay;
    }

    // Fast lookup maps

    public boolean checkAll(Exam candidateExam, ScheduleState state) {
        if (!isWithinTimeWindow(candidateExam.getSlot()))
            return false;

        // Capacity Logic
        List<Student> students = state.getStudentsForCourse(candidateExam.getCourse().getCode());
        int studentCount = students.size();

        if (studentCount > candidateExam.getClassroom().getCapacity())
            return false;

        // Check Classroom availability (still O(N) relative to schedule size? No, needs
        // optimization too?)
        // Actually classroom is a single resource. We can index that too in
        // ScheduleState if needed.
        // For now, let's keep iterating existing exams for Classroom check,
        // OR better: check against the global list in ScheduleState.
        // Optimization: ScheduleState allows getExams().
        if (!isClassroomAvailable(candidateExam.getClassroom(), candidateExam.getSlot(), state.getExams()))
            return false;

        // Student constraints - OPTIMIZED
        for (Student s : students) {
            if (violatesDailyLimit(s, candidateExam.getSlot(), state))
                return false;
            if (!hasMinimumGap(s, candidateExam.getSlot(), state))
                return false;
        }

        return true;
    }

    public boolean isWithinTimeWindow(ExamSlot slot) {
        return !slot.getStartTime().isBefore(MIN_START_TIME) && !slot.getEndTime().isAfter(MAX_END_TIME);
    }

    public boolean fitsCapacity(Classroom classroom, Course course, Map<String, List<Student>> courseStudentsMap) {
        int studentCount = courseStudentsMap.getOrDefault(course.getCode(), List.of()).size();
        return studentCount <= classroom.getCapacity();
    }

    public boolean isClassroomAvailable(Classroom classroom, ExamSlot slot, List<Exam> existingExams) {
        for (Exam exam : existingExams) {
            if (exam.getClassroom().getId().equals(classroom.getId())) {
                if (exam.getSlot().overlaps(slot)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean violatesDailyLimit(Student student, ExamSlot slot, ScheduleState state) {
        // O(1) Lookup
        int count = state.getExamsCountForStudentDate(student.getId(), slot.getDate());
        // If they already have 2, they can't have another (making it 3)
        // Adjust limit here: if limit is 2 exams per day.
        return count >= maxExamsPerDay;
    }

    public boolean hasMinimumGap(Student student, ExamSlot slot, ScheduleState state) {
        // O(K) where K is number of exams that student has ON THAT DAY (usually 0, 1,
        // or 2)
        List<Exam> dayExams = state.getExamsForStudentDate(student.getId(), slot.getDate());

        for (Exam existing : dayExams) {
            long gap1 = Duration.between(existing.getSlot().getEndTime(), slot.getStartTime()).toMinutes();
            long gap2 = Duration.between(slot.getEndTime(), existing.getSlot().getStartTime()).toMinutes();

            if (gap1 >= 0 && gap1 < minGapMinutes)
                return false;
            if (gap2 >= 0 && gap2 < minGapMinutes)
                return false;
        }
        return true;
    }

}
