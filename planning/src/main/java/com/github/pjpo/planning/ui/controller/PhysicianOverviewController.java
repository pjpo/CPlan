package com.github.pjpo.planning.ui.controller;

import java.time.format.DateTimeFormatter;
import java.util.LinkedList;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;

import com.github.pjpo.planning.model.Physician;
import com.github.pjpo.planning.ui.PlanningMainUIApp;
import com.google.common.collect.HashMultimap;

public class PhysicianOverviewController {

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

    /** Date Formatter */
    @SuppressWarnings("unused")
	private DateTimeFormatter dateFormatter;

    /** Reference to the main application */
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
        		(observable, oldValue, newValue) -> showPhysicianDetails(newValue));
    }
    
    private void showPhysicianDetails(Physician physician) {
    	nameLabel.setText(physician == null ? "" : (physician.getName() == null ? "Non défini" : physician.getName()));
    	parttimeLabel.setText(physician == null ? "" : (physician.getTimePart() == null ? "Non défini" : physician.getTimePart().toString()));
    }
    
    @FXML
    private void handleDeletePhysician() {
      int selectedIndex = physicianTable.getSelectionModel().getSelectedIndex();
      if (selectedIndex >= 0) {
    	  physicianTable.getItems().remove(selectedIndex);
      } else {
    	  // NOTHING SELECTED
    	  Alert alert = new Alert(AlertType.INFORMATION);
    	  alert.setTitle("Information");
    	  alert.setHeaderText("No person selected");
    	  alert.setContentText("Please select a physician in the table");
    	  alert.showAndWait();
      }
    }
    
    @FXML
    private void handleNewPhysician() {
      final Physician tempPhysician = new Physician();
      tempPhysician.setName("");
      tempPhysician.setTimePart(100);
      tempPhysician.setPaidVacation(new LinkedList<>());
      tempPhysician.setRefusedPostes(new LinkedList<>());
      tempPhysician.setUnpaidVacation(new LinkedList<>());
      tempPhysician.setWorkedVacs(HashMultimap.create());
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
    	  Alert alert = new Alert(AlertType.INFORMATION);
    	  alert.setTitle("Information");
    	  alert.setHeaderText("No physician selected");
    	  alert.setContentText("Select a physician");
    	  alert.showAndWait();
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

	public void setDateFormatter(DateTimeFormatter dateFormatter) {
		this.dateFormatter = dateFormatter;
	}
    
}
