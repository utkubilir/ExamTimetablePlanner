package com.examplanner.domain;

public class Exam {
    private Course course;
    private Classroom classroom;
    private ExamSlot slot;

    public Exam(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null");
        }
        this.course = course;
    }

    public Exam(Course course, Classroom classroom, ExamSlot slot) {
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null");
        }
        if (classroom == null) {
            throw new IllegalArgumentException("Classroom cannot be null");
        }
        if (slot == null) {
            throw new IllegalArgumentException("ExamSlot cannot be null");
        }
        this.course = course;
        this.classroom = classroom;
        this.slot = slot;
    }

    public Course getCourse() {
        return course;
    }

    public Classroom getClassroom() {
        return classroom;
    }

    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
    }

    public ExamSlot getSlot() {
        return slot;
    }

    public void setSlot(ExamSlot slot) {
        this.slot = slot;
    }

    @Override
    public String toString() {
        return "Exam{" +
                "course=" + course +
                ", classroom=" + classroom +
                ", slot=" + slot +
                '}';
    }
}
