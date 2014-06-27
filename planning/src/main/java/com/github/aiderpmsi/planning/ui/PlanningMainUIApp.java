package com.github.aiderpmsi.planning.ui;

import java.io.IOException;

import com.github.aiderpmsi.planning.physician.Physician;
import com.github.aiderpmsi.planning.physician.PhysicianBuilder;
import com.github.aiderpmsi.planning.ui.controller.PhysicianController;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class PlanningMainUIApp extends Application {

	private Stage primaryStage;
    
	private BorderPane rootLayout;
	
	private ObservableList<Physician> physicians = FXCollections.observableArrayList();
	
	@Override
	public void start(Stage primaryStage) {
		physicians.add(new PhysicianBuilder().setName("Med 1").setTimePart(100).toPhysician());
		this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Planning");

        try {
            // Load the root layout from the fxml file
            FXMLLoader loader = new FXMLLoader(PlanningMainUIApp.class.getResource("view/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            // Exception gets thrown if the fxml file could not be loaded
            e.printStackTrace();
        }

        showPhysicianOverview();

	}

    public void showPhysicianOverview() {
        try {
            // Load the fxml file and set into the center of the main layout
            FXMLLoader loader = new FXMLLoader(PlanningMainUIApp.class.getResource("view/PhysicianOverview.fxml"));
            AnchorPane overviewPage = (AnchorPane) loader.load();
            rootLayout.setCenter(overviewPage);
            
            PhysicianController controller = loader.getController();
            controller.setMainApp(this);

        } catch (IOException e) {
            // Exception gets thrown if the fxml file could not be loaded
            e.printStackTrace();
        }
    }
    
	public ObservableList<Physician> getPhysicians() {
		return physicians;
	}

	public static void main(String[] args) {
		launch(args);
	}
}
