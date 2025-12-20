package com.examplanner.ui;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isEnabled;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.examplanner.persistence.DataRepository;

/**
 * Tests for edge case scenarios and UI state management.
 * Focuses on verifiable non-blocking UI behaviors.
 */
public class ErrorHandlingUITest extends BaseUITest {

    private DataRepository repository;

    @BeforeEach
    public void setup() throws Exception {
        repository = new DataRepository();
        repository.clearAllData();
        launchApp();
    }

    @Test
    public void testEmptyStateShowsStatusLabels() {
        waitForAsync(2);

        // With no data, status labels should be visible
        verifyThat("#lblCoursesStatus", isVisible());
        verifyThat("#lblClassroomsStatus", isVisible());
        verifyThat("#lblStudentsStatus", isVisible());

        // Navigation buttons should still be accessible
        verifyThat("#btnDashboard", isVisible());
        verifyThat("#btnTimetable", isVisible());
    }

    @Test
    public void testDeleteDataButtonIsEnabled() {
        waitForAsync(2);

        // Delete data button should be visible and enabled
        verifyThat("#btnDeleteData", isVisible());
        verifyThat("#btnDeleteData", isEnabled());
    }

    @Test
    public void testSettingsButtonIsAccessible() {
        waitForAsync(2);

        // Settings button should be visible and enabled
        verifyThat("#btnSettings", isVisible());
        verifyThat("#btnSettings", isEnabled());
    }

    @Test
    public void testGenerateButtonWithNoData() {
        waitForAsync(2);

        // Generate button should be visible
        verifyThat("#btnGenerateDataImport", isVisible());

        // Click Generate - should not crash with empty data
        clickOn("#btnGenerateDataImport");
        waitForAsync(2);

        // Should stay on data import view
        verifyThat("#lblDataImportTitle", isVisible());
    }
}
