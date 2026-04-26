package com.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainMenuController {

    @FXML private void handleImport(ActionEvent event) {}
    @FXML private void handleSearch(ActionEvent event) {}
    @FXML private void handleExit(ActionEvent event) {}

    // Shared LibraryManager instance passed between controllers
    private LibraryManager manager;

    public void setManager(LibraryManager manager) {
        this.manager = manager;
    }

    // ── Navigation helpers ────────────────────────────────────────────

    private Stage getStage() {
        // Retrieve the stage from any injected node — we use the Application reference
        return MainGUI.getPrimaryStage();
    }

    private void loadScreen(String fxmlFile, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/" + fxmlFile));
            Scene scene = new Scene(loader.load(), width, height);

            // Pass the shared manager to the new controller
            Object controller = loader.getController();
            if (controller instanceof com.example.ManagerAware) {
                ((com.example.ManagerAware) controller).setManager(manager);
            }

            getStage().setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Button handlers ───────────────────────────────────────────────

    @FXML
    private void handleImport() {
        loadScreen("ImportScreen.fxml", 700, 520);
    }

    @FXML
    private void handleReport() {
        loadScreen("ReportScreen.fxml", 580, 500);
    }

    @FXML
    private void handleAvgCost() {
        loadScreen("AverageCostScreen.fxml", 480, 380);
    }

    @FXML
    private void handleSearch() {
        loadScreen("SearchScreen.fxml", 700, 520);
    }

    @FXML
    private void handleExit() {
        getStage().close();
    }
}
