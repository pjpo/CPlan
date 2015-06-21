package com.github.pjpo.planning.ui.controller;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import com.github.pjpo.planning.model.PositionConstraintBase;
import com.github.pjpo.planning.model.Worker;
import com.github.pjpo.planning.model.dao.DaoConstraints;
import com.github.pjpo.planning.problem.PlanningDefinition;
import com.github.pjpo.planning.problem.PlanningForInterval;
import com.github.pjpo.planning.problem.Solution;
import com.github.pjpo.planning.ui.PlanningMainUIApp;
import com.github.pjpo.planning.ui.controller.utils.DaoSolution;
import com.github.pjpo.planning.ui.controller.utils.PlanningGenerationTask;
import com.github.pjpo.planning.utils.IntervalDate;

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
    private Label optimizeIndexLabel;
    
    @FXML
    private Button pauseButton;
    @FXML
    private Button saveButton;
    
    /** Reference to the main application */
    private PlanningMainUIApp mainApp;
	
    /** Solution set when the generation of planning succeded */
	private Solution solution = null;

	/** Reference to task generating the solution */
	private PlanningGenerationTask task = null; 
	
	@FXML
    private void initialize() {
    	// INITIALIZES THE BUTTONS VISIBILITY
    	setButtonsStatus(true, false, false);

    	// Defines the default value of startperiodpicker and endperiodpicker
    	startPeriodPicker.setValue(LocalDate.now());
    	endPeriodPicker.setValue(startPeriodPicker.getValue().plusDays(1));
    	
    	// Colorises the datecells in endPeriodPicker which are before startPeriodPicker 
    	endPeriodPicker.setDayCellFactory((datePicker) -> {
    		return new DateCell() {
    			@Override public void updateItem(final LocalDate item, final boolean empty) {
    				super.updateItem(item, empty);
    				if (startPeriodPicker.getValue() != null && !item.isAfter(startPeriodPicker.getValue())) {
    					setDisable(true);
    					setStyle("-fx-background-color: #ffc0cb;");
    				}
    			}
    		};
    	});
    	// Colorises the datecells in startPeriodPicker which are after startPeriodPicker 
    	startPeriodPicker.setDayCellFactory((datePicker) -> {
    		return new DateCell() {
    			@Override public void updateItem(final LocalDate item, final boolean empty) {
    				super.updateItem(item, empty);
    				if (endPeriodPicker.getValue() != null && !item.isBefore(endPeriodPicker.getValue())) {
    					setDisable(true);
    					setStyle("-fx-background-color: #ffc0cb;");
    				}
    			}
    		};
    	});

    	// Sets values when actions on start and end date pickers are modified
    	startPeriodPicker.setOnAction(
    			(event) -> {
    				// If a start date is set, check if it is valid, and if it is berfore end date
    				// Correct if if needed
    				if (startPeriodPicker.getValue() == null)
    					startPeriodPicker.setValue(endPeriodPicker.getValue() == null ?
    							LocalDate.now() : endPeriodPicker.getValue().minusDays(1L));
    				else if (endPeriodPicker.getValue() != null && !startPeriodPicker.getValue().isBefore(endPeriodPicker.getValue()))
    					startPeriodPicker.setValue(endPeriodPicker.getValue().minusDays(1L));
    				});
    	endPeriodPicker.setOnAction(
    			(event) -> {
    				// If an end date is set, check if it is valid, and if it is after start date
    				// Correct if if needed
    				if (endPeriodPicker.getValue() == null)
    					endPeriodPicker.setValue(startPeriodPicker.getValue() == null ?
    							LocalDate.now().plusDays(1L) : startPeriodPicker.getValue().plusDays(1L));
    				else if (startPeriodPicker.getValue() != null && !endPeriodPicker.getValue().isAfter(startPeriodPicker.getValue()))
    					endPeriodPicker.setValue(startPeriodPicker.getValue().plusDays(1L));
    				});

    	// CLEARS THE feedback labels
    	showFeedBack(null, null);
    }
    
	@FXML
	/**
	 * Launched when the generate button has been pressed
	 */
    public void handleGenerateButton() {

    	// Loads the constraints
    	final DaoConstraints daoConstraints = new DaoConstraints(mainApp.getConstraintsCode().getValue());
    	final List<PositionConstraintBase> constraints = daoConstraints.load();

    	// Creates couples of physician // integer
    	final HashMap<Integer, Worker> workers = new HashMap<>();
    	int i = 0;
    	for (final Worker worker: mainApp.getPhysicians()) {
    		final Worker clonedWorker = worker.clone(); 
    		clonedWorker.setInternalIndice(i);
    		workers.put(i++, clonedWorker);
    	}
    	// Creates the definition of planning
    	final PlanningDefinition planningDefinition =
    			new PlanningDefinition(workers, mainApp.getPositions(), constraints);
    	
    	// Planning for the interval defined
    	final PlanningForInterval planningPositions =
    			planningDefinition.generatePlanningImplementation(new IntervalDate(startPeriodPicker.getValue(), endPeriodPicker.getValue()));
    	
    	// Solver task for this planning
    	task = new PlanningGenerationTask(planningPositions, this);

    	task.setOnFailed( (event) -> {
    		final Throwable exception  =
    				event.getSource().getException() == null ?
    						new Exception("Erreur inconnue") :
    							event.getSource().getException();
   			Alert alert = new Alert(AlertType.INFORMATION);
   			alert.setTitle("Information");
   			alert.setHeaderText("Pas de solution");
   			alert.setContentText(exception.getMessage());
   			alert.showAndWait();
			setButtonsStatus(true, false, false);
			this.solution = null;
		});

    	task.setOnSucceeded( (event) -> {
    		try {
				if (task.get() == null)
					setButtonsStatus(true, false, false);
				else {
					setButtonsStatus(true, false, true);
					this.solution = task.get();
				}
			} catch (Exception e) {
				final Alert alert = new Alert(AlertType.INFORMATION);
    			alert.setTitle("Information");
    			alert.setHeaderText("Erreur");
    			alert.setContentText(e.getMessage());
    			alert.showAndWait();
			}
    	});

    	// Starts the task
    	new Thread(task).start();

    	// adapts buttons visibility when generation is occuring
    	setButtonsStatus(false, true, false);
    }	

	/**
	 * Handles the pause button action
	 */
	@FXML
    public void handlePauseButton() {
    	// If task is null, it means the window has been closed
    	if (task != null)
    		task.stopProcessing("Stopped by user");
    }

	/**
	 * Handles the save button action
	 */
    public void handleSaveButton() {
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Save Planning");
    	
    	File saveFile = fileChooser.showSaveDialog(mainApp.getPrimaryStage());
    	if (saveFile != null) {
    		try {
    			saveConfiguration(saveFile);
    		} catch (final IOException e) {
    			final Alert alert = new Alert(AlertType.INFORMATION);
    			alert.setTitle("Information");
    			alert.setHeaderText("Erreur");
    			alert.setContentText(e.getMessage());
    			alert.showAndWait();
    		}
    	}
    }
    
    public void saveConfiguration(final File file) throws IOException {
		 Path saveFile = Paths.get(file.toURI());
		 try (final Writer writer = Files.newBufferedWriter(
				 saveFile, Charset.forName("UTF-8"),
				 StandardOpenOption.WRITE,
				 StandardOpenOption.CREATE,
				 StandardOpenOption.TRUNCATE_EXISTING)) {
			 final DaoSolution daoSolution = new DaoSolution(writer);
			 daoSolution.store(solution);
		 }
    }

    /**
     * Sets the feedback about the searching solution
     * @param nbTests
     * @param optimizeIndice
     */
    public void showFeedBack(final Integer nbTests, final Double optimizeIndice) {
    	nbTestsLabel.setText(nbTests == null ? "" : nbTests.toString());
    	optimizeIndexLabel.setText(optimizeIndice == null ? "" : optimizeIndice.toString());
    }

    /**
     * Sets the visibility of buttons
     * @param generateStatus
     * @param pauseStatus
     * @param saveStatus
     */
    private void setButtonsStatus(final boolean generateStatus, final boolean pauseStatus, final boolean saveStatus) {
		generateButton.setDisable(!generateStatus);
		pauseButton.setDisable(!pauseStatus);
		saveButton.setDisable(!saveStatus);
    }

    /**
     * Sets the javafx {@link Application}
     * @param mainApp
     */
    public void setMainApp(PlanningMainUIApp mainApp) {
        this.mainApp = mainApp;
    }

}
