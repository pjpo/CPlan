package com.github.pjpo.planning.ui;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.github.pjpo.planning.model.Worker;
import com.github.pjpo.planning.model.PositionCode;
import com.github.pjpo.planning.ui.controller.GenerationOverviewController;
import com.github.pjpo.planning.ui.controller.PhysicianEditDialogController;
import com.github.pjpo.planning.ui.controller.PhysicianOverviewController;
import com.github.pjpo.planning.ui.controller.PositionsOverviewController;
import com.github.pjpo.planning.ui.controller.RootLayoutController;

public class PlanningMainUIApp extends Application {

	private Stage primaryStage;
    
	final private ObservableList<Worker> physicians = FXCollections.observableArrayList();
	
	final private ObservableList<PositionCode> positions = FXCollections.observableArrayList();
	
	private GenerationOverviewController generationOverviewController;
	
	private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	
	@Override
	public void start(Stage primaryStage) {

		this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Planning");

        try {        	
            // LOADS THE ROOT LAYOUT
        	BorderPane rootLayout = loadRootLayout();

        	// USE THIS ELEMENT AS ROOT FOR THE SCENE
            Scene scene = new Scene(rootLayout);

            primaryStage.setScene(scene);
            
            primaryStage.setOnCloseRequest( (event) -> {
            	generationOverviewController.handlePauseButton();
            });
            
            primaryStage.show();
        } catch (IOException e) {
            // Exception gets thrown if the fxml file could not be loaded
            e.printStackTrace();
        }
        
	}
	
	private BorderPane loadRootLayout() throws IOException {
		// LOADS UI DEFINITION
        FXMLLoader loader = new FXMLLoader(PlanningMainUIApp.class.getResource("view/RootLayout.fxml"));

        // LOADS ROOT LAYOUT
        BorderPane borderPane = (BorderPane) loader.load();

        // RETRIEVES CONTROLLER
        RootLayoutController controller = loader.getController();

        // SETS CONTROLLER DEFINITIONS
        controller.setMainApp(this);
        
        // LOADS THE PHYSICIAN OVERVIEW
        AnchorPane physicianOverview = loadPhysicianOverview();
        
        // LOADS THE POSITIONS CODE
        AnchorPane positionsOverview = loadPositionsOverview();
        
        // LOADS THE GENERATION OVERVIEW
        AnchorPane generationOverview = loadGenerationOverview();

        // SETS THE TAB PANES
        TabPane tabPane = (TabPane) borderPane.getCenter();
        tabPane.getTabs().get(0).setContent(physicianOverview);
        tabPane.getTabs().get(1).setContent(positionsOverview);
        tabPane.getTabs().get(2).setContent(generationOverview);
        
        return borderPane;
	}

    public AnchorPane loadPhysicianOverview() throws IOException {
    	// LOADS UI DEFINITION
    	FXMLLoader loader = new FXMLLoader(PlanningMainUIApp.class.getResource("view/PhysicianOverview.fxml"));
        
    	// LOADS LAYOUT
    	AnchorPane overviewPage = (AnchorPane) loader.load();

    	// RETRIEVES CONTROLLER
    	PhysicianOverviewController controller = loader.getController();

    	// SETS CONTROLLER DEFINITIONS
    	controller.setMainApp(this);
    	controller.setDateFormatter(dateFormatter);
    	
    	return overviewPage;
    }
    
    public AnchorPane loadPositionsOverview() throws IOException {
    	// LOADS UI DEFINITION
    	FXMLLoader loader = new FXMLLoader(PlanningMainUIApp.class.getResource("view/PositionsOverview.fxml"));
    	
    	// LOADS LAYOUT
    	AnchorPane positionsOverview = (AnchorPane) loader.load();
    	
    	// RETRIEVES THE CONTROLLER
    	PositionsOverviewController controller = loader.getController();
    	
    	// SETS CONTROLLER DEFINITIONS
    	controller.setMainApp(this);
    	
    	return positionsOverview;
    }
    
    public AnchorPane loadGenerationOverview() throws IOException {
    	// LOADS UI DEFINITION
    	FXMLLoader loader = new FXMLLoader(PlanningMainUIApp.class.getResource("view/GenerationOverview.fxml"));
        
    	// LOADS LAYOUT
    	AnchorPane overviewPage = (AnchorPane) loader.load();

    	// RETRIEVES CONTROLLER
    	generationOverviewController = loader.getController();

    	// SETS CONTROLLER DEFINITIONS
    	generationOverviewController.setMainApp(this);
    	generationOverviewController.setDateFormatter(dateFormatter);
    	
    	return overviewPage;
    }

    public ObservableList<Worker> getPhysicians() {
		return physicians;
	}

    public boolean showPhysicianEditDialog(Worker physician) {
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

	public ObservableList<PositionCode> getPositions() {
		return positions;
	}
}
