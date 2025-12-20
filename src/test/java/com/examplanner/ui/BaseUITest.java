package com.examplanner.ui;

// import java.util.concurrent.TimeUnit; // Removed - unused
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.AfterEach;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;

public class BaseUITest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        stage.show();
    }

    public void launchApp() throws Exception {
        // Skip Guided Tour by setting preference
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(
                com.examplanner.ui.tour.TourManager.class);
        prefs.putBoolean("tour_completed_v1", true);

        ApplicationTest.launch(MainApp.class);
    }

    @AfterEach
    public void afterEachTest() throws TimeoutException {
        FxToolkit.hideStage();
        release(new KeyCode[] {});
        release(new MouseButton[] {});
    }

    /**
     * Wait for JavaFX events to complete and add a small buffer for async tasks.
     * Replaces Thread.sleep() anti-pattern.
     */
    protected void waitForFxEvents() {
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Wait for JavaFX events with a timeout for async operations.
     * 
     * @param seconds Maximum seconds to wait
     */
    protected void waitForAsync(int seconds) {
        try {
            // Use Thread.sleep for background task delays
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        WaitForAsyncUtils.waitForFxEvents();
    }
}
