package com.examplanner.ui;

import javafx.application.Application;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.TranslateTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.Interpolator;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import java.util.Random;

public class MainApp extends Application {

    private static Scene scene;
    private Stage splashStage;
    private Stage mainStage;
    private Random random = new Random();

    // Loading tips
    private static final String[] LOADING_TIPS = {
            "💡 F1 tuşu ile yardım menüsünü açabilirsiniz",
            "⌨️ Ctrl+O ile dosya import edebilirsiniz",
            "🎨 Ayarlardan dark mode'u etkinleştirebilirsiniz",
            "📅 Ctrl+G ile zaman çizelgesi oluşturabilirsiniz",
            "🔍 Ctrl+K ile kısayolları görüntüleyebilirsiniz",
            "🎓 Öğrenci takvimlerini ayrı ayrı görüntüleyebilirsiniz"
    };

    @Override
    public void start(Stage stage) {
        this.mainStage = stage;
        showSplashScreen();
    }

    private void showSplashScreen() {
        try {
            // Load splash screen
            FXMLLoader splashLoader = new FXMLLoader(MainApp.class.getResource("SplashScreen.fxml"));
            Parent splashRoot = splashLoader.load();
            Scene splashScene = new Scene(splashRoot, 550, 400);
            splashScene.getStylesheets().add(MainApp.class.getResource("style.css").toExternalForm());

            splashStage = new Stage();
            splashStage.initStyle(StageStyle.UNDECORATED);
            splashStage.setScene(splashScene);

            // Set initial opacity for fade-in
            splashRoot.setOpacity(0);
            splashStage.show();

            // Fade-in animation
            FadeTransition fadeIn = new FadeTransition(Duration.millis(600), splashRoot);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();

            // Get UI elements
            ProgressBar progressBar = (ProgressBar) splashRoot.lookup("#progressBar");
            Label lblStatus = (Label) splashRoot.lookup("#lblStatus");
            Label loadingDots = (Label) splashRoot.lookup("#loadingDots");
            ImageView appIcon = (ImageView) splashRoot.lookup("#appIcon");
            Label tipLabel = (Label) splashRoot.lookup("#tipLabel");
            Pane particleContainer = (Pane) splashRoot.lookup("#particleContainer");

            // Create floating particles
            if (particleContainer != null) {
                createFloatingParticles(particleContainer, 550, 400);
            }

            // Icon pulse animation
            if (appIcon != null) {
                ScaleTransition pulse = new ScaleTransition(Duration.millis(1200), appIcon);
                pulse.setFromX(1.0);
                pulse.setFromY(1.0);
                pulse.setToX(1.06);
                pulse.setToY(1.06);
                pulse.setCycleCount(Timeline.INDEFINITE);
                pulse.setAutoReverse(true);
                pulse.setInterpolator(Interpolator.EASE_BOTH);
                pulse.play();
            }

            // Loading dots animation
            if (loadingDots != null) {
                Timeline dotsAnimation = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(loadingDots.textProperty(), "")),
                        new KeyFrame(Duration.millis(300), new KeyValue(loadingDots.textProperty(), ".")),
                        new KeyFrame(Duration.millis(600), new KeyValue(loadingDots.textProperty(), "..")),
                        new KeyFrame(Duration.millis(900), new KeyValue(loadingDots.textProperty(), "...")));
                dotsAnimation.setCycleCount(Timeline.INDEFINITE);
                dotsAnimation.play();
            }

            // Tip rotation animation
            if (tipLabel != null) {
                Timeline tipRotation = new Timeline(
                        new KeyFrame(Duration.seconds(3), e -> {
                            // Fade out
                            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), tipLabel);
                            fadeOut.setFromValue(1.0);
                            fadeOut.setToValue(0.0);
                            fadeOut.setOnFinished(ev -> {
                                // Change text
                                tipLabel.setText(LOADING_TIPS[random.nextInt(LOADING_TIPS.length)]);
                                // Fade in
                                FadeTransition tipFadeIn = new FadeTransition(Duration.millis(300), tipLabel);
                                tipFadeIn.setFromValue(0.0);
                                tipFadeIn.setToValue(1.0);
                                tipFadeIn.play();
                            });
                            fadeOut.play();
                        }));
                tipRotation.setCycleCount(Timeline.INDEFINITE);
                tipRotation.play();
            }

            // Load preferences for language
            java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(MainApp.class);
            String lang = prefs.get("language_preference", "en");
            java.util.Locale locale = new java.util.Locale(lang);
            java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com.examplanner.ui.messages", locale);

            // Animate progress bar with detailed stages
            Task<Void> loadingTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    // Stage 1: Initialize database
                    updateMessage(bundle.getString("splash.initDatabase"));
                    updateProgress(0.15, 1.0);
                    com.examplanner.persistence.DatabaseManager.initializeDatabase();
                    Thread.sleep(350);

                    // Stage 2: Loading configuration
                    updateMessage(bundle.getString("splash.loadingUI"));
                    updateProgress(0.35, 1.0);
                    Thread.sleep(300);

                    // Stage 3: Loading resources
                    updateProgress(0.55, 1.0);
                    Thread.sleep(250);

                    // Stage 4: Preparing UI
                    updateMessage(bundle.getString("splash.preparing"));
                    updateProgress(0.75, 1.0);
                    Thread.sleep(200);

                    // Stage 5: Finalizing
                    updateProgress(0.90, 1.0);
                    Thread.sleep(150);

                    // Complete
                    updateProgress(1.0, 1.0);
                    Thread.sleep(100);

                    return null;
                }
            };

            progressBar.progressProperty().bind(loadingTask.progressProperty());
            lblStatus.textProperty().bind(loadingTask.messageProperty());

            loadingTask.setOnSucceeded(e -> {
                // Fade out splash screen
                FadeTransition fadeOut = new FadeTransition(Duration.millis(400), splashRoot);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(event -> {
                    splashStage.close();
                    showMainWindow();
                });
                fadeOut.play();
            });

            loadingTask.setOnFailed(e -> {
                lblStatus.textProperty().unbind();
                lblStatus.setText("Hata: " + loadingTask.getException().getMessage());
            });

            new Thread(loadingTask).start();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load splash screen: " + e.getMessage());
            // Fallback to main window
            showMainWindow();
        }
    }

    private void createFloatingParticles(Pane container, double width, double height) {
        for (int i = 0; i < 15; i++) {
            Circle particle = new Circle();
            particle.setRadius(random.nextDouble() * 4 + 2); // 2-6 radius
            particle.setFill(Color.rgb(255, 255, 255, random.nextDouble() * 0.3 + 0.1)); // Semi-transparent white
            particle.setCenterX(random.nextDouble() * width);
            particle.setCenterY(random.nextDouble() * height);
            container.getChildren().add(particle);

            // Float up animation
            TranslateTransition floatUp = new TranslateTransition(
                    Duration.seconds(random.nextDouble() * 5 + 5), // 5-10 seconds
                    particle);
            floatUp.setByY(-(height + 50)); // Float up beyond screen
            floatUp.setCycleCount(Timeline.INDEFINITE);
            floatUp.setInterpolator(Interpolator.LINEAR);

            // Slight horizontal sway
            TranslateTransition sway = new TranslateTransition(
                    Duration.seconds(random.nextDouble() * 3 + 2),
                    particle);
            sway.setByX(random.nextDouble() * 60 - 30); // -30 to +30 horizontal
            sway.setCycleCount(Timeline.INDEFINITE);
            sway.setAutoReverse(true);
            sway.setInterpolator(Interpolator.EASE_BOTH);

            // Fade animation
            FadeTransition fade = new FadeTransition(
                    Duration.seconds(random.nextDouble() * 4 + 3),
                    particle);
            fade.setFromValue(particle.getOpacity());
            fade.setToValue(0.0);
            fade.setCycleCount(Timeline.INDEFINITE);
            fade.setAutoReverse(true);

            // Play all animations
            ParallelTransition allAnimations = new ParallelTransition(floatUp, sway, fade);
            allAnimations.setDelay(Duration.seconds(random.nextDouble() * 2)); // Stagger start
            allAnimations.play();
        }
    }

    private void showMainWindow() {
        try {
            // Load preferences for language
            java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(MainApp.class);
            String lang = prefs.get("language_preference", "en");
            java.util.Locale locale = new java.util.Locale(lang);
            java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com.examplanner.ui.messages", locale);

            FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("MainView.fxml"));
            fxmlLoader.setResources(bundle);

            scene = new Scene(fxmlLoader.load(), 1200, 800);
            mainStage.setScene(scene);
            mainStage.setTitle("Exam Timetable Planner");

            scene.getRoot().setOpacity(0);
            mainStage.show();

            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), scene.getRoot());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to start application: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
