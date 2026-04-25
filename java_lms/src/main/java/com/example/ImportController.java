package com.example;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ImportController implements com.example.ManagerAware {

    @FXML private TextField tfBooks;
    @FXML private TextField tfStudents;
    @FXML private TextField tfTrans;
    @FXML private TextArea  taErrors;

    private LibraryManager manager;

    @FXML
    public void initialize() {
        String dataPath = new File("data").getAbsolutePath();
        tfBooks.setText(dataPath + File.separator + "book.csv");
        tfStudents.setText(dataPath + File.separator + "student.csv");
        tfTrans.setText(dataPath + File.separator + "transactions.csv");
    }

    @Override
    public void setManager(LibraryManager manager) {
        this.manager = manager;
    }

    @FXML
    private void handleBrowseBooks() { browse(tfBooks); }

    @FXML
    private void handleBrowseStudents() { browse(tfStudents); }

    @FXML
    private void handleBrowseTrans() { browse(tfTrans); }

    private void browse(TextField target) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File f = fc.showOpenDialog(MainGUI.getPrimaryStage());
        if (f != null) target.setText(f.getAbsolutePath());
    }

    @FXML
    private void handleImport(ActionEvent event) {
        if (manager == null) return;
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
            taErrors.setText(sb.toString());
        }
    }

    @FXML
    public void handleEditRecord() {
        if (manager == null || manager.getBooks().isEmpty()) {
            showAlert("No Data", "Please import files first.");
            return;
        }

        ObservableList<ObservableList<StringProperty>> data = FXCollections.observableArrayList();
        for (Book b : manager.getBooks()) {
            data.add(FXCollections.observableArrayList(
                    new SimpleStringProperty(b.getBookID()),
                    new SimpleStringProperty(b.getTitle()),
                    new SimpleStringProperty(b.getAuthor()),
                    new SimpleStringProperty(b.getIsbn13()),
                    new SimpleStringProperty(String.valueOf(b.getCopies())),
                    new SimpleStringProperty(String.valueOf(b.getAvailable())),
                    new SimpleStringProperty(String.format("%.2f", b.getPrice()))
            ));
        }

        TableView<ObservableList<StringProperty>> table = buildEditTable(
                new String[]{"Book ID", "Title", "Author", "ISBN-13", "Copies", "Available", "Price"},
                new double[]{ 80, 130, 120, 130, 70, 80, 70}, data);

        Button btnSave = styledButton("Save Changes", "#27ae60");
        btnSave.setOnAction(e -> {
            // FORCE COMMIT: Saves the cell currently being typed in
            table.edit(-1, null);

            StringBuilder validationErrors = new StringBuilder();
            for (int i = 0; i < data.size(); i++) {
                ObservableList<StringProperty> row = data.get(i);
                int r = i + 1;
                String isbn = row.get(3).get().trim();
                if (!Book.validateISBN(isbn)) validationErrors.append("Row ").append(r).append(": Invalid ISBN\n");

                try {
                    int c = Integer.parseInt(row.get(4).get().trim());
                    int a = Integer.parseInt(row.get(5).get().trim());
                    if (c < 0 || c > 2) validationErrors.append("Row ").append(r).append(": Copies 0-2 only\n");
                    if (a < 0 || a > c) validationErrors.append("Row ").append(r).append(": Available invalid\n");
                } catch (Exception ex) { validationErrors.append("Row ").append(r).append(": Non-numeric counts\n"); }
            }

            if (validationErrors.length() > 0) {
                showAlert("Validation Errors", validationErrors.toString());
                return;
            }

            for (int i = 0; i < data.size(); i++) {
                ObservableList<StringProperty> row = data.get(i);
                manager.updateBook(i, new Book(row.get(0).get(), row.get(1).get(), row.get(2).get(),
                        row.get(3).get(), Integer.parseInt(row.get(4).get()),
                        Integer.parseInt(row.get(5).get()), Double.parseDouble(row.get(6).get())));
            }
            showAlert("Saved", "Records updated.");
            ((Stage) btnSave.getScene().getWindow()).close();
        });

        Stage stage = buildEditStage("Edit Books", table, new Label("Edit inline and save"), btnSave, styledButton("Cancel", "#95a5a6"), 700, 480);
        stage.show();
    }

    @FXML
    public void handleEditStudentRecord() {
        if (manager == null || manager.getStudents().isEmpty()) {
            showAlert("No Data", "Import first.");
            return;
        }

        ObservableList<ObservableList<StringProperty>> data = FXCollections.observableArrayList();
        for (Student s : manager.getStudents()) {
            data.add(FXCollections.observableArrayList(new SimpleStringProperty(s.getStudentID()), new SimpleStringProperty(s.getFirstName())));
        }

        TableView<ObservableList<StringProperty>> table = buildEditTable(new String[]{"ID", "Name"}, new double[]{200, 280}, data);
        Button btnSave = styledButton("Save Changes", "#27ae60");

        btnSave.setOnAction(e -> {
            table.edit(-1, null); // Commit active edit
            StringBuilder errs = new StringBuilder();
            for (int i = 0; i < data.size(); i++) {
                if (!Student.validateStudentID(data.get(i).get(0).get().trim()))
                    errs.append("Row ").append(i+1).append(": ID must be 8 digits\n");
            }
            if (errs.length() > 0) { showAlert("Errors", errs.toString()); return; }

            for (int i = 0; i < data.size(); i++) {
                manager.updateStudent(i, new Student(data.get(i).get(0).get(), data.get(i).get(1).get()));
            }
            ((Stage) btnSave.getScene().getWindow()).close();
        });

        buildEditStage("Edit Students", table, new Label(""), btnSave, styledButton("Cancel", "#95a5a6"), 520, 400).show();
    }

    @FXML
    public void handleEditTransactionRecord() {
        if (manager == null || manager.getTransactions().isEmpty()) {
            showAlert("No Data", "Import first.");
            return;
        }

        ObservableList<ObservableList<StringProperty>> data = FXCollections.observableArrayList();
        for (Transaction t : manager.getTransactions()) {
            data.add(FXCollections.observableArrayList(new SimpleStringProperty(t.getDate()), new SimpleStringProperty(t.getBookID()), new SimpleStringProperty(t.getStudentID()), new SimpleStringProperty(String.valueOf(t.getType()))));
        }

        TableView<ObservableList<StringProperty>> table = buildEditTable(new String[]{"Date", "Book", "Student", "Type"}, new double[]{160, 100, 130, 160}, data);
        Button btnSave = styledButton("Save Changes", "#27ae60");

        btnSave.setOnAction(e -> {
            table.edit(-1, null); // Commit active edit
            StringBuilder errs = new StringBuilder();
            for (int i = 0; i < data.size(); i++) {
                if (!Transaction.validateDate(data.get(i).get(0).get().trim()))
                    errs.append("Row ").append(i+1).append(": Invalid Date\n");
            }
            if (errs.length() > 0) { showAlert("Errors", errs.toString()); return; }

            for (int i = 0; i < data.size(); i++) {
                ObservableList<StringProperty> r = data.get(i);
                manager.updateTransaction(i, new Transaction(r.get(0).get(), r.get(1).get(), r.get(2).get(), Integer.parseInt(r.get(3).get())));
            }
            ((Stage) btnSave.getScene().getWindow()).close();
        });

        buildEditStage("Edit Transactions", table, new Label(""), btnSave, styledButton("Cancel", "#95a5a6"), 600, 460).show();
    }

    private TableView<ObservableList<StringProperty>> buildEditTable(String[] names, double[] widths, ObservableList<ObservableList<StringProperty>> data) {
        TableView<ObservableList<StringProperty>> table = new TableView<>(data);
        table.setEditable(true);
        for (int i = 0; i < names.length; i++) {
            final int idx = i;
            TableColumn<ObservableList<StringProperty>, String> col = new TableColumn<>(names[i]);
            col.setCellValueFactory(p -> p.getValue().get(idx));
            col.setCellFactory(TextFieldTableCell.forTableColumn());
            col.setOnEditCommit(ev -> ev.getRowValue().get(idx).set(ev.getNewValue()));
            col.setPrefWidth(widths[i]);
            table.getColumns().add(col);
        }
        return table;
    }

    private Stage buildEditStage(String t, TableView<?> tbl, Label h, Button s, Button c, int w, int hgt) {
        Stage stage = new Stage();
        stage.setTitle(t);
        VBox vbox = new VBox(10, tbl, h, new HBox(10, s, c));
        vbox.setPadding(new javafx.geometry.Insets(15));
        stage.setScene(new Scene(vbox, w, hgt));
        c.setOnAction(e -> stage.close());
        return stage;
    }

    private Button styledButton(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 8 18;");
        return b;
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader l = new FXMLLoader(getClass().getResource("fxml/MainMenu.fxml"));
            Scene s = new Scene(l.load(), 500, 520);
            ((MainMenuController)l.getController()).setManager(manager);
            MainGUI.getPrimaryStage().setScene(s);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showAlert(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait();
    }
}