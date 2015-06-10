package com.github.pjpo.planning.ui.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import com.github.pjpo.planning.model.PositionDefinition;
import com.github.pjpo.planning.ui.PlanningMainUIApp;

public class PositionsOverviewController {

	@FXML
    private TextField positionName;
    @FXML
    private TextArea codeArea;
    
	@FXML
    private TableView<PositionDefinition> positionNamesTable;
    @FXML
    private TableColumn<PositionDefinition, String> positionNameColumn;

    /** Reference to the main application */
    private PlanningMainUIApp mainApp;

	@FXML
    private void initialize() {
		// Initialize the person table
		positionNameColumn.setCellValueFactory(
				param ->
				new SimpleStringProperty(param.getValue().getName() == null ? "A définir" : param.getValue().getName()));
        
        // clear code
        showPositionDetails(null);

        // Listen for selection changes
        positionNamesTable.getSelectionModel().selectedItemProperty().addListener(
        		(observable, oldValue, newValue) -> showPositionDetails(newValue));

    }
	
	public void setMainApp(PlanningMainUIApp mainApp) {
		this.mainApp = mainApp;
        // Add observable list data to the table
        positionNamesTable.setItems(mainApp.getPositions());
	}
	
    private void showPositionDetails(PositionDefinition position) {
    	positionName.setText(position == null ? "" : (position.getName() == null ? "Non défini" : position.getName()));
    	codeArea.setText(position == null ? "" : (position.getScript() == null ? "Non défini" : position.getScript()));
    }

    @FXML
    public void newHandler() {
    	
    	final PositionDefinition positionCode = new PositionDefinition();
    	positionCode.setScript("");
    
    	mainApp.getPositions().add(positionCode);
    }
    
    @FXML
    public void deleteHandler() {
    	int selectedIndex = positionNamesTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
        	positionNamesTable.getItems().remove(selectedIndex);
        } else {
      	  // NOTHING SELECTED
      	  Alert alert = new Alert(AlertType.INFORMATION);
      	  alert.setTitle("Information");
      	  alert.setHeaderText("No position selected");
      	  alert.setContentText("Please select a position in the table");
      	  alert.showAndWait();
        }
    }

    @FXML
    public void saveHandler() {
    	int selectedIndex = positionNamesTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
        	final ObservableList<PositionDefinition> positionCodes = positionNamesTable.getItems(); 
        	final PositionDefinition positionCode = positionCodes.get(selectedIndex);
        	positionCode.setName(positionName.getText());
        	positionCode.setScript(codeArea.getText());
        	// Refresh element
        	positionCodes.set(selectedIndex, positionCode);
        } else {
      	  // NOTHING SELECTED
      	  Alert alert = new Alert(AlertType.INFORMATION);
      	  alert.setTitle("Information");
      	  alert.setHeaderText("No position selected");
      	  alert.setContentText("Please select a position in the table");
      	  alert.showAndWait();
        }
    }

}
