package com.github.aiderpmsi.planning.ui.controller;

import org.controlsfx.dialog.Dialogs;

import com.github.aiderpmsi.planning.physician.Physician;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class PhysicianEditDialogController {

	@FXML
	private TextField nameField;
	
	@FXML
	private TextField timePartField;
	
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

        nameField.setText(physician.getName());
        timePartField.setText(physician.getTimePart().toString());
    }

    public boolean isOkClicked() {
        return okClicked;
    }
    
    @FXML
    private void handleOk() {
        if (isInputValid()) {
            physician.setName(nameField.getText());
            physician.setTimePart(Integer.decode(timePartField.getText()));

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
