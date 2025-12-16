package com.examplanner.domain;

public class Course {
    private String code;
    private String name;
    private int examDurationMinutes;

    public Course(String code, String name, int examDurationMinutes) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Course code cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Course name cannot be null or empty");
        }
        if (examDurationMinutes <= 0) {
            throw new IllegalArgumentException("Exam duration must be positive, got: " + examDurationMinutes);
        }
        this.code = code.trim();
        this.name = name.trim();
        this.examDurationMinutes = examDurationMinutes;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public int getExamDurationMinutes() {
        return examDurationMinutes;
    }

    @Override
    public String toString() {
        if (name != null && name.equals(code)) {
            return code;
        }
        return code + " - " + name;
    }
}
