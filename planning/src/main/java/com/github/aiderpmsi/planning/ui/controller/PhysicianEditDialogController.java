package com.github.aiderpmsi.planning.ui.controller;

import java.time.format.DateTimeFormatter;

import org.controlsfx.dialog.Dialogs;

import com.github.aiderpmsi.planning.physician.Physician;

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
        String errorMessage = "";

        if (nameField.getText() == null || nameField.getText().length() == 0) {
            errorMessage += "Nom invalide!\n"; 
        }
        
        if (timePartField.getText() == null || timePartField.getText().length() == 0) {
            errorMessage += "Proportion de temps invalide!\n"; 
        } else {
            // TRY TO PARSE THIS AS AN INT
            try {
            	Integer timePartI = Integer.parseInt(timePartField.getText());
            	if (timePartI < 1 || timePartI > 100)
            		throw new NumberFormatException();
            } catch (NumberFormatException e) {
                errorMessage += "Proportion de temps invalide (doit être un entier entre 1 et 100)!\n"; 
            }
        }
        
        if (errorMessage.length() == 0) {
        	return true;
        } else {
        	Dialogs.create()
        	.owner(dialogStage)
        	.title("Information Dialog")
            .masthead("Invalid fields")
            .message(errorMessage)
            .showError();
        	return false;
        }
    }
}
