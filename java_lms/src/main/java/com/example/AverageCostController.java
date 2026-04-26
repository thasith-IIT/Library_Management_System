package com.example;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

import java.io.IOException;

public class AverageCostController implements com.example.ManagerAware {

    @FXML private Label lblResult;
    @FXML private Label lblBookCount;

    private LibraryManager manager;

    @Override
    public void setManager(LibraryManager manager) {
        this.manager = manager;
    }

    @FXML
    private void handleCalculate() {
        if (manager == null || manager.getBooks().isEmpty()) {
            showAlert("No Data", "Please import CSV files first.");
            return;
        }
        double avg = manager.calculateAverageCost();
        lblResult.setText(String.format("Average Cost: £%.2f", avg));
        lblBookCount.setText("Based on " + manager.getBooks().size() + " book(s) in the library.");
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
