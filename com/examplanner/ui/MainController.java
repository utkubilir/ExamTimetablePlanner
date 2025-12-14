package com.examplanner.ui;


import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.examplanner.domain.*;
import com.examplanner.services.*;
import com.examplanner.persistence.*;

public class MainController {

    // --- FXML UI BİLEŞENLERİ ---
    @FXML private Button btnDataImport;
    @FXML private Button btnTimetable;
    @FXML private Button btnDashboard;
    @FXML private Button btnGenerateTimetable;

    @FXML private VBox viewDataImport;
    @FXML private VBox viewTimetable;
    @FXML private VBox viewDashboard;
    @FXML private VBox sidebar;

    @FXML private GridPane timetableGrid;

    // Durum Etiketleri
    @FXML private Label lblCoursesStatus;
    @FXML private Label lblClassroomsStatus;
    @FXML private Label lblAttendanceStatus;
    @FXML private Label lblStudentsStatus;

    // --- VERİ LİSTELERİ ---
    private List<Course> courses = new ArrayList<>();
    private List<Classroom> classrooms = new ArrayList<>();
    private List<Student> students = new ArrayList<>();
    private List<Enrollment> enrollments = new ArrayList<>();

    private ExamTimetable currentTimetable;

    // --- SERVİSLER ---
    private DataImportService dataImportService = new DataImportService();
    private SchedulerService schedulerService = new SchedulerService();
    private DataRepository repository = new DataRepository();

    @FXML
    public void initialize() {
        showDataImport();
        // Veritabanı varsa yüklemeyi dene, yoksa geç
        try {
            loadDataFromDatabase();
        } catch (Exception e) {
            System.out.println("Database connection skipped.");
        }
    }

    private void loadDataFromDatabase() {
        // Repository boş gelebilir diye try-catch içinde basit kontrol
        try {
            List<Course> dbCourses = repository.loadCourses();
            if (dbCourses != null && !dbCourses.isEmpty()) {
                this.courses = dbCourses;
                updateStatus(lblCoursesStatus, "Loaded from DB (" + courses.size() + ")", true);
            }
            // Diğer yüklemeler buraya eklenebilir...
        } catch (Exception e) {
            // Sessizce geç
        }
    }

    // --- EKRAN GEÇİŞLERİ ---
    @FXML
    private void showDataImport() {
        setViewVisible(viewDataImport);
        setActive(btnDataImport);
        setInactive(btnTimetable);
        setInactive(btnDashboard);
    }

    @FXML
    private void showTimetable() {
        setViewVisible(viewTimetable);
        setActive(btnTimetable);
        setInactive(btnDataImport);
        setInactive(btnDashboard);
        refreshTimetableDisplay();
    }

    @FXML
    private void showDashboard() {
        setViewVisible(viewDashboard);
        setActive(btnDashboard);
        setInactive(btnDataImport);
        setInactive(btnTimetable);
    }

    private void setViewVisible(VBox targetView) {
        if(viewDataImport != null) viewDataImport.setVisible(false);
        if(viewTimetable != null) viewTimetable.setVisible(false);
        if(viewDashboard != null) viewDashboard.setVisible(false);
        if(targetView != null) targetView.setVisible(true);
    }

    // --- CSV YÜKLEME ---
    @FXML
    private void handleLoadCourses() {
        File file = chooseFile("Load Courses CSV");
        if (file != null) {
            try {
                courses = dataImportService.loadCourses(file);
                updateStatus(lblCoursesStatus, file.getName() + " • " + courses.size() + " courses", true);
            } catch (Exception e) {
                showError("Import Error", e.getMessage());
            }
        }
    }

    @FXML
    private void handleLoadClassrooms() {
        File file = chooseFile("Load Classrooms CSV");
        if (file != null) {
            try {
                classrooms = dataImportService.loadClassrooms(file);
                updateStatus(lblClassroomsStatus, file.getName() + " • " + classrooms.size() + " rooms", true);
            } catch (Exception e) {
                showError("Import Error", e.getMessage());
            }
        }
    }

    @FXML
    private void handleLoadStudents() {
        File file = chooseFile("Load Students CSV");
        if (file != null) {
            try {
                students = dataImportService.loadStudents(file);
                updateStatus(lblStudentsStatus, file.getName() + " • " + students.size() + " students", true);
            } catch (Exception e) {
                showError("Import Error", e.getMessage());
            }
        }
    }

    @FXML
    private void handleLoadAttendance() {
        if (courses.isEmpty() || students.isEmpty()) {
            showError("Missing Data", "Please load Courses and Students first.");
            return;
        }
        File file = chooseFile("Load Enrollment CSV");
        if (file != null) {
            try {
                enrollments = dataImportService.loadAttendance(file, courses, students);
                updateStatus(lblAttendanceStatus, file.getName() + " • " + enrollments.size() + " entries", true);
            } catch (Exception e) {
                showError("Import Error", e.getMessage());
            }
        }
    }

    // --- [GÖREV 12] ASENKRON İŞLEM (Hatanın Çözüldüğü Yer) ---
    @FXML
    private void handleGenerateTimetable() {
        // Basit kontrol
        if (courses.isEmpty() || classrooms.isEmpty()) {
            showError("Missing Data", "Cannot generate timetable without Courses and Classrooms.");
            return;
        }

        // UI Kilitleme Metodunu Çağırıyoruz (Aşağıda tanımlı)
        setLoadingState(true);

        // TASK Tanımlama
        Task<ExamTimetable> task = new Task<>() {
            @Override
            protected ExamTimetable call() throws Exception {
                updateMessage("Initializing Scheduler...");
                Thread.sleep(500);

                updateMessage("Running Algorithm...");
                LocalDate startDate = LocalDate.now().plusDays(1);

                // --- DÜZELTİLMİŞ KISIM BURASI ---
                return schedulerService.generateTimetable(
                        courses,
                        classrooms,
                        enrollments,
                        startDate,
                        false,
                        new ArrayList<LocalDate>()
                );
            }
        };

        // Başarılı olursa
        task.setOnSucceeded(e -> {
            setLoadingState(false);
            this.currentTimetable = task.getValue();
            if (currentTimetable != null) {
                showTimetable();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Generation Complete");
                alert.setContentText("Timetable creates successfully!");
                alert.showAndWait();
            } else {
                showError("Failed", "No valid schedule found.");
            }
        });

        // Hata olursa
        task.setOnFailed(e -> {
            setLoadingState(false);
            Throwable ex = task.getException();
            ex.printStackTrace();
            showError("Error", "Generation failed: " + ex.getMessage());
        });

        // Buton yazısını güncelle
        task.messageProperty().addListener((obs, old, newVal) -> {
            if(btnGenerateTimetable != null) btnGenerateTimetable.setText(newVal);
        });

        // Thread'i başlat
        new Thread(task).start();
    }

    private void refreshTimetableDisplay() {
        if (timetableGrid == null || currentTimetable == null) return;
        timetableGrid.getChildren().clear();

        // Basit listeleme (Yer tutucu)
        int row = 1;
        for (Exam exam : currentTimetable.getExams()) {
            if (exam.getSlot() != null) {
                String text = exam.getCourse().getCode() + " @ " +
                        exam.getSlot().getDate() + " " + exam.getSlot().getStartTime();
                timetableGrid.add(new Label(text), 0, row++);
            }
        }
    }

    // --- İŞTE EKSİK OLAN YARDIMCI METOT ---
    // Bu metodu eklemediğin için hata alıyordun. Şimdi burada.
    private void setLoadingState(boolean loading) {
        if (btnGenerateTimetable != null) {
            btnGenerateTimetable.setDisable(loading);
            btnGenerateTimetable.setText(loading ? "Processing..." : "Generate Timetable");
        }
        if (btnDataImport != null) btnDataImport.setDisable(loading);
        if (btnTimetable != null) btnTimetable.setDisable(loading);

        if (viewDataImport != null && viewDataImport.getScene() != null) {
            viewDataImport.getScene().setCursor(loading ? javafx.scene.Cursor.WAIT : javafx.scene.Cursor.DEFAULT);
        }
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }

    private File chooseFile(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        if (btnDataImport != null && btnDataImport.getScene() != null) {
            return fileChooser.showOpenDialog(btnDataImport.getScene().getWindow());
        }
        return null;
    }

    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void updateStatus(Label label, String text, boolean success) {
        if (label != null) {
            label.setText(text);
            label.setStyle(success ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
        }
    }

    private void setActive(Button btn) {
        if (btn != null && !btn.getStyleClass().contains("active")) btn.getStyleClass().add("active");
    }

    private void setInactive(Button btn) {
        if (btn != null) btn.getStyleClass().remove("active");
    }
}