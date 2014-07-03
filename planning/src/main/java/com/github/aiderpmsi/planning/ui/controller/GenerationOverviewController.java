package com.github.aiderpmsi.planning.ui.controller;

import java.time.LocalDate;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;

import com.github.aiderpmsi.planning.ui.PlanningMainUIApp;

public class GenerationOverviewController {

	@FXML
    private DatePicker startPeriodPicker;
    @FXML
    private DatePicker endPeriodPicker;

    @FXML
    private Label nbTestsLabel;
    @FXML
    private Label maxIndiceLabel;
    @FXML
    private Label minIndiceLabel;
    
    @FXML
    private Button pauseButton;
    @FXML
    private Button saveButton;
    
    /** Reference to the main application */
    private PlanningMainUIApp mainApp;

    @FXML
    private void initialize() {
    	// INITIALIZES THE BUTTONS VISIBILITY
    	pauseButton.setDisable(true);
    	saveButton.setDisable(true);
    	
    	startPeriodPicker.setValue(LocalDate.now());
    	endPeriodPicker.setValue(LocalDate.now());
    	
    	startPeriodPicker.setOnAction(
    			(event) -> {if (startPeriodPicker.getValue() == null) startPeriodPicker.setValue(LocalDate.now());});
    	endPeriodPicker.setOnAction(
    			(event) -> {if (startPeriodPicker.getValue() == null) startPeriodPicker.setValue(LocalDate.now());});

    	// CLEARS THE TESTS LABELS
    	showFeedBack("", "", "");

    }
    
    private void showFeedBack(String nbTests, String maxIndice, String minIndice) {
    	nbTestsLabel.setText(nbTests == null ? "" : nbTests);
    	maxIndiceLabel.setText(maxIndice == null ? "" : maxIndice);
    	minIndiceLabel.setText(minIndice == null ? "" : minIndice);
    }
    
    public void setMainApp(PlanningMainUIApp mainApp) {
        this.mainApp = mainApp;
    }

}
