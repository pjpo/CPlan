package com.github.pjpo.planning.ui.controller;

import java.time.format.DateTimeFormatter;

import org.controlsfx.dialog.Dialogs;

import com.github.pjpo.planning.physician.Physician;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class PhysicianEditDialogController {

	@FXML
	private TextField nameField;
	
	@FXML
	private TextField timePartField;
	
	@FXML
	private DatePicker startWorkPicker;
	
	@FXML
	private DatePicker endWorkPicker;

	
	@SuppressWarnings("unused")
	private DateTimeFormatter dateFormatter;
	
	private Stage dialogStage;
    
	private Physician physician;

    private boolean okClicked = false;
    
    @FXML
    private void initialize() {
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public void setPhysician(Physician physician) {
        this.physician = physician;

        nameField.setText(physician.getName() == null ? "" : physician.getName());
        timePartField.setText(physician.getTimePart() == null ? "" : physician.getTimePart().toString());
        startWorkPicker.setValue(physician.getWorkStart());
        endWorkPicker.setValue(physician.getWorkEnd());
    }

    public void setDateFormatter(DateTimeFormatter dateFormatter) {
		this.dateFormatter = dateFormatter;
	}

	public boolean isOkClicked() {
        return okClicked;
    }
    
    @FXML
    private void handleOk() {
        if (isInputValid()) {
            physician.setName(nameField.getText());
            physician.setTimePart(Integer.decode(timePartField.getText()));
            physician.setWorkStart(startWorkPicker.getValue());
            physician.setWorkEnd(endWorkPicker.getValue());

            okClicked = true;
            dialogStage.close();
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private boolean isInputValid() {
        StringBuilder errorMessageBuilder = new StringBuilder();

        if (nameField.getText() == null || nameField.getText().length() == 0) {
            errorMessageBuilder.append("Nom invalide!\n"); 
        }
        
        if (timePartField.getText() == null || timePartField.getText().length() == 0) {
            errorMessageBuilder.append("Proportion de temps invalide!\n"); 
        } else {
            // TRY TO PARSE THIS AS AN INT
            try {
            	Integer timePartI = Integer.parseInt(timePartField.getText());
            	if (timePartI < 1 || timePartI > 100)
            		throw new NumberFormatException();
            } catch (NumberFormatException e) {
            	errorMessageBuilder.append("Proportion de temps invalide (doit être un entier entre 1 et 100)!\n"); 
            }
        }
        
        if (startWorkPicker.getValue() != null && endWorkPicker.getValue() != null) {
        	// CHECK IF END OF WORK IS GREATER THAN START OF WORK
        	if (!endWorkPicker.getValue().isAfter(startWorkPicker.getValue())) {
        		errorMessageBuilder.append("Fin de période de travail doit être supérieur à début de période de travail!\n");
        	}
        }
        
        if (errorMessageBuilder.length() == 0) {
        	return true;
        } else {
        	Dialogs.create()
        	.owner(dialogStage)
        	.title("Information Dialog")
            .masthead("Invalid fields")
            .message(errorMessageBuilder.toString())
            .showError();
        	return false;
        }
    }
}
