package com.examplanner.ui;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.examplanner.domain.Classroom;
import com.examplanner.domain.Course;
import com.examplanner.domain.Enrollment;
import com.examplanner.domain.Student;
import com.examplanner.persistence.DataRepository;

public class DashboardUITest extends BaseUITest {

    private DataRepository repository;

    @BeforeEach
    public void setup() throws Exception {
        // Seed Database with sample data
        repository = new DataRepository();
        repository.clearAllData();

        Course c1 = new Course("CS101", "Intro to CS", 60);
        Course c2 = new Course("CS102", "Data Structures", 90);
        Student s1 = new Student("S1", "John Doe");
        Student s2 = new Student("S2", "Jane Smith");
        Classroom r1 = new Classroom("R101", "Room 101", 50);
        Enrollment e1 = new Enrollment(s1, c1);
        Enrollment e2 = new Enrollment(s2, c2);

        repository.saveCourses(Arrays.asList(c1, c2));
        repository.saveStudents(Arrays.asList(s1, s2));
        repository.saveClassrooms(Collections.singletonList(r1));
        repository.saveEnrollments(Arrays.asList(e1, e2));

        // Launch App
        launchApp();
    }

    @AfterEach
    public void cleanup() {
        if (repository != null) {
            repository.clearAllData();
        }
    }

    @Test
    public void testDashboardStatistics() {
        waitForAsync(2);

        // Navigate to Dashboard
        clickOn("#btnDashboard");
        waitForFxEvents();

        // Verify Dashboard Title is visible
        verifyThat("#lblDashboardTitle", isVisible());

        // Verify Statistics Cards are visible
        verifyThat("#lblStatExamsValue", isVisible());
        verifyThat("#lblStatStudentsValue", isVisible());
        verifyThat("#lblStatClassroomsValue", isVisible());

        // Verify Charts are rendered (visible)
        verifyThat("#chartExamsPerDay", isVisible());
        verifyThat("#chartRoomUsage", isVisible());
    }

    @Test
    public void testDashboardAfterGeneration() {
        waitForAsync(2);

        // Generate Timetable first
        clickOn("#btnGenerateDataImport");

        // Wait for async generation task
        waitForAsync(5);

        // Navigate to Dashboard
        clickOn("#btnDashboard");
        waitForFxEvents();

        // Verify Dashboard is visible and stats are showing
        verifyThat("#lblDashboardTitle", isVisible());
        verifyThat("#lblStatExamsValue", isVisible());
        verifyThat("#lblStatStudentsValue", isVisible());
        verifyThat("#lblStatClassroomsValue", isVisible());

        // Verify Charts are rendered
        verifyThat("#chartExamsPerDay", isVisible());
    }
}
