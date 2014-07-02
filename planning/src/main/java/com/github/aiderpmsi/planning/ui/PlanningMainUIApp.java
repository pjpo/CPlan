package com.github.aiderpmsi.planning.ui;

import java.io.IOException;

import com.github.aiderpmsi.planning.physician.Physician;
import com.github.aiderpmsi.planning.physician.PhysicianBuilder;
import com.github.aiderpmsi.planning.ui.controller.PhysicianController;
import com.github.aiderpmsi.planning.ui.controller.PhysicianEditDialogController;
import com.github.aiderpmsi.planning.ui.controller.RootLayoutController;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PlanningMainUIApp extends Application {

	private Stage primaryStage;
    
	private BorderPane rootLayout;
	
	private ObservableList<Physician> physicians = FXCollections.observableArrayList();
	
	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Planning");

        try {
            // Load the root layout from the fxml file
            FXMLLoader loader = new FXMLLoader(PlanningMainUIApp.class.getResource("view/RootLayout.fxml"));
            RootLayoutController root = new RootLayoutController();
            root.setMainApp(this);
            loader.setController(root);
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

    public boolean showPhysicianEditDialog(Physician physician) {
  	  try {
  	    // Load the fxml file and create a new stage for the popup
  	    FXMLLoader loader = new FXMLLoader(PlanningMainUIApp.class.getResource("view/PhysicianEditDialog.fxml"));
  	    AnchorPane page = (AnchorPane) loader.load();
  	    Stage dialogStage = new Stage();
  	    dialogStage.setTitle("Modifier médecin");
  	    dialogStage.initModality(Modality.WINDOW_MODAL);
  	    dialogStage.initOwner(primaryStage);
  	    Scene scene = new Scene(page);
  	    dialogStage.setScene(scene);

  	    // Set the person into the controller
  	    PhysicianEditDialogController controller = loader.getController();
  	    controller.setDialogStage(dialogStage);
  	    controller.setPhysician(physician);

  	    // Show the dialog and wait until the user closes it
  	    dialogStage.showAndWait();

  	    return controller.isOkClicked();

  	  } catch (IOException e) {
  	    // Exception gets thrown if the fxml file could not be loaded
  	    e.printStackTrace();
  	    return false;
  	  }
  	}

    public Stage getPrimaryStage() {
		return primaryStage;
	}

	public static void main(String[] args) {
		launch(args);
	}
}
