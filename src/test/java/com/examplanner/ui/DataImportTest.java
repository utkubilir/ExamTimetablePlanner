package com.examplanner.ui;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.testfx.matcher.base.NodeMatchers.isEnabled;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DataImportTest extends BaseUITest {

    @BeforeEach
    public void setup() throws Exception {
        launchApp();
    }

    @Test
    public void testDataImportViewElements() {
        waitForAsync(2);

        // Navigate to Data Import
        clickOn("#btnDataImport");
        waitForFxEvents();

        // Verify Title and Subtitle
        verifyThat("#lblDataImportTitle", isVisible());
        verifyThat("#lblDataImportSubtitle", isVisible());

        // Verify Import Cards (Labels) are visible
        verifyThat("#lblCoursesTitle", isVisible());
        verifyThat("#lblStudentsTitle", isVisible());
        verifyThat("#lblClassroomsTitle", isVisible());

        // Verify Clear Data button is enabled
        verifyThat("#btnDeleteData", isEnabled());
    }
}
