package com.github.aiderpmsi.planning.ui.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import org.controlsfx.dialog.Dialogs;

import com.github.aiderpmsi.planning.physician.Physician;
import com.github.aiderpmsi.planning.physician.PhysicianBuilder;
import com.github.aiderpmsi.planning.ui.PlanningMainUIApp;

public class PhysicianController {

	@FXML
    private TableView<Physician> physicianTable;
    @FXML
    private TableColumn<Physician, String> nameColumn;
    @FXML
    private TableColumn<Physician, Integer> parttimeColumn;

    @FXML
    private Label nameLabel;
    @FXML
    private Label parttimeLabel;
    
    // Reference to the main application
    private PlanningMainUIApp mainApp;

    @FXML
    private void initialize() {
    	// Initialize the person table
        nameColumn.setCellValueFactory(new PropertyValueFactory<Physician, String>("name"));
        parttimeColumn.setCellValueFactory(new PropertyValueFactory<Physician, Integer>("timePart"));
        
        // clear person
        showPhysicianDetails(null);

        // Listen for selection changes
        physicianTable.getSelectionModel().selectedItemProperty().addListener(
        		new ChangeListener<Physician>() {

        			@Override
        			public void changed(ObservableValue<? extends Physician> observable,
        					Physician oldValue, Physician newValue) {
        				showPhysicianDetails(newValue);
        			}
        		});
    }
    
    private void showPhysicianDetails(Physician physician) {
    	if (physician == null) {
    		nameLabel.setText("");
    		parttimeLabel.setText("");
    	} else {
    		nameLabel.setText(physician.getName());
    		parttimeLabel.setText(physician.getTimePart().toString());
    	}
    }

    @FXML
    private void handleDeletePhysician() {
      int selectedIndex = physicianTable.getSelectionModel().getSelectedIndex();
      if (selectedIndex >= 0) {
    	  physicianTable.getItems().remove(selectedIndex);
      } else {
    	  // NOTHING SELECTED
    	  Dialogs.create()
    	  .owner(mainApp.getPrimaryStage())
    	  .title("Information Dialog")
          .masthead("No person selected")
          .message("Please select a physician in the table")
          .showInformation();
      }
    }
    
    @FXML
    private void handleNewPhysician() {
      Physician tempPhysician = new PhysicianBuilder().setName("").setTimePart(100).toPhysician();
      boolean okClicked = mainApp.showPhysicianEditDialog(tempPhysician);
      if (okClicked) {
        mainApp.getPhysicians().add(tempPhysician);
      }
    }

    /**
     * Called when the user clicks the edit button.
     * Opens a dialog to edit details for the selected person.
     */
    @FXML
    private void handleEditPhysician() {
      Physician selectedPhysician = physicianTable.getSelectionModel().getSelectedItem();
      if (selectedPhysician != null) {
        boolean okClicked = mainApp.showPhysicianEditDialog(selectedPhysician);
        if (okClicked) {
          refreshPhysicianTable();
          showPhysicianDetails(selectedPhysician);
        }

      } else {
        // Nothing selected
        Dialogs.create()
        .owner(mainApp.getPrimaryStage())
        .title("Information dialog")
        .masthead("No physician selected")
        .message("Select a physician")
        .showInformation();
      }
    }
    
    private void refreshPhysicianTable() {
    	  int selectedIndex = physicianTable.getSelectionModel().getSelectedIndex();
    	  physicianTable.setItems(null);
    	  physicianTable.layout();
    	  physicianTable.setItems(mainApp.getPhysicians());
    	  physicianTable.getSelectionModel().select(selectedIndex);
    }
    
    /**
     * Is called by the main application to give a reference back to itself.
     * 
     * @param mainApp
     */
    public void setMainApp(PlanningMainUIApp mainApp) {
        this.mainApp = mainApp;

        // Add observable list data to the table
        physicianTable.setItems(mainApp.getPhysicians());
    }
    
}
