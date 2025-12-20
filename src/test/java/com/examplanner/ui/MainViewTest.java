package com.examplanner.ui;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MainViewTest extends BaseUITest {

    @BeforeEach
    public void setup() throws Exception {
        launchApp();
    }

    @Test
    public void testMainViewLaunch() {
        // Wait for splash screen to fade
        waitForAsync(2);

        // Verify Sidebar Buttons are visible (even in collapsed mode)
        verifyThat("#btnDashboard", isVisible());
        verifyThat("#btnDataImport", isVisible());

        // Verify main content is loaded
        verifyThat("#lblDataImportTitle", isVisible());
    }

    @Test
    public void testSidebarNavigation() {
        waitForAsync(2);

        // Click on Timetable button
        clickOn("#btnTimetable");
        waitForFxEvents();

        // Verify Timetable View Title is visible
        verifyThat("#lblTimetableTitle", isVisible());

        // Click on Dashboard button
        clickOn("#btnDashboard");
        waitForFxEvents();

        // Verify Dashboard View Title is visible
        verifyThat("#lblDashboardTitle", isVisible());
    }
}
