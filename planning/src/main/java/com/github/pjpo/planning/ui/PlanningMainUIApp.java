package com.github.pjpo.planning.ui;

import java.io.IOException;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.github.pjpo.planning.model.PositionDefinition;
import com.github.pjpo.planning.model.Worker;
import com.github.pjpo.planning.ui.controller.ConstraintsOverviewController;
import com.github.pjpo.planning.ui.controller.GenerationOverviewController;
import com.github.pjpo.planning.ui.controller.PhysicianEditDialogController;
import com.github.pjpo.planning.ui.controller.PhysicianOverviewController;
import com.github.pjpo.planning.ui.controller.PositionsOverviewController;
import com.github.pjpo.planning.ui.controller.RootLayoutController;

/**
 * Entry point for javafx application 
 * @author jp@dm.lan
 *
 */
public class PlanningMainUIApp extends Application {

	/** Root stage */
	private Stage primaryStage;
    
	/** Defined employees shown in user interface */
	final private ObservableList<Worker> employees = FXCollections.observableArrayList();
	
	/** Positions definitions shown in ui */
	final private ObservableList<PositionDefinition> positions = FXCollections.observableArrayList();

	/** Code for constraints shown in ui */
	final private SimpleStringProperty constraintsCode = new SimpleStringProperty("");
	
	@Override
	public void start(final Stage primaryStage) {

		this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Planning");

        try {        	
            // LOADS THE ROOT LAYOUT
        	loadRootLayout(primaryStage);
            
            primaryStage.show();
        } catch (final IOException e) {
            // Exception gets thrown if the fxml file could not be loaded
            e.printStackTrace();
        }
        
	}

	/**
	 * Loads the root layout and fills the different tabs
	 * @return
	 * @throws IOException
	 */
	private BorderPane loadRootLayout(final Stage stage) throws IOException {
		// LOADS UI DEFINITION
		final BorderPane borderPane =
				load("view/RootLayout.fxml",
						(BorderPane fxml, RootLayoutController controller) -> {
							controller.setMainApp(this);
							return fxml;
						});

    	// Use this element as root scene
        final Scene scene = new Scene(borderPane);

        stage.setScene(scene);

        // Loads employee overview
		final AnchorPane employeePane =
				load("view/PhysicianOverview.fxml",
						(AnchorPane fxml, PhysicianOverviewController controller) -> {
							controller.setMainApp(this);
							return fxml;
						});
        
		// Loads positions definitions
		final AnchorPane positionsPane =
				load("view/PositionsOverview.fxml",
						(AnchorPane fxml, PositionsOverviewController controller) -> {
							controller.setMainApp(this);
							return fxml;
						});

		// Loads constraints definitions
		final AnchorPane constraintPane =
				load("view/ConstraintsOverview.fxml",
						(AnchorPane fxml, ConstraintsOverviewController controller) -> {
							controller.setMainApp(this);
							return fxml;
						});

        // Loads generation ui
		final AnchorPane generationPane =
				load("view/GenerationOverview.fxml",
						(AnchorPane fxml, GenerationOverviewController controller) -> {
							controller.setMainApp(this);
					        // Stops the planning generation if window is closed
							stage.setOnCloseRequest( (event) -> {
					        	controller.handlePauseButton();
					        });
							return fxml;
						});
		
        // SETS THE TAB PANES
        final TabPane tabPane = (TabPane) borderPane.getCenter();
        tabPane.getTabs().get(0).setContent(employeePane);
        tabPane.getTabs().get(1).setContent(positionsPane);
        tabPane.getTabs().get(2).setContent(constraintPane);
        tabPane.getTabs().get(3).setContent(generationPane);
        
        return borderPane;
	}

	private <T, U, V> V load(
			final String fxmlSource,
			final FxmlLoader<T, U, V> initFunction) throws IOException {
		// URL of fxml with ui definition
		final FXMLLoader loader = new FXMLLoader(PlanningMainUIApp.class.getResource(fxmlSource));
		
		// Loads the fxml
		final T fxml = loader.load();
		
        // retrieves the controller
		final U controller = loader.getController();
		
		// Executes the corresponding code
		return initFunction.apply(fxml, controller);
	}

    public boolean showPhysicianEditDialog(Worker physician) {
    	try {
    		// Load the fxml file and create a new stage for the popup
    		return load("view/PhysicianEditDialog.fxml",
    				(AnchorPane fxml, PhysicianEditDialogController controller) -> {
    			// Creates a new window
    			final Stage dialogStage = new Stage();
    	  	    dialogStage.setTitle("Modifier médecin");
    	  	    dialogStage.initModality(Modality.WINDOW_MODAL);
    	  	    dialogStage.initOwner(primaryStage);
    	  	    final Scene scene = new Scene(fxml);
    	  	    dialogStage.setScene(scene);

    	  	    // Sets the parameters of controller
    	  	    controller.setDialogStage(dialogStage);
    	  	    controller.setPhysician(physician);

    	  	    // Show the dialog and wait until the user closes it
    	  	    dialogStage.showAndWait();
    	  	    
    	  	    return controller.isOkClicked();
    		});
    	} catch (IOException e) {
    		// Exception gets thrown if the fxml file could not be loaded
    		e.printStackTrace();
    		return false;
    	}
    }

	public static void main(String[] args) {
		launch(args);
	}

    public Stage getPrimaryStage() {
		return primaryStage;
	}

	public ObservableList<PositionDefinition> getPositions() {
		return positions;
	}
	
	public SimpleStringProperty getConstraintsCode() {
		return constraintsCode;
	}
	
    public ObservableList<Worker> getPhysicians() {
		return employees;
	}

	@FunctionalInterface
	private interface FxmlLoader<T, U, V> {
		public V apply(T fxml, U controller);
	}
}
