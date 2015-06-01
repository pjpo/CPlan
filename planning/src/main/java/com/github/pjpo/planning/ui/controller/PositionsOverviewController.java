package com.github.pjpo.planning.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import com.github.pjpo.planning.model.PositionCode;
import com.github.pjpo.planning.ui.PlanningMainUIApp;

public class PositionsOverviewController {

	@FXML
    private TextField positionName;
    @FXML
    private TextArea codeArea;
    
	@FXML
    private TableView<PositionCode> positionsTable;
    @FXML
    private TableColumn<PositionCode, String> nameColumn;

    /** Reference to the main application */
    private PlanningMainUIApp mainApp;

	@FXML
    private void initialize() {
		// Initialize the person table
		nameColumn.setCellValueFactory(new PropertyValueFactory<PositionCode, String>("name"));
        
        // clear code
        showPositionDetails(null);

        // Listen for selection changes
        positionsTable.getSelectionModel().selectedItemProperty().addListener(
        		(observable, oldValue, newValue) -> showPositionDetails(newValue));

    }
	
    private void showPositionDetails(PositionCode position) {
    	positionName.setText(position == null ? "" : (position.getName() == null ? "Non défini" : position.getName()));
    }

    @FXML
    public void newHandler() {
    	
    	final PositionCode positionCode = new PositionCode();
    	positionCode.setName("");
    	positionCode.setCode("");
    
    	mainApp.getPositions().add(positionCode);
    }
    
    @FXML
    public void deleteHandler() {
    	int selectedIndex = positionsTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
        	positionsTable.getItems().remove(selectedIndex);
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
    	int selectedIndex = positionsTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
        	final PositionCode positionCode = positionsTable.getItems().get(selectedIndex);
        	positionCode.setName(positionName.getText());
        	positionCode.setCode(codeArea.getText());
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
