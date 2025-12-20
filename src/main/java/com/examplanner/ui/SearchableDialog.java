package com.examplanner.ui;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Enhanced searchable dialog with:
 * - i18n localization support
 * - Empty state handling
 * - Keyboard navigation (Enter to select, Escape to cancel)
 * - Search debounce for performance
 * - Sorting options
 * - Recent searches tracking
 * - Enhanced list cells with additional info
 */
public class SearchableDialog<T> extends Dialog<T> {

    private final FilteredList<T> filteredList;
    private final ListView<T> listView;
    private final TextField searchField;
    private Label resultCount;
    private final ResourceBundle bundle;
    private final boolean isDarkMode;

    // Recent searches (static to persist across dialog instances)
    private static final LinkedList<String> recentSearches = new LinkedList<>();
    private static final int MAX_RECENT_SEARCHES = 5;

    // Sorting
    private final ObservableList<T> observableItems;

    // Debounce
    private PauseTransition debounce;

    /**
     * Constructor with ResourceBundle for i18n support
     */
    public SearchableDialog(String title, String headerText, List<T> items,
            BiPredicate<T, String> filterLogic, ResourceBundle bundle) {
        this(title, headerText, items, filterLogic, bundle, null, null, false);
    }

    /**
     * Full constructor with enrollment count function for enhanced display
     */
    public SearchableDialog(String title, String headerText, List<T> items,
            BiPredicate<T, String> filterLogic, ResourceBundle bundle,
            Function<T, Integer> enrollmentCountFunc,
            Function<T, Integer> examCountFunc) {
        this(title, headerText, items, filterLogic, bundle, enrollmentCountFunc, examCountFunc, false);
    }

    /**
     * Full constructor with dark mode support
     */
    public SearchableDialog(String title, String headerText, List<T> items,
            BiPredicate<T, String> filterLogic, ResourceBundle bundle,
            Function<T, Integer> enrollmentCountFunc,
            Function<T, Integer> examCountFunc,
            boolean isDarkMode) {
        this.bundle = bundle;
        this.isDarkMode = isDarkMode;

        setTitle(title);
        setHeaderText(null);
        initStyle(StageStyle.UTILITY);

        // Apply application stylesheet
        DialogPane dialogPane = getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        dialogPane.getStyleClass().add("searchable-dialog");

        // Apply dark mode class if needed
        if (isDarkMode) {
            dialogPane.getStyleClass().add("dark-mode");
        }

        // Main container
        VBox mainContainer = new VBox(15);
        mainContainer.getStyleClass().add("modal-content");
        if (isDarkMode) {
            mainContainer.getStyleClass().add("dark-mode");
        }
        mainContainer.setPadding(new Insets(20));

        // Header section
        VBox headerBox = new VBox(5);
        Label titleLabel = new Label(getBundleString("studentSearch.title", title));
        titleLabel.getStyleClass().addAll("section-title", "dialog-title");

        Label subtitleLabel = new Label(getBundleString("studentSearch.subtitle", headerText));
        subtitleLabel.getStyleClass().addAll("label", "text-secondary", "dialog-subtitle");

        headerBox.getChildren().addAll(titleLabel, subtitleLabel);

        // Search bar with icon
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.getStyleClass().add("modern-search-bar");

        FontIcon searchIcon = new FontIcon("fas-search");
        searchIcon.setIconSize(14);
        searchIcon.getStyleClass().add("search-icon");
        HBox.setMargin(searchIcon, new Insets(0, 0, 0, 12));

        searchField = new TextField();
        searchField.setPromptText(getBundleString("studentSearch.searchPrompt", "Search by name or ID..."));
        searchField.getStyleClass().add("modern-search-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        searchBox.getChildren().addAll(searchIcon, searchField);

        // Sorting dropdown
        HBox sortBox = new HBox(10);
        sortBox.setAlignment(Pos.CENTER_LEFT);

        Label sortLabel = new Label(getBundleString("studentSearch.sortBy", "Sort by") + ":");
        sortLabel.getStyleClass().addAll("label", "text-secondary", "sort-label");

        ComboBox<String> sortCombo = new ComboBox<>();
        sortCombo.getStyleClass().add("schedule-selector");
        sortCombo.getItems().addAll(
                getBundleString("studentSearch.sortByName", "Name (A-Z)"),
                getBundleString("studentSearch.sortByNameDesc", "Name (Z-A)"),
                getBundleString("studentSearch.sortById", "ID"));
        if (enrollmentCountFunc != null) {
            sortCombo.getItems().add(getBundleString("studentSearch.sortByEnrollments", "Enrollments"));
        }
        sortCombo.getSelectionModel().selectFirst();
        sortCombo.setPrefWidth(130);

        sortBox.getChildren().addAll(sortLabel, sortCombo);

        // Initialize list
        observableItems = FXCollections.observableArrayList(items);
        filteredList = new FilteredList<>(observableItems, p -> true);

        listView = new ListView<>(filteredList);
        listView.setPrefHeight(300);
        listView.setPrefWidth(400);
        listView.getStyleClass().add("student-list-view");

        // Empty state check
        if (items.isEmpty()) {
            // Show empty state
            VBox emptyState = createEmptyState();
            mainContainer.getChildren().addAll(headerBox, emptyState);
        } else {
            // Custom cell factory with enhanced display
            listView.setCellFactory(lv -> new ListCell<T>() {
                @Override
                protected void updateItem(T item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        HBox cellContent = new HBox(10);
                        cellContent.setAlignment(Pos.CENTER_LEFT);
                        cellContent.setPadding(new Insets(8, 12, 8, 12));

                        FontIcon userIcon = new FontIcon("fas-user-graduate");
                        userIcon.setIconSize(18);
                        userIcon.getStyleClass().add("card-icon");

                        VBox infoBox = new VBox(2);
                        HBox.setHgrow(infoBox, Priority.ALWAYS);

                        Label nameLabel = new Label(item.toString());
                        nameLabel.getStyleClass().addAll("card-title", "student-name-label");

                        infoBox.getChildren().add(nameLabel);

                        // Add enrollment/exam count if functions provided
                        if (enrollmentCountFunc != null || examCountFunc != null) {
                            HBox badgeBox = new HBox(8);
                            badgeBox.setAlignment(Pos.CENTER_LEFT);

                            if (enrollmentCountFunc != null) {
                                int enrollCount = enrollmentCountFunc.apply(item);
                                Label enrollBadge = createBadge(
                                        MessageFormat.format(
                                                getBundleString("studentSearch.enrollments", "{0} courses"),
                                                enrollCount),
                                        "fas-book");
                                badgeBox.getChildren().add(enrollBadge);
                            }

                            if (examCountFunc != null) {
                                int examCount = examCountFunc.apply(item);
                                Label examBadge = createBadge(
                                        MessageFormat.format(getBundleString("studentSearch.exams", "{0} exams"),
                                                examCount),
                                        "fas-calendar-check");
                                badgeBox.getChildren().add(examBadge);
                            }

                            infoBox.getChildren().add(badgeBox);
                        }

                        cellContent.getChildren().addAll(userIcon, infoBox);
                        setGraphic(cellContent);
                        setText(null);
                    }
                }
            });

            // Debounced search filter
            debounce = new PauseTransition(Duration.millis(300));
            debounce.setOnFinished(event -> {
                String query = searchField.getText();
                filteredList.setPredicate(item -> {
                    if (query == null || query.isEmpty()) {
                        return true;
                    }
                    return filterLogic.test(item, query.toLowerCase());
                });
                updateResultCount();
            });

            searchField.textProperty().addListener((obs, oldValue, newValue) -> {
                debounce.playFromStart();
            });

            // Sorting logic
            sortCombo.setOnAction(e -> {
                String selected = sortCombo.getValue();
                applySorting(selected, enrollmentCountFunc);
            });

            // Result count
            resultCount = new Label(MessageFormat.format(
                    getBundleString("studentSearch.resultCount", "{0} students"), items.size()));
            resultCount.getStyleClass().addAll("label", "text-secondary", "result-count-label");

            // Recent searches section
            VBox recentSection = createRecentSearchesSection();

            // Assemble layout
            mainContainer.getChildren().addAll(headerBox, searchBox, sortBox, listView, resultCount);
            if (!recentSearches.isEmpty()) {
                mainContainer.getChildren().add(3, recentSection); // Insert after sortBox
            }
        }

        dialogPane.setContent(mainContainer);

        // Styled Buttons
        ButtonType selectButtonType = new ButtonType(
                getBundleString("studentSearch.select", "Select"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType(
                getBundleString("studentSearch.cancel", "Cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().addAll(selectButtonType, cancelButtonType);

        // Style the buttons
        Button selectButton = (Button) dialogPane.lookupButton(selectButtonType);
        if (selectButton != null) {
            selectButton.getStyleClass().add("primary-button");
            selectButton.setDisable(items.isEmpty());
        }

        Button cancelButton = (Button) dialogPane.lookupButton(cancelButtonType);
        if (cancelButton != null) {
            cancelButton.getStyleClass().add("secondary-button");
        }

        // Keyboard navigation
        setupKeyboardNavigation(selectButton, cancelButton);

        // Result Converter
        setResultConverter(dialogButton -> {
            if (dialogButton == selectButtonType) {
                T selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    // Add to recent searches
                    addToRecentSearches(searchField.getText());
                }
                return selected;
            }
            return null;
        });

        // Select first item by default
        if (!items.isEmpty()) {
            listView.getSelectionModel().selectFirst();
        }

        // Double-click to select
        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && listView.getSelectionModel().getSelectedItem() != null) {
                if (selectButton != null) {
                    selectButton.fire();
                }
            }
        });

        // Focus search field on open
        javafx.application.Platform.runLater(() -> searchField.requestFocus());
    }

    /**
     * Legacy constructor for backward compatibility
     */
    public SearchableDialog(String title, String headerText, List<T> items, BiPredicate<T, String> filterLogic) {
        this(title, headerText, items, filterLogic, null, null, null, false);
    }

    private String getBundleString(String key, String defaultValue) {
        if (bundle != null) {
            try {
                return bundle.getString(key);
            } catch (MissingResourceException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private VBox createEmptyState() {
        VBox emptyState = new VBox(15);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(40));
        emptyState.getStyleClass().add("search-empty-state");

        FontIcon emptyIcon = new FontIcon("fas-users-slash");
        emptyIcon.setIconSize(48);
        emptyIcon.getStyleClass().add("empty-state-icon");

        Label emptyLabel = new Label(getBundleString("studentSearch.emptyState",
                "No students found. Import attendance data to see enrolled students."));
        emptyLabel.getStyleClass().addAll("label", "text-secondary", "empty-state-label");
        emptyLabel.setWrapText(true);

        emptyState.getChildren().addAll(emptyIcon, emptyLabel);
        return emptyState;
    }

    private Label createBadge(String text, String iconLiteral) {
        Label badge = new Label(text);
        badge.getStyleClass().add("student-info-badge");
        return badge;
    }

    private void updateResultCount() {
        if (resultCount != null) {
            resultCount.setText(MessageFormat.format(
                    getBundleString("studentSearch.resultCount", "{0} students"), filteredList.size()));

            // Show "no results" message if filtered list is empty but original isn't
            if (filteredList.isEmpty() && !observableItems.isEmpty()) {
                resultCount.setText(getBundleString("studentSearch.noResults", "No students match your search."));
            }
        }
    }

    private void applySorting(String sortOption, Function<T, Integer> enrollmentCountFunc) {
        if (sortOption == null)
            return;

        // Re-sort the observable items
        List<T> sorted = new ArrayList<>(observableItems);

        if (sortOption.equals(getBundleString("studentSearch.sortByName", "Name (A-Z)"))) {
            sorted.sort(Comparator.comparing(Object::toString));
        } else if (sortOption.equals(getBundleString("studentSearch.sortByNameDesc", "Name (Z-A)"))) {
            sorted.sort(Comparator.comparing(Object::toString).reversed());
        } else if (sortOption.equals(getBundleString("studentSearch.sortById", "ID"))) {
            sorted.sort(Comparator.comparing(Object::toString)); // ID is part of toString
        } else if (sortOption.equals(getBundleString("studentSearch.sortByEnrollments", "Enrollments"))
                && enrollmentCountFunc != null) {
            sorted.sort(Comparator.comparing(enrollmentCountFunc).reversed());
        }

        observableItems.setAll(sorted);
    }

    private VBox createRecentSearchesSection() {
        VBox section = new VBox(5);
        section.setStyle("-fx-padding: 5 0 5 0;");

        Label title = new Label(getBundleString("studentSearch.recentSearches", "Recent Searches") + ":");
        title.getStyleClass().addAll("label", "text-secondary");
        title.setStyle("-fx-font-size: 10px;");

        HBox chips = new HBox(5);
        chips.setAlignment(Pos.CENTER_LEFT);

        for (String search : recentSearches) {
            if (search != null && !search.trim().isEmpty()) {
                Label chip = new Label(search);
                chip.getStyleClass().add("recent-search-chip");
                chip.setOnMouseClicked(e -> {
                    searchField.setText(search);
                });
                chips.getChildren().add(chip);
            }
        }

        if (chips.getChildren().isEmpty()) {
            return new VBox(); // Return empty if no valid recent searches
        }

        section.getChildren().addAll(title, chips);
        return section;
    }

    private void addToRecentSearches(String search) {
        if (search != null && !search.trim().isEmpty()) {
            recentSearches.remove(search); // Remove if already exists
            recentSearches.addFirst(search);
            while (recentSearches.size() > MAX_RECENT_SEARCHES) {
                recentSearches.removeLast();
            }
        }
    }

    private void setupKeyboardNavigation(Button selectButton, Button cancelButton) {
        // Enter key to select
        getDialogPane().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (listView.getSelectionModel().getSelectedItem() != null && selectButton != null) {
                    selectButton.fire();
                    event.consume();
                }
            } else if (event.getCode() == KeyCode.ESCAPE) {
                if (cancelButton != null) {
                    cancelButton.fire();
                    event.consume();
                }
            } else if (event.getCode() == KeyCode.DOWN) {
                if (!searchField.isFocused()) {
                    int idx = listView.getSelectionModel().getSelectedIndex();
                    if (idx < listView.getItems().size() - 1) {
                        listView.getSelectionModel().select(idx + 1);
                        listView.scrollTo(idx + 1);
                    }
                    event.consume();
                }
            } else if (event.getCode() == KeyCode.UP) {
                if (!searchField.isFocused()) {
                    int idx = listView.getSelectionModel().getSelectedIndex();
                    if (idx > 0) {
                        listView.getSelectionModel().select(idx - 1);
                        listView.scrollTo(idx - 1);
                    }
                    event.consume();
                }
            }
        });

        // Arrow keys from search field to navigate list
        searchField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DOWN) {
                listView.requestFocus();
                if (listView.getSelectionModel().isEmpty()) {
                    listView.getSelectionModel().selectFirst();
                }
                event.consume();
            } else if (event.getCode() == KeyCode.ENTER) {
                if (listView.getSelectionModel().getSelectedItem() != null && selectButton != null) {
                    selectButton.fire();
                    event.consume();
                }
            }
        });
    }
}
