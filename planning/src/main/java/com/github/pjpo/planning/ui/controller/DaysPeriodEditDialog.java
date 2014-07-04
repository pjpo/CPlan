package com.github.pjpo.planning.ui.controller;

import com.github.pjpo.planning.physician.Physician;
import com.github.pjpo.planning.utils.DaysPeriod;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;

public class DaysPeriodEditDialog {

	@FXML
	private DatePicker startDatePicker;
	
	@FXML
	private DatePicker endDatePicker;
	
	private boolean okClicked;
	
	private Stage dialogStage;

	@FXML
	public void initialize() {
		// DO NOTHING
	}
	
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public void setDaysPeriod(DaysPeriod period) {
    	
    }
	
}
