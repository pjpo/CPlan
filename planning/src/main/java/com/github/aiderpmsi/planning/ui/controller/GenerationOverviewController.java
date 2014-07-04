package com.github.aiderpmsi.planning.ui.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.util.StringConverter;

import org.controlsfx.dialog.Dialogs;

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
    
    /** Indicates if the calculation have to stop */
    private boolean continueGeneration = false;
    	
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

    	Task<LinkedList<Solution>> task = new Task<LinkedList<Solution>>() {

			@Override protected LinkedList<Solution> call() throws Exception {
				// INIT VARS
				LinkedList<Solution> solutions = new LinkedList<>();
				Integer retrys = 0;
				
				for (;;) {
					// FIRST CHECK GENERATION STATUS
					try {
						lock.lock();
						if (!continueGeneration || isCancelled())
							break;
					} finally {
						lock.unlock();
					}
					
	    			// GENERATION STATUS OK, SEARCHES A SOLUTION
    				retrys++;
    				final Solution solution = planning.findSolution(solutions);
    				final Integer nbRetrys = new Integer(retrys);
    				
    				if (solution != null) {
    					// UPDATES VALUES IN LABELS
    					Platform.runLater(() -> {
    						showFeedBack(nbRetrys, solution.getMaxWorkLoad(), solution.getMinWorkLoad());
    					});
    					// ADDS THIS SOLUTION AS LAST SOLUTION
    					solutions.add(solution);
    				} else {
    					Platform.runLater(() -> nbTestsLabel.setText(nbRetrys.toString()));
    				}
	    		}
				// HERE, RETURN SOLUTIONS
				return solutions;
			}
    	};
    	
    	task.setOnFailed( (event) -> {
    		Throwable exception  =
    				event.getSource().getException() == null ?
    						new Exception("Erreur inconnue") :
    							event.getSource().getException();
    		if (exception instanceof SolutionException) 
    			Dialogs.create()
    			.owner(mainApp.getPrimaryStage())
    			.masthead("Pas de solution")
    			.message(exception.getMessage())
    			.showError();
    		else
    			Dialogs.create()
    			.owner(mainApp.getPrimaryStage())
    			.masthead("Erreur")
    			.showException(exception);
			setButtonsStatus(true, false, false);
		});

    	task.setOnSucceeded( (event) -> {
    		setButtonsStatus(true, false, true);
    	});
    	
    	new Thread(task).start();
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
		nbTestsLabel.setText(nbTests == null ? "" : nbTests.toString());
		maxIndiceLabel.setText(maxIndice == null ? "" : maxIndice.toString());
		minIndiceLabel.setText(minIndice == null ? "" : minIndice.toString());
    }
    
    private void setButtonsStatus(boolean generateStatus, boolean pauseStatus, boolean saveStatus) {
		generateButton.setDisable(!generateStatus);
		pauseButton.setDisable(!pauseStatus);
		saveButton.setDisable(!saveStatus);
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
