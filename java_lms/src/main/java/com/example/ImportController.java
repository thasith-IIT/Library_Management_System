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

    /**
     * This method runs automatically when the FXML is loaded.
     * It sets the absolute path for your CSV files so you don't have to browse.
     */
    @FXML
    public void initialize() {
        // We look for the 'data' folder in your project root
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
    private void handleBrowseBooks() {
        browse(tfBooks);
    }

    @FXML
    private void handleBrowseStudents() {
        browse(tfStudents);
    }

    @FXML
    private void handleBrowseTrans() {
        browse(tfTrans);
    }

    private void browse(TextField target) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File f = fc.showOpenDialog(MainGUI.getPrimaryStage());
        if (f != null) target.setText(f.getAbsolutePath());
    }

    /**
     * Updated handleImport to use the ActionEvent from the FXML button.
     */
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
            sb.append("\nData loaded with warnings. Please use the Edit button to correct records.");
            taErrors.setText(sb.toString());
        }
    }

    @FXML
    public void handleEditRecord() {
        if (manager.getBooks().isEmpty()) {
            showAlert("No Data", "Please import files first.");
            return;
        }

        Stage editStage = new Stage();
        editStage.setTitle("Edit Book Records");

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
            col.setOnEditCommit(e -> e.getRowValue().get(idx).set(e.getNewValue()));
            col.setPrefWidth(100);
            table.getColumns().add(col);
        }

        Button btnSave = new Button("Save Changes");
        btnSave.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 8 18;");
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
        vbox.setPadding(new javafx.geometry.Insets(15));
        editStage.setScene(new Scene(vbox, 750, 420));
        editStage.show();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/MainMenu.fxml"));
            Scene scene = new Scene(loader.load(), 500, 520);
            MainMenuController ctrl = loader.getController();
            ctrl.setManager(manager);
            MainGUI.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}