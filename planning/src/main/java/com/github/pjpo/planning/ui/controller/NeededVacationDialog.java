package com.github.pjpo.planning.ui.controller;

import java.io.IOException;
import com.github.pjpo.planning.ui.PlanningMainUIApp;
import com.github.pjpo.planning.ui.model.Poste;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class NeededVacationDialog {

	@FXML
	private DatePicker datePicker;
	
	@FXML
	private TextField positionField;

	private Stage stage;
	
	private Poste poste;
	
	private boolean okClicked = false;
	
	@FXML
	private void handleOk() {
		setOkClicked(true);
		stage.close();
	}
	
	@FXML
	private void handleCancel() {
		setOkClicked(false);
		stage.close();
	}

	public boolean isOkClicked() {
		return okClicked;
	}

	private void setOkClicked(boolean okClicked) {
		this.okClicked = okClicked;
	}
	
	public Stage getStage() {
		return stage;
	}

	private void setStage(Stage stage) {
		this.stage = stage;
	}

	public Poste getPoste() {
		if (datePicker.getValue() == null || positionField.getText().length() == 0) {
			poste.setDate(null);
			poste.setPoste(null);
		} else {
			poste.setDate(datePicker.getValue());
			poste.setPoste(positionField.getText());
		}
		return poste;
	}

	private void setPoste(Poste poste) {
		this.poste = poste;
		// SETS THE START OF INTERVAL
		if (poste.getDate() != null) {
			datePicker.setValue(poste.getDate());
		}
		// SETS THE END OF INTERVAL
		if (poste.getPoste() != null) {
			positionField.setText(poste.getPoste());
		}
	}

	public static NeededVacationDialog showDialog(
			Stage parentStage,
			String title,
			Poste poste) {
	
		try {
			// Loads the fxml file and create a new stage for the popup
			FXMLLoader loader = new FXMLLoader(PlanningMainUIApp.class.getResource("view/NeededVacatioinDialog.fxml"));
			AnchorPane page = (AnchorPane) loader.load();
			Stage dialogStage = new Stage();
			dialogStage.setTitle(title);
    		dialogStage.initModality(Modality.WINDOW_MODAL);
    		dialogStage.initOwner(parentStage);
    		Scene scene = new Scene(page);
    		dialogStage.setScene(scene);

    		// Sets the interval into the controller
    		NeededVacationDialog controller = loader.getController();
    		controller.setStage(dialogStage);
    		controller.setPoste(poste);

    		// Show the dialog and wait until the user closes it
    		dialogStage.showAndWait();

    		return controller;

		} catch (IOException e) {
			// Exception gets thrown if the fxml file could not be loaded
			e.printStackTrace();
			return null;
		}
	}

}
