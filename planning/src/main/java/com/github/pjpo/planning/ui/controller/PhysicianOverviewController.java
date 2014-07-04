package com.github.pjpo.planning.ui.controller;

import java.time.format.DateTimeFormatter;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import org.controlsfx.dialog.Dialogs;

import com.github.pjpo.planning.physician.Physician;
import com.github.pjpo.planning.physician.PhysicianBuilder;
import com.github.pjpo.planning.ui.PlanningMainUIApp;

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
    @FXML
    private Label startWorkLabel;
    @FXML
    private Label endWorkLabel;
    
    /** Date Formatter */
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
    	startWorkLabel.setText(physician == null ? "" : (physician.getWorkStart() == null ? "Non défini" : physician.getWorkStart().format(dateFormatter)));
    	endWorkLabel.setText(physician == null ? "" : (physician.getWorkEnd() == null ? "Non défini" : physician.getWorkEnd().format(dateFormatter)));
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

	public void setDateFormatter(DateTimeFormatter dateFormatter) {
		this.dateFormatter = dateFormatter;
	}
    
}
