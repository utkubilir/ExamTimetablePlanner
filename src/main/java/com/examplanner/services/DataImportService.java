package com.examplanner.services;

import com.examplanner.domain.Classroom;
import com.examplanner.domain.Course;
import com.examplanner.domain.Enrollment;
import com.examplanner.domain.Student;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataImportService {

    public List<Course> loadCourses(File file) throws IOException {
        List<Course> courses = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                // Skip empty lines or headers
                if (line.isEmpty() || line.startsWith("ALL OF THE"))
                    continue;

                // Format: <CourseCode> (Single column)
                // Use Code as Name, Default duration 60
                String code = line;
                String name = line;
                int duration = 60;

                courses.add(new Course(code, name, duration));
            }
        }
        return courses;
    }

    public List<Classroom> loadClassrooms(File file) throws IOException {
        List<Classroom> classrooms = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("ALL OF THE"))
                    continue;

                // Format: Name;Capacity
                String[] parts = line.split(";");
                if (parts.length >= 2) {
                    String name = parts[0].trim();
                    try {
                        int capacity = Integer.parseInt(parts[1].trim());
                        // Use Name (e.g., Classroom_01) as ID as well
                        classrooms.add(new Classroom(name, name, capacity));
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping invalid capacity in line: " + line);
                    }
                }
            }
        }
        return classrooms;
    }

    public List<Student> loadStudents(File file) throws IOException {
        List<Student> students = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("ALL OF THE"))
                    continue;

                // Format: <StudentID> (Single column)
                // Use ID as Name
                String id = line;
                String name = line;

                students.add(new Student(id, name));
            }
        }
        return students;
    }

    public List<Enrollment> loadAttendance(File file, List<Course> courses, List<Student> existingStudents)
            throws IOException {
        List<Enrollment> enrollments = new ArrayList<>();

        // Quick lookup maps
        Map<String, Course> courseMap = new HashMap<>();
        for (Course c : courses) {
            courseMap.put(c.getCode(), c);
        }

        Map<String, Student> studentMap = new HashMap<>();
        if (existingStudents != null) {
            for (Student s : existingStudents) {
                studentMap.put(s.getId(), s);
            }
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            String currentCourseCode = null;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;

                // If line doesn't start with [, assume it is a Course Code
                if (!line.startsWith("[")) {
                    // Check if it's a known course code or just a label
                    // Sample file has lines like "CourseCode_01"
                    currentCourseCode = line;
                } else {
                    // It is the student list line: ['Std_ID_170', 'Std_ID_077', ...]
                    if (currentCourseCode == null)
                        continue;

                    Course course = courseMap.get(currentCourseCode);
                    if (course == null) {
                        System.err.println("Course code not found during import: " + currentCourseCode);
                        continue;
                    }

                    // Remove brackets and split by comma
                    String content = line.substring(1, line.length() - 1); // remove [ and ]
                    // content is: 'Std_ID_170', 'Std_ID_077', ...

                    if (content.isEmpty())
                        continue;

                    String[] studentIds = content.split(",");
                    for (String rawId : studentIds) {
                        String sId = rawId.trim();
                        // Remove single quotes if present
                        if (sId.startsWith("'") && sId.endsWith("'")) {
                            sId = sId.substring(1, sId.length() - 1);
                        }

                        // Find or create student (if logically allowed, but better to match existing)
                        Student student = studentMap.get(sId);
                        if (student == null) {
                            // If student wasn't loaded in the student file, we can optionally create a
                            // placeholder
                            // or skip. Given the user context "import all", creating placeholder is safer.
                            student = new Student(sId, sId);
                            studentMap.put(sId, student);
                            // Also maybe add to existingStudents list if we were passing it back,
                            // but here we just need verification object
                        }

                        enrollments.add(new Enrollment(student, course));
                    }
                }
            }
        }
        return enrollments;
    }
}
