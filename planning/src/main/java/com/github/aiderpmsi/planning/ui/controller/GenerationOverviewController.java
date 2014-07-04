package com.github.aiderpmsi.planning.ui.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;
import org.controlsfx.dialog.Dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.util.StringConverter;

import com.github.aiderpmsi.planning.Planning;
import com.github.aiderpmsi.planning.Solution;
import com.github.aiderpmsi.planning.SolutionException;
import com.github.aiderpmsi.planning.jours.JourChuMtp;
import com.github.aiderpmsi.planning.ui.PlanningMainUIApp;

public class GenerationOverviewController {


	@FXML
    private DatePicker startPeriodPicker;
    @FXML
    private DatePicker endPeriodPicker;
    @FXML
    private Button generateButton;

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

    /** Locks multithreaded variables */
    private ReentrantLock lock = new ReentrantLock();
    
    /** Indicates if the calculation havs to stop */
    private boolean continueGeneration = false;
    
	/** List of calculated Solutions */
	private LinkedList<Solution> solutions = null;
	
	/** Date formatter */
	private DateTimeFormatter dateFormatter;

	@FXML
    private void initialize() {
    	// INITIALIZES THE BUTTONS VISIBILITY
    	setButtonsStatus(true, false, false);
    	
    	startPeriodPicker.setValue(LocalDate.now());
    	
    	endPeriodPicker.setValue(startPeriodPicker.getValue().plusDays(1));
    	endPeriodPicker.setDayCellFactory( (datePicker) -> {
    		return new DateCell() {
    			@Override public void updateItem(LocalDate item, boolean empty) {
    				super.updateItem(item, empty);
    				if (!item.isAfter(startPeriodPicker.getValue())) {
    					setDisable(true);
    					setStyle("-fx-background-color: #ffc0cb;");
    				}
    			}
    		};
    	});

    	startPeriodPicker.setOnAction(
    			(event) -> {if (startPeriodPicker.getValue() == null) startPeriodPicker.setValue(LocalDate.now());});
    	endPeriodPicker.setOnAction(
    			(event) -> {
    				if (endPeriodPicker.getValue() == null)
    					endPeriodPicker.setValue(startPeriodPicker.getValue().plusDays(1));
    				});

    	// CLEARS THE TESTS LABELS
    	showFeedBack(null, null, null);

    }
    
    public void handleGenerateButton() {
    	// ADAPTS BUTTONS VISIBILITY
    	setButtonsStatus(false, true, false);
    	
    	// SETS THE GENERATION CONTINUATION POLICY
    	continueGeneration = true;
    	
    	// INIT PLANNING
    	Planning planning = new Planning(
    			startPeriodPicker.getValue(),
    			endPeriodPicker.getValue(),
    			new ArrayList<>(mainApp.getPhysicians()),
    			new JourChuMtp());
    	
    	// INIT SOLUTIONS
    	solutions = new LinkedList<>();
    	
    	new Thread( () -> {
    		boolean canSave = false;
    		Integer retrys = 0;
    		for (;;) {
    			// FIRST CHECK GENERATION STATUS
    			try {
    				lock.lock();
    				if (!continueGeneration)
    					break;
    			} finally {
    				lock.unlock();
    			}
    			// GENERATION STATUS OK, SEARCHES A SOLUTION
    			try {
    				retrys++;
    				Solution solution = planning.findSolution(solutions);
    				
    				if (solution != null) {
    					// UPDATES VALUES IN LABELS
    					showFeedBack(retrys, solution.getMaxWorkLoad(), solution.getMinWorkLoad());
    					// ADDS THIS SOLUTION AS LAST SOLUTION
    					solutions.add(solution);
    					canSave = true;
    				}
    			} catch (SolutionException e) {
    				Dialogs.create()
    				.owner(mainApp.getPrimaryStage())
    				.showException(e);
    				canSave = false;
    				break;
    			}
    		}
    		// HERE, REINIT BUTTONS
    		setButtonsStatus(true, false, canSave);
    	});
    }	
    
    public void handlePauseButton() {
    	try {
    		lock.lock();
    		continueGeneration = false;
    	} finally {
    		lock.unlock();
    	}
    }
    private void showFeedBack(Integer nbTests, Long maxIndice, Long minIndice) {
    	try {
    		lock.lock();
    		nbTestsLabel.setText(nbTests == null ? "" : nbTests.toString());
    		maxIndiceLabel.setText(maxIndice == null ? "" : maxIndice.toString());
    		minIndiceLabel.setText(minIndice == null ? "" : minIndice.toString());
    	} finally {
    		lock.unlock();
    	}
    }
    
    private void setButtonsStatus(boolean generateStatus, boolean pauseStatus, boolean saveStatus) {
    	try {
    		lock.lock();
    		generateButton.setDisable(!generateStatus);
    		pauseButton.setDisable(!pauseStatus);
    		saveButton.setDisable(!saveStatus);
    	} finally {
    		lock.unlock();
    	}
    }
    
    public void setMainApp(PlanningMainUIApp mainApp) {
        this.mainApp = mainApp;
    }

	public void setDateFormatter(DateTimeFormatter dateFormatter) {
		this.dateFormatter = dateFormatter;
		// ONCE THE DATE FORMATTER HAS BEEN SET, WE CAN SET THE DATE HANDLING FOR DATEPICKERS
    	startPeriodPicker.setConverter(
    			new DefaultDatePickerConverter(dateFormatter,
    					null, endPeriodPicker));
    	endPeriodPicker.setConverter(
    			new DefaultDatePickerConverter(dateFormatter,
    					startPeriodPicker, null));
	}

	private class DefaultDatePickerConverter extends StringConverter<LocalDate> {
		
		private DateTimeFormatter dateTimeFormatter;
		private DatePicker beforeDatePicker;
		private DatePicker afterDatePicker;

		public DefaultDatePickerConverter(
				DateTimeFormatter dateTimeFormatter,
				DatePicker beforeDatePicker,
				DatePicker afterDatePicker) {
			this.dateTimeFormatter = dateTimeFormatter;
			this.beforeDatePicker = beforeDatePicker;
			this.afterDatePicker = afterDatePicker;
		}
		
		@Override public String toString(LocalDate object) {
			if (object == null)
				return null;
			else
				return dateFormatter.format(object);
		}

		@Override public LocalDate fromString(String string) {
			if (string == null) {
				return beforeDatePicker.getValue().plusDays(1);
			} else {
				LocalDate newDate = dateTimeFormatter.parse(string, LocalDate::from);
				if (beforeDatePicker != null && !newDate.isAfter(beforeDatePicker.getValue())) {
					beforeDatePicker.setValue(newDate.minusDays(1));
				}
				if (afterDatePicker != null && !newDate.isBefore(afterDatePicker.getValue())) {
					afterDatePicker.setValue(newDate.plusDays(1));
				}
				return newDate;
			}

		}
	};

}
