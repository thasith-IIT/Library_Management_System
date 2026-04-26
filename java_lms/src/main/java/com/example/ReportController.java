package com.example;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.List;

public class ReportController implements com.example.ManagerAware {

    @FXML private TextField              tfDate;
    @FXML private Label                  lblCount;
    @FXML private TableView<Transaction> table;
    @FXML private TableColumn<Transaction, String> colDate;
    @FXML private TableColumn<Transaction, String> colBook;
    @FXML private TableColumn<Transaction, String> colStudent;

    private LibraryManager manager;

    @Override
    public void setManager(LibraryManager manager) {
        this.manager = manager;
    }

    @FXML
    public void initialize() {
        colDate.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getDate()));
        colBook.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getBookID()));
        colStudent.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getStudentID()));
    }

    @FXML
    private void handleSearch() {
        String date = tfDate.getText().trim();
        if (!Transaction.validateDate(date)) {
            showAlert("Invalid Date", "Please enter a date in DD/MM/YYYY format.");
            return;
        }
        if (manager == null || manager.getBooks().isEmpty()) {
            showAlert("No Data", "Please import CSV files first.");
            return;
        }
        List<Transaction> results = manager.getBooksIssuedByDate(date);
        table.setItems(FXCollections.observableArrayList(results));
        lblCount.setText("Total books issued on " + date + ": " + results.size());
    }

    @FXML
    private void handleBack() {
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
