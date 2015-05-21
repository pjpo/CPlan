package com.github.pjpo.planning.ui.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.github.pjpo.planning.ui.PlanningMainUIApp;
import com.github.pjpo.planning.utils.IntervalDateTime;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DateTimeIntervalEditDialogController {

	@FXML
	private DatePicker startDatePicker;
	
	@FXML
	private TextField startTimeField;

	@FXML
	private DatePicker endDatePicker;
	
	@FXML
	private TextField endTimeField;
	
	private Stage stage;
	
	private IntervalDateTime interval;
	
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

	public IntervalDateTime getInterval() {
		// SETS THE START OF THE INTERVAL
		if (startDatePicker.getValue() != null && startTimeField.getText().length() !=0) {
			LocalDateTime startInterval = LocalDateTime.of(startDatePicker.getValue(), LocalTime.parse(startTimeField.getText()));
			interval.setStart(startInterval);
		} else {
			interval.setStart(null);
		}
		if (endDatePicker.getValue() != null && endTimeField.getText().length() !=0) {
			LocalDateTime endInterval = LocalDateTime.of(endDatePicker.getValue(), LocalTime.parse(endTimeField.getText()));
			interval.setEnd(endInterval);
		}
		return interval;
	}

	private void setInterval(IntervalDateTime interval) {
		this.interval = interval;
		// SETS THE START OF INTERVAL
		if (interval.getStart() != null) {
			startDatePicker.setValue(interval.getStart().toLocalDate());
			startTimeField.setText(interval.getStart().toLocalTime().toString());
		}
		// SETS THE END OF INTERVAL
		if (interval.getEnd() != null) {
			endDatePicker.setValue(interval.getEnd().toLocalDate());
			endTimeField.setText(interval.getEnd().toLocalTime().toString());
		}
	}

	public static DateTimeIntervalEditDialogController showDialog(
			Stage parentStage,
			String title,
			IntervalDateTime interval) {
	
		try {
			// Loads the fxml file and create a new stage for the popup
			FXMLLoader loader = new FXMLLoader(PlanningMainUIApp.class.getResource("view/DateTimeIntervalEditDialog.fxml"));
			AnchorPane page = (AnchorPane) loader.load();
			Stage dialogStage = new Stage();
			dialogStage.setTitle(title);
    		dialogStage.initModality(Modality.WINDOW_MODAL);
    		dialogStage.initOwner(parentStage);
    		Scene scene = new Scene(page);
    		dialogStage.setScene(scene);

    		// Sets the interval into the controller
    		DateTimeIntervalEditDialogController controller = loader.getController();
    		controller.setStage(dialogStage);
    		controller.setInterval(interval);

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
