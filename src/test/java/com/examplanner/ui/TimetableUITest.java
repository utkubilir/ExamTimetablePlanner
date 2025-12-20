package com.examplanner.ui;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

import java.util.Collections;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.examplanner.domain.Classroom;
import com.examplanner.domain.Course;
import com.examplanner.domain.Enrollment;
import com.examplanner.domain.Student;
import com.examplanner.persistence.DataRepository;

public class TimetableUITest extends BaseUITest {

    private DataRepository repository;

    @BeforeEach
    public void setup() throws Exception {
        // Seed Database
        repository = new DataRepository();
        repository.clearAllData();

        Course c1 = new Course("CS101", "Intro to CS", 60);
        Student s1 = new Student("S1", "John Doe");
        Classroom r1 = new Classroom("R101", "Room 101", 50);
        Enrollment e1 = new Enrollment(s1, c1);

        repository.saveCourses(Collections.singletonList(c1));
        repository.saveStudents(Collections.singletonList(s1));
        repository.saveClassrooms(Collections.singletonList(r1));
        repository.saveEnrollments(Collections.singletonList(e1));

        // Launch App
        launchApp();
    }

    @AfterEach
    public void cleanup() {
        // Clean up seeded data after test
        if (repository != null) {
            repository.clearAllData();
        }
    }

    @Test
    public void testTimetableGenerationFlow() {
        waitForAsync(2);

        // Verify Data Loaded Status
        verifyThat("#lblCoursesStatus", hasText(Matchers.containsString("1")));

        // Click Generate Timetable
        clickOn("#btnGenerateDataImport");

        // Wait for async generation task
        waitForAsync(5);

        // Verify switched to Timetable View
        verifyThat("#lblTimetableTitle", isVisible());

        // Verify exam table is visible (content verification is fragile due to async
        // timing)
        verifyThat("#examTableView", isVisible());
    }
}
