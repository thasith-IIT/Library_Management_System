package com.example;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.IOException;
import java.util.List;

/**
 * Controller for SearchScreen.fxml
 * CM1601 - Library Management System
 */
public class SearchController implements com.example.ManagerAware {

    @FXML
    private void handleExport(ActionEvent event) {
    }

    @FXML private TextField            tfQuery;
    @FXML private Label                lblCount;
    @FXML private TableView<Book>      table;
    @FXML private TableColumn<Book, String> colID;
    @FXML private TableColumn<Book, String> colTitle;
    @FXML private TableColumn<Book, String> colAuth;
    @FXML private TableColumn<Book, String> colISBN;
    @FXML private TableColumn<Book, String> colAvail;
    @FXML private TableColumn<Book, String> colPrice;

    private LibraryManager manager;

    @Override
    public void setManager(LibraryManager manager) {
        this.manager = manager;
    }

    @FXML
    public void initialize() {
        colID.setCellValueFactory(p    -> new SimpleStringProperty(p.getValue().getBookID()));
        colTitle.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getTitle()));
        colAuth.setCellValueFactory(p  -> new SimpleStringProperty(p.getValue().getAuthor()));
        colISBN.setCellValueFactory(p  -> new SimpleStringProperty(p.getValue().getIsbn13()));
        colAvail.setCellValueFactory(p -> new SimpleStringProperty(
                p.getValue().getAvailable() + "/" + p.getValue().getCopies()));
        colPrice.setCellValueFactory(p -> new SimpleStringProperty(
                String.format("£%.2f", p.getValue().getPrice())));
    }

    @FXML
    private void handleSearch() {
        if (manager == null || manager.getBooks().isEmpty()) {
            showAlert("No Data", "Please import CSV files first.");
            return;
        }
        String query = tfQuery.getText().trim();
        if (query.isEmpty()) {
            showAlert("Input Required", "Please enter a search term.");
            return;
        }
        List<Book> results = manager.searchByTitle(query);
        table.setItems(FXCollections.observableArrayList(results));
        lblCount.setText("Found: " + results.size() + " result(s).");
    }

    @FXML
    private void handleExport() {
        if (table.getItems().isEmpty()) {
            showAlert("Nothing to Export", "Run a search first.");
            return;
        }
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Search Results");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        fc.setInitialFileName("search_results.txt");
        java.io.File file = fc.showSaveDialog(MainGUI.getPrimaryStage());
        if (file != null) {
            try {
                manager.exportSearchResults(table.getItems(), file.getAbsolutePath());
                showAlert("Exported", "Results saved to:\n" + file.getAbsolutePath());
            } catch (IOException ex) {
                showAlert("Error", "Could not save file: " + ex.getMessage());
            }
        }
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
