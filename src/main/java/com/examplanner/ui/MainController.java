package com.examplanner.ui;

import com.examplanner.domain.Classroom;
import com.examplanner.domain.Course;
import com.examplanner.domain.Enrollment;
import com.examplanner.domain.Exam;
import com.examplanner.domain.Student;
import com.examplanner.domain.ExamTimetable;
import com.examplanner.services.DataImportService;
import com.examplanner.services.SchedulerService;
import javafx.concurrent.Task;
import java.time.LocalTime;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import java.util.Comparator;
import java.io.PrintWriter;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

public class MainController {

    @FXML
    private Button btnDataImport;
    @FXML
    private Button btnTimetable;
    @FXML
    private Button btnDashboard;
    @FXML
    private Button btnFilter;
    @FXML
    private Button btnStudentPortal;

    @FXML
    private Button btnGenerateDataImport;
    @FXML
    private Button btnGenerateTimetable;

    @FXML
    private VBox viewDataImport;
    @FXML
    private VBox viewTimetable;
    @FXML
    private VBox sidebar;
    @FXML
    private VBox viewDashboard;
    @FXML
    private javafx.scene.chart.BarChart<String, Number> chartExamsPerDay;
    @FXML
    private javafx.scene.chart.PieChart chartRoomUsage;
    @FXML
    private javafx.scene.control.CheckBox chkStrictConstraints;

    @FXML
    private Label lblCoursesStatus;
    @FXML
    private Label lblClassroomsStatus;
    @FXML
    private Label lblAttendanceStatus;
    @FXML
    private Label lblStudentsStatus;

    @FXML
    private GridPane timetableGrid;

    private List<Course> courses = new ArrayList<>();
    private List<Classroom> classrooms = new ArrayList<>();
    private List<Student> students = new ArrayList<>();
    private List<Enrollment> enrollments = new ArrayList<>();
    private List<LocalDate> blackoutDates = new ArrayList<>();
    private ExamTimetable currentTimetable;

    private DataImportService dataImportService = new DataImportService();
    private SchedulerService schedulerService = new SchedulerService();
    private com.examplanner.persistence.DataRepository repository = new com.examplanner.persistence.DataRepository();

    @FXML
    public void initialize() {
        showDataImport();

        // Load data from DB
        List<Course> loadedCourses = repository.loadCourses();
        if (!loadedCourses.isEmpty()) {
            this.courses = loadedCourses;
            lblCoursesStatus.setText("Loaded from DB (" + courses.size() + ")");
            lblCoursesStatus.setStyle("-fx-text-fill: green;");
        }

        List<Classroom> loadedClassrooms = repository.loadClassrooms();
        if (!loadedClassrooms.isEmpty()) {
            this.classrooms = loadedClassrooms;
            lblClassroomsStatus.setText("Loaded from DB (" + classrooms.size() + ")");
            lblClassroomsStatus.setStyle("-fx-text-fill: green;");
        }

        List<Student> loadedStudents = repository.loadStudents();
        if (!loadedStudents.isEmpty()) {
            this.students = loadedStudents;
            lblStudentsStatus.setText("Loaded from DB (" + students.size() + ")");
            lblStudentsStatus.setStyle("-fx-text-fill: green;");
        }

        // Enrollments depend on students and courses
        if (!students.isEmpty() && !courses.isEmpty()) {
            List<Enrollment> loadedEnrollments = repository.loadEnrollments(students, courses);
            if (!loadedEnrollments.isEmpty()) {
                this.enrollments = loadedEnrollments;
                lblAttendanceStatus.setText("Loaded from DB (" + enrollments.size() + ")");
                lblAttendanceStatus.setStyle("-fx-text-fill: green;");

                // Load Timetable if everything else is present
                ExamTimetable loadedTimetable = repository.loadTimetable(courses, classrooms, enrollments);
                if (loadedTimetable != null) {
                    this.currentTimetable = loadedTimetable;
                    refreshTimetable();
                }
            }
        }
    }

    @FXML
    private void showDataImport() {
        viewDataImport.setVisible(true);
        viewTimetable.setVisible(false);
        viewDashboard.setVisible(false);
        if (sidebar != null) {
            sidebar.setVisible(true);
            sidebar.setManaged(true);
        }
        setActive(btnDataImport);
        setInactive(btnTimetable);
        setInactive(btnDashboard);
    }

    @FXML
    private void showDashboard() {
        viewDataImport.setVisible(false);
        viewTimetable.setVisible(false);
        viewDashboard.setVisible(true);

        if (sidebar != null) {
            sidebar.setVisible(true);
            sidebar.setManaged(true);
        }

        setActive(btnDashboard);
        setInactive(btnDataImport);
        setInactive(btnTimetable);

        refreshDashboard();
    }

    @FXML
    private void showTimetable() {
        viewDataImport.setVisible(false);
        viewDashboard.setVisible(false);
        viewTimetable.setVisible(true);

        if (sidebar != null) {
            sidebar.setVisible(false);
            sidebar.setManaged(false);
        }
        setActive(btnTimetable);
        setInactive(btnDataImport);
        setInactive(btnDashboard);
        refreshTimetable();
    }

    private void setActive(Button btn) {
        if (!btn.getStyleClass().contains("active")) {
            btn.getStyleClass().add("active");
        }
    }

    private void setInactive(Button btn) {
        btn.getStyleClass().remove("active");
    }

    @FXML
    private void handleLoadCourses() {
        File file = chooseFile("Load Courses CSV");
        if (file != null) {
            try {
                courses = dataImportService.loadCourses(file);
                repository.saveCourses(courses);
                lblCoursesStatus.setText(file.getName() + " • " + courses.size() + " courses loaded");
                lblCoursesStatus.getStyleClass().add("text-success");
            } catch (Exception e) {
                showError("Error loading courses",
                        "Your file may be empty or formatted incorrectly.\\nError: " + e.getMessage());
                lblCoursesStatus.getStyleClass().add("text-error");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleLoadClassrooms() {
        File file = chooseFile("Load Classrooms CSV");
        if (file != null) {
            try {
                classrooms = dataImportService.loadClassrooms(file);
                repository.saveClassrooms(classrooms);
                lblClassroomsStatus.setText(file.getName() + " • " + classrooms.size() + " classrooms loaded");
                lblClassroomsStatus.getStyleClass().add("text-success");
            } catch (Exception e) {
                showError("Error loading classrooms",
                        "Your file may be empty or formatted incorrectly.\\nError: " + e.getMessage());
                lblClassroomsStatus.getStyleClass().add("text-error");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleLoadStudents() {
        File file = chooseFile("Load Students CSV");
        if (file != null) {
            try {
                students = dataImportService.loadStudents(file);
                repository.saveStudents(students);
                lblStudentsStatus.setText(file.getName() + " • " + students.size() + " students loaded");
                lblStudentsStatus.getStyleClass().add("text-success");
            } catch (Exception e) {
                showError("Error loading students",
                        "Your file may be empty or formatted incorrectly.\\nError: " + e.getMessage());
                lblStudentsStatus.getStyleClass().add("text-error");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleLoadAttendance() {
        if (courses.isEmpty()) {
            showError("Pre-requisite missing", "Please load courses first.");
            return;
        }
        File file = chooseFile("Load Attendance CSV");
        if (file != null) {
            try {
                enrollments = dataImportService.loadAttendance(file, courses, students);
                repository.saveEnrollments(enrollments);
                lblAttendanceStatus.setText(file.getName() + " • " + enrollments.size() + " enrollments loaded");
                lblAttendanceStatus.getStyleClass().add("text-success");
            } catch (Exception e) {
                showError("Error loading attendance",
                        "Your file may be empty or formatted incorrectly.\\nError: " + e.getMessage());
                lblAttendanceStatus.getStyleClass().add("text-error");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleGenerateTimetable() {
        System.out.println("=== GENERATE TIMETABLE BUTTON CLICKED ===");

        if (courses.isEmpty() || classrooms.isEmpty() || enrollments.isEmpty()) {
            System.out.println("ERROR: Missing data!");
            System.out.println("Courses: " + courses.size());
            System.out.println("Classrooms: " + classrooms.size());
            System.out.println("Enrollments: " + enrollments.size());
            showError("Missing Data", "Please load Courses, Classrooms, and Attendance data first.");
            return;
        }

        setLoadingState(true);

        Task<ExamTimetable> task = new Task<>() {
            @Override
            protected ExamTimetable call() throws Exception {
                System.out.println("Starting timetable generation...");
                LocalDate startDate = LocalDate.now().plusDays(1);
                System.out.println("Start date: " + startDate);
                System.out.println("Calling scheduler service...");
                boolean strictMode = chkStrictConstraints.isSelected();
                return schedulerService.generateTimetable(courses, classrooms, enrollments, startDate, strictMode,
                        blackoutDates);
            }
        };

        task.setOnSucceeded(e -> {
            try {
                this.currentTimetable = task.getValue();
                repository.saveTimetable(currentTimetable);
                System.out.println("Timetable generated successfully!");
                System.out.println(
                        "Number of exams: " + (currentTimetable != null ? currentTimetable.getExams().size() : "null"));

                refreshTimetable();
                showTimetable();
                setLoadingState(false);
                System.out.println("UI updated successfully!");
            } catch (Exception ex) {
                System.err.println("CRITICAL UI ERROR:");
                ex.printStackTrace();
                showError("UI Error", "Failed to render timetable: " + ex.getMessage());
                setLoadingState(false);
            }
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            System.err.println("ERROR during timetable generation:");
            ex.printStackTrace();
            showError("Scheduling Failed",
                    "Could not generate timetable.\n\nError: " + ex.getMessage() + "\n\nCheck terminal for details.");
            setLoadingState(false);
        });

        new Thread(task).start();
    }

    private void setLoadingState(boolean loading) {
        String text = loading ? "Generating..." : "Generate Timetable";

        if (btnGenerateDataImport != null) {
            btnGenerateDataImport.setDisable(loading);
            btnGenerateDataImport.setText(text);
        }
        if (btnGenerateTimetable != null) {
            btnGenerateTimetable.setDisable(loading);
            btnGenerateTimetable.setText(text);
        }

        if (viewDataImport.getScene() != null) {
            viewDataImport.getScene().setCursor(loading ? javafx.scene.Cursor.WAIT : javafx.scene.Cursor.DEFAULT);
        }
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }

    @FXML
    private void handleFilter() {
        if (currentTimetable == null) {
            showError("No Timetable", "Please generate a timetable first.");
            return;
        }

        List<String> choices = new ArrayList<>();
        choices.add("Student");
        choices.add("Course");

        javafx.scene.control.ChoiceDialog<String> dialog = new javafx.scene.control.ChoiceDialog<>("Student", choices);
        dialog.setTitle("Filter Timetable");
        dialog.setHeaderText("Select Filter Type");
        dialog.setContentText("Choose what to filter by:");

        java.util.Optional<String> result = dialog.showAndWait();
        result.ifPresent(type -> {
            if (type.equals("Student")) {
                filterByStudent();
            } else {
                filterByCourse();
            }
        });
    }

    private void filterByStudent() {
        List<Student> studentList = enrollments.stream()
                .map(Enrollment::getStudent)
                .distinct()
                .sorted(Comparator.comparing(Student::getName))
                .collect(Collectors.toList());

        SearchableDialog<Student> dialog = new SearchableDialog<>(
                "Select Student",
                "Filter by Student",
                studentList,
                (student, query) -> student.getName().toLowerCase().contains(query)
                        || student.getId().toLowerCase().contains(query));

        dialog.showAndWait().ifPresent(student -> {
            List<Exam> exams = currentTimetable.getExamsForStudent(student);
            showFilteredExams("Exams for " + student.getName(), exams);
        });
    }

    private void filterByCourse() {
        List<Course> courseList = new ArrayList<>(courses);
        courseList.sort(Comparator.comparing(Course::getName));

        SearchableDialog<Course> dialog = new SearchableDialog<>(
                "Select Course",
                "Filter by Course",
                courseList,
                (course, query) -> course.getName().toLowerCase().contains(query)
                        || course.getCode().toLowerCase().contains(query));

        dialog.showAndWait().ifPresent(course -> {
            List<Exam> exams = currentTimetable.getExamsForCourse(course);
            showFilteredExams("Exams for " + course.getName(), exams);
        });
    }

    @FXML
    private void handleExport() {
        if (currentTimetable == null || currentTimetable.getExams().isEmpty()) {
            showError("No Timetable", "Nothing to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Timetable to CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("timetable_export.csv");
        File file = fileChooser.showSaveDialog(btnTimetable.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("Date,Time,Course Code,Course Name,Classroom");
                List<Exam> sortedExams = new ArrayList<>(currentTimetable.getExams());
                sortedExams.sort(Comparator.comparing((Exam e) -> e.getSlot().getDate())
                        .thenComparing(e -> e.getSlot().getStartTime()));

                for (Exam e : sortedExams) {
                    writer.printf("%s,%s,%s,%s,%s%n",
                            e.getSlot().getDate(),
                            e.getSlot().getStartTime(),
                            e.getCourse().getCode(),
                            e.getCourse().getName(),
                            e.getClassroom().getName());
                }

                showInformation("Export Successful", "Timetable exported to " + file.getName());
            } catch (Exception e) {
                e.printStackTrace();
                showError("Export Failed", "Could not save file: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleConflicts() {
        if (currentTimetable == null) {
            showError("No Timetable", "Please generate a timetable first.");
            return;
        }

        // Detect "Conflicts" -> Students having > 1 exam per day (Soft constraint
        // warning)
        // Since hard Constraint is max 2 exams/day, a "conflict" here could mean 2
        // exams on the same day
        // to warn the user about heavy load, OR checking for < 30 min gap if we allowed
        // it (but we didn't).

        // Group exams by student -> date
        // Note: This is computationally expensive O(N_students * M_exams), but for the
        // UI button it's okay?
        // Better: Iterate exams, and for each exam find enrolled students.

        Map<Student, Map<LocalDate, List<Exam>>> studentDailyLoad = new HashMap<>();

        for (Exam exam : currentTimetable.getExams()) {
            List<Student> enrolled = enrollments.stream()
                    .filter(e -> e.getCourse().getCode().equals(exam.getCourse().getCode()))
                    .map(Enrollment::getStudent)
                    .collect(Collectors.toList());

            for (Student s : enrolled) {
                studentDailyLoad.putIfAbsent(s, new HashMap<>());
                Map<LocalDate, List<Exam>> days = studentDailyLoad.get(s);
                LocalDate d = exam.getSlot().getDate();
                days.putIfAbsent(d, new ArrayList<>());
                days.get(d).add(exam);
            }
        }

        // Filter those with > 1 exam
        List<String> reportLines = new ArrayList<>();

        for (Map.Entry<Student, Map<LocalDate, List<Exam>>> entry : studentDailyLoad.entrySet()) {
            Student s = entry.getKey();
            for (Map.Entry<LocalDate, List<Exam>> dayEntry : entry.getValue().entrySet()) {
                if (dayEntry.getValue().size() > 1) {
                    reportLines.add("🔴 " + s.getName() + " has " + dayEntry.getValue().size() + " exams on "
                            + dayEntry.getKey());
                    // Maybe list the exams?
                    reportLines.add("   " + dayEntry.getValue().stream().map(e -> e.getCourse().getCode())
                            .collect(Collectors.joining(", ")));
                }
            }
        }

        if (reportLines.isEmpty()) {
            showInformation("No Conflicts", "No students have more than 1 exam per day.");
        } else {
            showScrollableDialog("Exam Load Conflicts", reportLines);
        }
    }

    @FXML
    private void handleStudentPortal() {
        if (students.isEmpty()) {
            showError("No Data", "Please load students first.");
            return;
        }

        // Simulating a "Login"
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
        dialog.setTitle("Student Portal Login");
        dialog.setHeaderText("Welcome Student");
        dialog.setContentText("Please enter your Student ID:");

        dialog.showAndWait().ifPresent(id -> {
            Student found = students.stream()
                    .filter(s -> s.getId().equalsIgnoreCase(id.trim()))
                    .findFirst()
                    .orElse(null);

            if (found != null) {
                if (currentTimetable != null) {
                    List<Exam> exams = currentTimetable.getExamsForStudent(found);
                    showFilteredExams("Welcome, " + found.getName(), exams);
                } else {
                    // Even if no timetable, show we found the student but no exams yet
                    showInformation("Welcome " + found.getName(), "No timetable generated yet.");
                }
            } else {
                showError("Login Failed", "Student ID not found: " + id);
            }
        });
    }

    @FXML
    private void handleBlackoutDatesConfig() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Blackout Dates");

        VBox root = new VBox(15);
        root.setPadding(new javafx.geometry.Insets(20));
        root.setPrefWidth(300);

        Label lbl = new Label("Manage Blackout Dates");
        lbl.getStyleClass().add("section-title");

        ListView<LocalDate> listView = new ListView<>();
        listView.getItems().addAll(blackoutDates);
        listView.setPrefHeight(200);

        javafx.scene.control.DatePicker datePicker = new javafx.scene.control.DatePicker();
        datePicker.setPromptText("Select Date");

        Button btnAdd = new Button("Add");
        btnAdd.setOnAction(e -> {
            LocalDate date = datePicker.getValue();
            if (date != null && !listView.getItems().contains(date)) {
                listView.getItems().add(date);
                datePicker.setValue(null);
            }
        });

        Button btnRemove = new Button("Remove Selected");
        btnRemove.setOnAction(e -> {
            LocalDate selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                listView.getItems().remove(selected);
            }
        });

        Button btnSave = new Button("Save & Close");
        btnSave.getStyleClass().add("primary-button");
        btnSave.setOnAction(e -> {
            this.blackoutDates = new ArrayList<>(listView.getItems());
            dialog.close();
            showInformation("Updated", "Blackout dates saved. Generating timetable will now skip these dates.");
        });

        HBox inputRow = new HBox(10, datePicker, btnAdd);
        inputRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        root.getChildren().addAll(lbl, listView, inputRow, btnRemove, new javafx.scene.control.Separator(), btnSave);
        Scene scene = new Scene(root);
        // Copy stylesheet if available
        if (root.getScene() != null) {
            scene.getStylesheets().addAll(root.getScene().getStylesheets());
        }

        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void showInformation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showScrollableDialog(String title, List<String> lines) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(10);
        root.setPadding(new javafx.geometry.Insets(20));

        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ListView<String> list = new ListView<>();
        list.getItems().addAll(lines);

        Button close = new Button("Close");
        close.setOnAction(e -> dialog.close());

        root.getChildren().addAll(lblTitle, list, close);
        Scene scene = new Scene(root, 500, 600);

        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void showFilteredExams(String titleText, List<Exam> exams) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox();
        root.getStyleClass().add("modal-window");
        root.setPrefSize(400, 500);

        VBox header = new VBox(5);
        header.getStyleClass().add("modal-header");

        Label title = new Label(titleText);
        title.getStyleClass().add("section-title");
        title.setStyle("-fx-font-size: 16px;");

        Label subtitle = new Label(exams.size() + " exams found");
        subtitle.getStyleClass().addAll("label", "text-secondary");

        header.getChildren().addAll(title, subtitle);

        VBox content = new VBox();
        content.getStyleClass().add("modal-content");
        javafx.scene.layout.VBox.setVgrow(content, javafx.scene.layout.Priority.ALWAYS);

        ListView<Exam> listView = new ListView<>();
        listView.getStyleClass().add("student-list-view");
        listView.getItems().addAll(exams);
        listView.setCellFactory(param -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Exam item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String text = item.getCourse().getCode() + " - " + item.getCourse().getName() + "\n" +
                            "🕒 " + item.getSlot().getDate() + " " + item.getSlot().getStartTime() + "\n" +
                            "📍 " + item.getClassroom().getName();
                    setText(text);
                    getStyleClass().add("student-list-cell");
                }
            }
        });

        javafx.scene.layout.VBox.setVgrow(listView, javafx.scene.layout.Priority.ALWAYS);
        content.getChildren().add(listView);

        HBox footer = new HBox();
        footer.setStyle(
                "-fx-padding: 15; -fx-alignment: center-right; -fx-background-color: #F3F4F6; -fx-background-radius: 0 0 12 12;");

        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("secondary-button");
        closeBtn.setOnAction(e -> dialog.close());
        footer.getChildren().add(closeBtn);

        root.getChildren().addAll(header, content, footer);

        Scene scene = new Scene(root);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        scene.getStylesheets().add(getClass().getResource("/com/examplanner/ui/style.css").toExternalForm());

        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void refreshTimetable() {
        try {
            timetableGrid.getChildren().clear();

            // Clear constraints
            timetableGrid.getColumnConstraints().clear();
            timetableGrid.getRowConstraints().clear();

            if (currentTimetable == null || currentTimetable.getExams().isEmpty()) {
                Label placeholder = new Label("No exams scheduled. Click 'Generate Timetable' to begin.");
                placeholder.getStyleClass().add("section-subtitle");
                timetableGrid.add(placeholder, 0, 0);
                return;
            }

            // --- 1. Setup Grid Dimensions (Fixed) ---
            double SLOT_HEIGHT = 50.0; // 50px per 30 mins
            int START_HOUR = 9;
            int END_HOUR = 18;
            int TOTAL_SLOTS = (END_HOUR - START_HOUR) * 2 + 1; // 9:00 to 18:00 inclusive (last slot starts at 18:00)

            // Setup Rows
            javafx.scene.layout.RowConstraints headerRow = new javafx.scene.layout.RowConstraints();
            headerRow.setMinHeight(40);
            headerRow.setPrefHeight(40);
            timetableGrid.getRowConstraints().add(headerRow); // Row 0 (Headers)

            for (int i = 0; i < TOTAL_SLOTS; i++) {
                javafx.scene.layout.RowConstraints row = new javafx.scene.layout.RowConstraints();
                row.setMinHeight(SLOT_HEIGHT);
                row.setPrefHeight(SLOT_HEIGHT);
                row.setMaxHeight(SLOT_HEIGHT);
                row.setVgrow(javafx.scene.layout.Priority.NEVER);
                timetableGrid.getRowConstraints().add(row);
            }

            // Setup Columns (Time Label Col + Day Cols)
            javafx.scene.layout.ColumnConstraints timeCol = new javafx.scene.layout.ColumnConstraints();
            timeCol.setMinWidth(60);
            timeCol.setPrefWidth(60);
            timetableGrid.getColumnConstraints().add(timeCol); // Col 0

            // Determine Start Date (Min Date from exams, or today)
            LocalDate startDate = LocalDate.now().plusDays(1); // Default
            if (!currentTimetable.getExams().isEmpty()) {
                startDate = currentTimetable.getExams().stream()
                        .map(e -> e.getSlot().getDate())
                        .min(LocalDate::compareTo)
                        .orElse(LocalDate.now());
            }

            // Create list of 7 days starting from startDate
            List<LocalDate> dateList = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                dateList.add(startDate.plusDays(i));
            }

            // Add constraints for each day column (7 columns)
            for (int i = 0; i < dateList.size(); i++) {
                javafx.scene.layout.ColumnConstraints dayCol = new javafx.scene.layout.ColumnConstraints();
                dayCol.setPercentWidth(100.0 / dateList.size()); // Distribute remaining width evenly (1/7 roughly)
                dayCol.setHgrow(javafx.scene.layout.Priority.ALWAYS);
                timetableGrid.getColumnConstraints().add(dayCol);
            }

            // --- 2. Render Headers and Time Labels ---
            DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE");
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            // Time Labels
            int row = 1;
            for (int hour = START_HOUR; hour <= END_HOUR; hour++) {
                for (int min = 0; min < 60; min += 30) {
                    if (hour == END_HOUR && min > 0)
                        break; // Stop at 18:00

                    String timeStr = LocalTime.of(hour, min).format(timeFormatter);
                    Label timeLabel = new Label(timeStr);
                    timeLabel.getStyleClass().add("time-label");
                    // Align top-right of the cell
                    javafx.scene.layout.GridPane.setValignment(timeLabel, javafx.geometry.VPos.TOP);
                    javafx.scene.layout.GridPane.setHalignment(timeLabel, javafx.geometry.HPos.RIGHT);
                    timetableGrid.add(timeLabel, 0, row);
                    row++;
                }
            }

            // Day Headers
            for (int i = 0; i < dateList.size(); i++) {
                LocalDate date = dateList.get(i);
                VBox header = new VBox();
                header.getStyleClass().add("grid-header");
                header.setAlignment(javafx.geometry.Pos.CENTER);

                Label dayLabel = new Label(date.format(dayFormatter));
                dayLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: -fx-color-text;");

                Label dateLabel = new Label(date.format(dateFormatter));
                dateLabel.setStyle("-fx-text-fill: -fx-color-text-secondary; -fx-font-size: 11px;");

                header.getChildren().addAll(dayLabel, dateLabel);
                timetableGrid.add(header, i + 1, 0);
            }

            // --- 3. advanced Layout: Place Exams in AnchorPanes per Day ---
            String[] colors = { "purple", "orange", "pink", "blue", "green", "red" };

            for (int i = 0; i < dateList.size(); i++) {
                LocalDate day = dateList.get(i);
                int colIndex = i + 1;

                // Create a container for this day's events
                javafx.scene.layout.AnchorPane dayPane = new javafx.scene.layout.AnchorPane();
                // Allow it to span all time rows
                timetableGrid.add(dayPane, colIndex, 1, 1, TOTAL_SLOTS);

                // Filter exams for this day
                List<Exam> dayExams = currentTimetable.getExams().stream()
                        .filter(e -> e.getSlot().getDate().equals(day))
                        .sorted(Comparator.comparing(e -> e.getSlot().getStartTime()))
                        .collect(Collectors.toList());

                // Algorithm to place concurrent exams side-by-side
                arrangeDayExams(dayPane, dayExams, SLOT_HEIGHT, START_HOUR, colors);

                // DRAG AND DROP - TARGET (DROP ZONE)
                final LocalDate targetDate = day;
                dayPane.setOnDragOver(event -> {
                    if (event.getGestureSource() != dayPane && event.getDragboard().hasString()) {
                        event.acceptTransferModes(javafx.scene.input.TransferMode.MOVE);
                    }
                    event.consume();
                });

                dayPane.setOnDragDropped(event -> {
                    javafx.scene.input.Dragboard db = event.getDragboard();
                    boolean success = false;
                    if (db.hasString()) {
                        String data = db.getString();
                        // Parse "CODE|DATE|TIME"
                        String[] parts = data.split("\\|");
                        if (parts.length == 3) {
                            String courseCode = parts[0];
                            // Find the exam object
                            Exam draggedExam = currentTimetable.getExams().stream()
                                    .filter(e -> e.getCourse().getCode().equals(courseCode))
                                    .findFirst()
                                    .orElse(null);

                            if (draggedExam != null) {
                                // Calculate new Time based on Y coordinate
                                double dropY = event.getY();
                                // SLOT_HEIGHT (50) = 30 mins
                                int slotsFromTop = (int) (dropY / SLOT_HEIGHT);
                                LocalTime newStartTime = LocalTime.of(START_HOUR, 0).plusMinutes(slotsFromTop * 30);
                                LocalTime newEndTime = newStartTime
                                        .plusMinutes(draggedExam.getCourse().getExamDurationMinutes());

                                // Constrain to 18:00
                                if (newEndTime.isAfter(LocalTime.of(18, 00))) {
                                    showError("Invalid Move", "Exam extends beyond 18:00.");
                                } else {
                                    // Update Exam Slot
                                    // TODO: Check Room Availability here?
                                    // For now, simpler: Just move.
                                    javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(
                                            javafx.scene.control.Alert.AlertType.CONFIRMATION);
                                    confirm.setTitle("Confirm Move");
                                    confirm.setHeaderText("Re-schedule Exam");
                                    confirm.setContentText(
                                            "Move " + courseCode + " to " + targetDate + " at " + newStartTime + "?");

                                    if (confirm.showAndWait().get() == javafx.scene.control.ButtonType.OK) {
                                        draggedExam.getSlot().setDate(targetDate);
                                        draggedExam.getSlot().setStartTime(newStartTime);
                                        draggedExam.getSlot().setEndTime(newEndTime);
                                        refreshTimetable(); // Re-render
                                    }
                                }
                                success = true;
                            }
                        }
                    }
                    event.setDropCompleted(success);
                    event.consume();
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Rendering Error", "Failed to refresh timetable: " + e.getMessage());
        }
    }

    private void refreshDashboard() {
        if (currentTimetable == null) {
            return;
        }

        // 1. Exams Per Day (Bar Chart)
        chartExamsPerDay.getData().clear();
        Map<LocalDate, Long> examsByDate = currentTimetable.getExams().stream()
                .collect(Collectors.groupingBy(e -> e.getSlot().getDate(), Collectors.counting()));

        javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
        series.setName("Exams");

        // Sort by date
        examsByDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    series.getData()
                            .add(new javafx.scene.chart.XYChart.Data<>(entry.getKey().toString(), entry.getValue()));
                });

        chartExamsPerDay.getData().add(series);

        // 2. Room Utilization (Pie Chart)
        chartRoomUsage.getData().clear();
        Map<String, Long> roomUsage = currentTimetable.getExams().stream()
                .collect(Collectors.groupingBy(e -> e.getClassroom().getName(), Collectors.counting()));

        roomUsage.forEach((room, count) -> {
            chartRoomUsage.getData().add(new javafx.scene.chart.PieChart.Data(room, count));
        });
    }

    // Helper: Arrange exams in a day column to avoid visual overlap
    private void arrangeDayExams(javafx.scene.layout.AnchorPane pane, List<Exam> exams, double slotHeight,
            int startHour, String[] colors) {
        if (exams.isEmpty())
            return;

        // Simple Greedy Coloring / Packing algorithm
        // 1. Calculate vertical position (top, height) for each exam
        // 2. Detect overlapping groups (clusters)
        // 3. For each cluster, distribute horizontally

        class RenderBlock {
            Exam exam;
            double top;
            double height;
            double startMin;
            double endMin;
            int colIndex = 0;
            int totalCols = 1;

            RenderBlock(Exam e) {
                this.exam = e;
                LocalTime start = e.getSlot().getStartTime();
                int minutesFromStart = (start.getHour() - startHour) * 60 + start.getMinute();
                this.top = (minutesFromStart / 30.0) * slotHeight;
                this.height = (e.getCourse().getExamDurationMinutes() / 30.0) * slotHeight;
                this.startMin = minutesFromStart;
                this.endMin = minutesFromStart + e.getCourse().getExamDurationMinutes();
            }
        }

        List<RenderBlock> blocks = exams.stream().map(RenderBlock::new).collect(Collectors.toList());

        // Group into clusters of overlapping events
        // Two events overlap if (Start1 < End2) and (Start2 < End1)
        List<List<RenderBlock>> clusters = new ArrayList<>();
        if (!blocks.isEmpty()) {
            List<RenderBlock> currentCluster = new ArrayList<>();
            currentCluster.add(blocks.get(0));
            clusters.add(currentCluster);

            double clusterEnd = blocks.get(0).endMin;

            for (int i = 1; i < blocks.size(); i++) {
                RenderBlock b = blocks.get(i);
                if (b.startMin < clusterEnd) {
                    // Overlaps with the current cluster context
                    currentCluster.add(b);
                    clusterEnd = Math.max(clusterEnd, b.endMin);
                } else {
                    // New cluster
                    currentCluster = new ArrayList<>();
                    currentCluster.add(b);
                    clusters.add(currentCluster);
                    clusterEnd = b.endMin;
                }
            }
        }

        // Process each cluster to assign columns
        for (List<RenderBlock> cluster : clusters) {
            // Simple packing: "First Fit".
            // We need to assign valid column indices 0..N such that no two events with same
            // column overlap

            // Sort by start time (already sorted)
            // But for packing, maybe just iterate
            List<List<RenderBlock>> columns = new ArrayList<>();

            for (RenderBlock block : cluster) {
                boolean placed = false;
                for (int c = 0; c < columns.size(); c++) {
                    List<RenderBlock> colEvents = columns.get(c);
                    // Check if block overlaps with last event in this column
                    // Since specific column's events are sequential, we just check the last one
                    RenderBlock last = colEvents.get(colEvents.size() - 1);
                    if (block.startMin >= last.endMin) {
                        colEvents.add(block);
                        block.colIndex = c;
                        placed = true;
                        break;
                    }
                }

                if (!placed) {
                    // Create new column
                    List<RenderBlock> newCol = new ArrayList<>();
                    newCol.add(block);
                    columns.add(newCol);
                    block.colIndex = columns.size() - 1;
                }
            }

            int maxCols = columns.size();
            for (RenderBlock block : cluster) {
                block.totalCols = maxCols;
            }
        }

        // Render to Pane
        int colorIdx = 0;
        for (RenderBlock block : blocks) {
            VBox card = new VBox();
            String colorClass = "exam-card-" + colors[colorIdx % colors.length];
            // Cycle color per exam? Or per course code?
            // Let's cycle just to look nice
            colorIdx++;

            card.getStyleClass().addAll("exam-card", colorClass);

            Label title = new Label(block.exam.getCourse().getCode());
            title.getStyleClass().add("exam-card-title");

            // Use smaller font if compressed
            if (block.totalCols > 2)
                title.setStyle("-fx-font-size: 9px;");

            Label detail = new Label("📍 " + block.exam.getClassroom().getName());
            detail.getStyleClass().add("exam-card-detail");
            if (block.totalCols > 2)
                detail.setStyle("-fx-font-size: 8px;");

            card.getChildren().addAll(title, detail);

            // Positioning
            // Use setTopAnchor for Y (handled by AnchorPane)
            // But manually handle X via listener to avoid binding/layout conflicts

            pane.getChildren().add(card);

            javafx.scene.layout.AnchorPane.setTopAnchor(card, block.top);

            // Set height
            card.setMinHeight(block.height - 2);
            card.setPrefHeight(block.height - 2);
            card.setMaxHeight(block.height - 2);

            card.setOnMouseClicked(e -> showExamDetails(block.exam));

            // DRAG AND DROP - SOURCE
            card.setOnDragDetected(event -> {
                javafx.scene.input.Dragboard db = card.startDragAndDrop(javafx.scene.input.TransferMode.MOVE);
                javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                // Store Exam details as string reference: "COURSE_CODE|DATE|TIME"
                content.putString(block.exam.getCourse().getCode() + "|" + block.exam.getSlot().getDate() + "|"
                        + block.exam.getSlot().getStartTime());
                db.setContent(content);
                event.consume();
            });

            // Use listener for Layout X and Width instead of binding property
            javafx.beans.value.ChangeListener<Number> widthListener = (obs, oldVal, newVal) -> {
                double totalWidth = pane.getWidth();
                if (totalWidth <= 0)
                    return;

                double colWidth = totalWidth / block.totalCols;
                double newX = (colWidth * block.colIndex) + 2;
                double newW = colWidth - 4;

                card.setLayoutX(newX);
                card.setPrefWidth(newW);
                card.setMinWidth(newW);
                card.setMaxWidth(newW);
            };

            pane.widthProperty().addListener(widthListener);
            // Initial call
            widthListener.changed(pane.widthProperty(), null, pane.getWidth());
        }
    }

    private void showExamDetails(Exam exam) {
        List<Student> students = enrollments.stream()
                .filter(e -> e.getCourse().getCode().equals(exam.getCourse().getCode()))
                .map(Enrollment::getStudent)
                .sorted(Comparator.comparing(Student::getName))
                .collect(Collectors.toList());

        // Create Custom Dialog Stage
        // Create Custom Dialog Stage
        Stage dialog = new Stage();
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);

        // Root Container
        VBox root = new VBox();
        root.getStyleClass().add("modal-window");
        root.setPrefSize(400, 500);

        // Header
        VBox header = new VBox(5);
        header.getStyleClass().add("modal-header");

        Label title = new Label(exam.getCourse().getName());
        title.getStyleClass().add("section-title");
        title.setStyle("-fx-font-size: 16px;");

        Label subtitle = new Label(exam.getCourse().getCode() + " • " + exam.getClassroom().getName());
        subtitle.getStyleClass().addAll("label", "text-secondary");

        HBox metaBox = new HBox(10);
        Label timeLabel = new Label("🕒 " + exam.getSlot().getStartTime() + " - " + exam.getSlot().getEndTime());
        timeLabel.getStyleClass().addAll("label", "text-secondary");
        Label countLabel = new Label("👥 " + students.size() + " Students");
        countLabel.getStyleClass().addAll("label", "text-secondary");
        metaBox.getChildren().addAll(timeLabel, countLabel);

        header.getChildren().addAll(title, subtitle, metaBox);

        // Content (List)
        VBox content = new VBox();
        content.getStyleClass().add("modal-content");
        javafx.scene.layout.VBox.setVgrow(content, javafx.scene.layout.Priority.ALWAYS);

        javafx.scene.control.ListView<Student> listView = new javafx.scene.control.ListView<>();
        listView.getStyleClass().add("student-list-view");
        listView.getItems().addAll(students);
        listView.setCellFactory(param -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Student item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getName() + " (" + item.getId() + ")");
                    getStyleClass().add("student-list-cell");
                }
            }
        });

        javafx.scene.layout.VBox.setVgrow(listView, javafx.scene.layout.Priority.ALWAYS);
        content.getChildren().add(listView);

        // Footer (Close Button)
        HBox footer = new HBox();
        footer.setStyle(
                "-fx-padding: 15; -fx-alignment: center-right; -fx-background-color: #F3F4F6; -fx-background-radius: 0 0 12 12;");

        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("secondary-button");
        closeBtn.setOnAction(e -> dialog.close());
        footer.getChildren().add(closeBtn);

        root.getChildren().addAll(header, content, footer);

        // Scene
        javafx.scene.Scene scene = new javafx.scene.Scene(root);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        scene.getStylesheets().add(getClass().getResource("/com/examplanner/ui/style.css").toExternalForm());

        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private File chooseFile(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        return fileChooser.showOpenDialog(btnDataImport.getScene().getWindow());
    }

    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
