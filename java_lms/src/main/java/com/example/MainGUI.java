package com.example;

import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.fxml.FXMLLoader;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.*;

import java.io.IOException;
import java.util.List;

/**
 * Main JavaFX GUI for the Library Management System.
 * CM1601 - Library Management System
 *
 * Screens:
 *   1. Import CSV Files (with validation & inline editing)
 *   2. Books Issued Report by Date
 *   3. Average Cost of Books
 *   4. Search Books by Title (with wildcard + export)
 */
public class MainGUI extends Application {

    private LibraryManager manager = new LibraryManager();
    private Stage primaryStage;

    private static final double WINDOW_WIDTH = 750;
    private static final double WINDOW_HEIGHT = 550;

    // Static reference so FXML controllers can access the stage
    private static Stage staticPrimaryStage;

    public static Stage getPrimaryStage() {
        return staticPrimaryStage;
    }

    // ──────────────────────────────────────────────
    // APPLICATION START  (updated to load from FXML)
    // ──────────────────────────────────────────────
    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("Library Management System - CM1601");
        stage.setResizable(false); // Prevents the user from stretching the window
        displayMainMenu();
        stage.show();
    }

    // ──────────────────────────────────────────────
    // MAIN MENU
    // ──────────────────────────────────────────────
    private void displayMainMenu() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1e2a3a;");

        Label title = new Label("Library Management System");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        title.setStyle("-fx-text-fill: #e8f4fd;");

        Label subtitle = new Label("CM1601 - Java FX Module");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setStyle("-fx-text-fill: #90b8d8;");

        Button btnImport  = menuButton("1.  Import CSV Files");
        Button btnReport  = menuButton("2.  Books Issued Report");
        Button btnAvgCost = menuButton("3.  Average Cost of Books");
        Button btnSearch  = menuButton("4.  Search Books by Title");
        Button btnExit    = menuButton("     Exit");
        btnExit.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; "
                + "-fx-font-size: 14px; -fx-min-width: 280px; -fx-padding: 12 20;");

        btnImport.setOnAction(e  -> displayImportScreen());
        btnReport.setOnAction(e  -> displayReportScreen());
        btnAvgCost.setOnAction(e -> displayAverageCostScreen());
        btnSearch.setOnAction(e  -> displaySearchScreen());
        btnExit.setOnAction(e    -> primaryStage.close());

        root.getChildren().addAll(title, subtitle, new Separator(),
                btnImport, btnReport, btnAvgCost, btnSearch,
                new Separator(), btnExit);
        primaryStage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
    }

    private Button menuButton(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: #ecf0f1; "
                + "-fx-font-size: 14px; -fx-min-width: 280px; -fx-padding: 12 20; "
                + "-fx-cursor: hand;");
        b.setOnMouseEntered(e -> b.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; "
                + "-fx-font-size: 14px; -fx-min-width: 280px; -fx-padding: 12 20; -fx-cursor: hand;"));
        b.setOnMouseExited(e  -> b.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: #ecf0f1; "
                + "-fx-font-size: 14px; -fx-min-width: 280px; -fx-padding: 12 20; -fx-cursor: hand;"));
        return b;
    }

    // ──────────────────────────────────────────────
    // SCREEN 1: IMPORT CSV FILES
    // ──────────────────────────────────────────────
    public void displayImportScreen() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: #f0f4f8;");

        Label heading = screenHeading("Import CSV Files");

        // File path fields
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(10));

        TextField tfBooks    = pathField("book.csv");
        TextField tfStudents = pathField("student.csv");
        TextField tfTrans    = pathField("transactions.csv");

        grid.add(new Label("Book File:"),        0, 0); grid.add(tfBooks,    1, 0);
        grid.add(new Label("Student File:"),     0, 1); grid.add(tfStudents, 1, 1);
        grid.add(new Label("Transaction File:"), 0, 2); grid.add(tfTrans,    1, 2);

        Button btnBrowseB = browseButton(primaryStage, tfBooks);
        Button btnBrowseS = browseButton(primaryStage, tfStudents);
        Button btnBrowseT = browseButton(primaryStage, tfTrans);
        grid.add(btnBrowseB, 2, 0); grid.add(btnBrowseS, 2, 1); grid.add(btnBrowseT, 2, 2);

        // Error / status area
        TextArea taErrors = new TextArea();
        taErrors.setEditable(false);
        taErrors.setPrefHeight(180);
        taErrors.setStyle("-fx-font-family: monospace; -fx-font-size: 12;");
        taErrors.setPromptText("Validation messages will appear here...");

        // Import button
        Button btnImport = actionButton("Import Files");
        btnImport.setOnAction(e -> {
            List<String> errors = manager.importCSVFiles(
                    tfBooks.getText().trim(),
                    tfStudents.getText().trim(),
                    tfTrans.getText().trim());
            if (errors.isEmpty()) {
                taErrors.setText("✓ All files imported successfully!\n"
                        + "  Books loaded:        " + manager.getBooks().size() + "\n"
                        + "  Students loaded:     " + manager.getStudents().size() + "\n"
                        + "  Transactions loaded: " + manager.getTransactions().size());
            } else {
                StringBuilder sb = new StringBuilder("⚠ Import completed with errors:\n\n");
                errors.forEach(err -> sb.append("  • ").append(err).append("\n"));
                sb.append("\nData loaded with warnings. Please use the Edit button to correct records.");
                taErrors.setText(sb.toString());
            }
        });

        // Edit Records button (opens book editing table)
        Button btnEdit = actionButton("Edit Book Records");
        btnEdit.setOnAction(e -> handleEditRecord());

        Button btnBack = backButton();
        btnBack.setOnAction(e -> displayMainMenu());

        HBox buttons = new HBox(10, btnImport, btnEdit, btnBack);
        buttons.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().addAll(heading, grid, buttons,
                new Label("Validation Results:"), taErrors);

        primaryStage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
    }

    /**
     * Opens an editable table for correcting imported book records.
     */
    public void handleEditRecord() {
        if (manager.getBooks().isEmpty()) {
            showAlert("No Data", "Please import files first.");
            return;
        }

        Stage editStage = new Stage();
        editStage.setTitle("Edit Book Records");

        // Build observable list with String properties for editing
        ObservableList<ObservableList<StringProperty>> data = FXCollections.observableArrayList();
        for (Book b : manager.getBooks()) {
            ObservableList<StringProperty> row = FXCollections.observableArrayList(
                    new SimpleStringProperty(b.getBookID()),
                    new SimpleStringProperty(b.getTitle()),
                    new SimpleStringProperty(b.getAuthor()),
                    new SimpleStringProperty(b.getIsbn13()),
                    new SimpleStringProperty(String.valueOf(b.getCopies())),
                    new SimpleStringProperty(String.valueOf(b.getAvailable())),
                    new SimpleStringProperty(String.format("%.2f", b.getPrice()))
            );
            data.add(row);
        }

        TableView<ObservableList<StringProperty>> table = new TableView<>(data);
        table.setEditable(true);
        String[] cols = {"BookID", "Title", "Author", "ISBN-13", "Copies", "Available", "Price"};
        for (int i = 0; i < cols.length; i++) {
            final int idx = i;
            TableColumn<ObservableList<StringProperty>, String> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(param -> param.getValue().get(idx));
            col.setCellFactory(TextFieldTableCell.forTableColumn());
            col.setOnEditCommit(event -> event.getRowValue().get(idx).set(event.getNewValue()));
            col.setPrefWidth(100);
            table.getColumns().add(col);
        }

        Button btnSave = actionButton("Save Changes");
        btnSave.setOnAction(e -> {
            for (int i = 0; i < data.size(); i++) {
                ObservableList<StringProperty> row = data.get(i);
                try {
                    Book updated = new Book(
                            row.get(0).get(), row.get(1).get(), row.get(2).get(),
                            row.get(3).get(),
                            Integer.parseInt(row.get(4).get()),
                            Integer.parseInt(row.get(5).get()),
                            Double.parseDouble(row.get(6).get())
                    );
                    manager.updateBook(i, updated);
                } catch (NumberFormatException ex) {
                    showAlert("Error", "Invalid numeric value in row " + (i + 1));
                }
            }
            showAlert("Saved", "Book records updated successfully.");
            editStage.close();
        });

        VBox vbox = new VBox(10, table, btnSave);
        vbox.setPadding(new Insets(15));
        editStage.setScene(new Scene(vbox, WINDOW_WIDTH, WINDOW_HEIGHT));
        editStage.show();
    }

    // ──────────────────────────────────────────────
    // SCREEN 2: BOOKS ISSUED REPORT
    // ──────────────────────────────────────────────
    public void displayReportScreen() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: #f0f4f8;");

        Label heading = screenHeading("Books Issued Report");

        HBox dateRow = new HBox(10);
        dateRow.setAlignment(Pos.CENTER_LEFT);
        TextField tfDate = new TextField();
        tfDate.setPromptText("DD/MM/YYYY");
        tfDate.setPrefWidth(150);
        Button btnSearch = actionButton("Search");
        dateRow.getChildren().addAll(new Label("Enter Date:"), tfDate, btnSearch);

        // Results table
        TableView<Transaction> table = new TableView<>();
        TableColumn<Transaction, String> colDate   = new TableColumn<>("Date");
        TableColumn<Transaction, String> colBook   = new TableColumn<>("Book ID");
        TableColumn<Transaction, String> colStudent= new TableColumn<>("Student ID");
        colDate.setCellValueFactory(p    -> new SimpleStringProperty(p.getValue().getDate()));
        colBook.setCellValueFactory(p    -> new SimpleStringProperty(p.getValue().getBookID()));
        colStudent.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getStudentID()));
        colDate.setPrefWidth(130); colBook.setPrefWidth(100); colStudent.setPrefWidth(150);
        table.getColumns().addAll(colDate, colBook, colStudent);
        table.setPrefHeight(280);

        Label lblCount = new Label();

        btnSearch.setOnAction(e -> {
            String date = tfDate.getText().trim();
            if (!Transaction.validateDate(date)) {
                showAlert("Invalid Date", "Please enter a date in DD/MM/YYYY format.");
                return;
            }
            if (manager.getBooks().isEmpty()) {
                showAlert("No Data", "Please import CSV files first.");
                return;
            }
            List<Transaction> results = manager.getBooksIssuedByDate(date);
            table.setItems(FXCollections.observableArrayList(results));
            lblCount.setText("Total books issued on " + date + ": " + results.size());
        });

        Button btnBack = backButton();
        btnBack.setOnAction(e -> displayMainMenu());

        root.getChildren().addAll(heading, dateRow, lblCount, table, btnBack);
        primaryStage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
    }

    // ──────────────────────────────────────────────
    // SCREEN 3: AVERAGE COST
    // ──────────────────────────────────────────────
    private void displayAverageCostScreen() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f0f4f8;");

        Label heading = screenHeading("Average Cost of Books");

        Button btnCalc = actionButton("Calculate Average Cost");
        Label lblResult = new Label();
        lblResult.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        lblResult.setStyle("-fx-text-fill: #2c3e50;");

        Label lblBookCount = new Label();

        btnCalc.setOnAction(e -> {
            if (manager.getBooks().isEmpty()) {
                showAlert("No Data", "Please import CSV files first.");
                return;
            }
            double avg = manager.calculateAverageCost();
            lblResult.setText(String.format("Average Cost: £%.2f", avg));
            lblBookCount.setText("Based on " + manager.getBooks().size() + " book(s) in the library.");
        });

        Button btnBack = backButton();
        btnBack.setOnAction(e -> displayMainMenu());

        root.getChildren().addAll(heading, btnCalc, lblResult, lblBookCount, btnBack);
        primaryStage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
    }

    // ──────────────────────────────────────────────
    // SCREEN 4: SEARCH BOOKS
    // ──────────────────────────────────────────────
    private void displaySearchScreen() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: #f0f4f8;");

        Label heading = screenHeading("Search Books by Title");
        Label hint = new Label("Tip: Use wildcard '*' at end, e.g. 'Java*' to match all Java titles.");
        hint.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");

        HBox searchRow = new HBox(10);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        TextField tfQuery = new TextField();
        tfQuery.setPromptText("Enter title or title prefix*");
        tfQuery.setPrefWidth(250);
        Button btnSearch = actionButton("Search");
        searchRow.getChildren().addAll(new Label("Title:"), tfQuery, btnSearch);

        // Results table
        TableView<Book> table = new TableView<>();
        TableColumn<Book, String> colID    = new TableColumn<>("Book ID");
        TableColumn<Book, String> colTitle = new TableColumn<>("Title");
        TableColumn<Book, String> colAuth  = new TableColumn<>("Author");
        TableColumn<Book, String> colISBN  = new TableColumn<>("ISBN-13");
        TableColumn<Book, String> colAvail = new TableColumn<>("Available");
        TableColumn<Book, String> colPrice = new TableColumn<>("Price");

        colID.setCellValueFactory(p    -> new SimpleStringProperty(p.getValue().getBookID()));
        colTitle.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getTitle()));
        colAuth.setCellValueFactory(p  -> new SimpleStringProperty(p.getValue().getAuthor()));
        colISBN.setCellValueFactory(p  -> new SimpleStringProperty(p.getValue().getIsbn13()));
        colAvail.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getAvailable() + "/" + p.getValue().getCopies()));
        colPrice.setCellValueFactory(p -> new SimpleStringProperty(String.format("£%.2f", p.getValue().getPrice())));

        colID.setPrefWidth(80); colTitle.setPrefWidth(160); colAuth.setPrefWidth(130);
        colISBN.setPrefWidth(130); colAvail.setPrefWidth(80); colPrice.setPrefWidth(70);
        table.getColumns().addAll(colID, colTitle, colAuth, colISBN, colAvail, colPrice);
        table.setPrefHeight(260);

        Label lblCount = new Label();

        btnSearch.setOnAction(e -> {
            if (manager.getBooks().isEmpty()) {
                showAlert("No Data", "Please import CSV files first.");
                return;
            }
            String query = tfQuery.getText().trim();
            if (query.isEmpty()) { showAlert("Input Required", "Please enter a search term."); return; }
            List<Book> results = manager.searchByTitle(query);
            table.setItems(FXCollections.observableArrayList(results));
            lblCount.setText("Found: " + results.size() + " result(s).");
        });

        // Export button
        Button btnExport = actionButton("Export Results");
        btnExport.setOnAction(e -> {
            if (table.getItems().isEmpty()) {
                showAlert("Nothing to Export", "Run a search first."); return;
            }
            FileChooser fc = new FileChooser();
            fc.setTitle("Save Search Results");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
            fc.setInitialFileName("search_results.txt");
            java.io.File file = fc.showSaveDialog(primaryStage);
            if (file != null) {
                try {
                    manager.exportSearchResults(table.getItems(), file.getAbsolutePath());
                    showAlert("Exported", "Results saved to:\n" + file.getAbsolutePath());
                } catch (IOException ex) {
                    showAlert("Error", "Could not save file: " + ex.getMessage());
                }
            }
        });

        Button btnBack = backButton();
        btnBack.setOnAction(e -> displayMainMenu());

        HBox buttons = new HBox(10, btnExport, btnBack);
        root.getChildren().addAll(heading, hint, searchRow, lblCount, table, buttons);
        primaryStage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
    }

    // ──────────────────────────────────────────────
    // UI HELPERS
    // ──────────────────────────────────────────────
    private Label screenHeading(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        l.setStyle("-fx-text-fill: #1a252f;");
        return l;
    }

    private Button actionButton(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; "
                + "-fx-font-size: 13px; -fx-padding: 8 18; -fx-cursor: hand;");
        return b;
    }

    private Button backButton() {
        Button b = new Button("← Back to Menu");
        b.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; "
                + "-fx-font-size: 13px; -fx-padding: 8 18; -fx-cursor: hand;");
        return b;
    }

    private TextField pathField(String defaultVal) {
        TextField tf = new TextField(defaultVal);
        tf.setPrefWidth(320);
        return tf;
    }

    private Button browseButton(Stage stage, TextField target) {
        Button b = new Button("Browse");
        b.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            java.io.File f = fc.showOpenDialog(stage);
            if (f != null) target.setText(f.getAbsolutePath());
        });
        return b;
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // ──────────────────────────────────────────────
    // ENTRY POINT
    // ──────────────────────────────────────────────
    public static void main(String[] args) {
        launch(args);
    }
}
