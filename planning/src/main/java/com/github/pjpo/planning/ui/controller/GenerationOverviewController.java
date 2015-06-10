package com.github.pjpo.planning.ui.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import au.com.bytecode.opencsv.CSVWriter;

import com.github.pjpo.planning.model.Worker;
import com.github.pjpo.planning.model.Position;
import com.github.pjpo.planning.model.PositionConstraintBase;
import com.github.pjpo.planning.model.dao.DaoConstraints;
import com.github.pjpo.planning.problem.PlanningDefinition;
import com.github.pjpo.planning.problem.PlanningForInterval;
import com.github.pjpo.planning.problem.Solution;
import com.github.pjpo.planning.problem.SolutionException;
import com.github.pjpo.planning.ui.PlanningMainUIApp;
import com.github.pjpo.planning.ui.controller.utils.DefaultDatePickerConverter;
import com.github.pjpo.planning.ui.controller.utils.PlanningGenerationTask;
import com.github.pjpo.planning.utils.IntervalDate;
import com.google.common.collect.HashMultimap;

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

	/** Date formatter */
	@SuppressWarnings("unused")
	private DateTimeFormatter dateFormatter;
	
	private Solution solution = null;

	private PlanningGenerationTask task = null; 
	
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
    	showFeedBack(null, null);

    }
    
    public void handleGenerateButton() {

    	// Loads the constraints
    	try (final InputStream is = PlanningMainUIApp.class.getResourceAsStream("/com/github/pjpo/planning/conditions.cfg")) {
    		final DaoConstraints daoConstraints = new DaoConstraints(is);
    		final List<PositionConstraintBase> constraints = daoConstraints.load();

        	// Creates couples of physician // integer
    		final HashMap<Integer, Worker> workers = new HashMap<>();
    		int i = 0;
    		for (final Worker physician: mainApp.getPhysicians()) {
    			final Worker clonedPhysician = new Worker();
    			clonedPhysician.setInternalIndice(i);
    			clonedPhysician.setName(physician.getName());
    			clonedPhysician.setPaidVacations(new ArrayList<>(physician.getPaidVacations()));
    			clonedPhysician.setRefusedPositions(new ArrayList<String>(physician.getRefusedPositions()));
    			clonedPhysician.setTimePart(physician.getTimePart());
    			clonedPhysician.setUnpaidVacations(new ArrayList<>(physician.getUnpaidVacations()));
    			clonedPhysician.setWorkedVacs(HashMultimap.create(physician.getWorkedPositions()));
    			workers.put(i, clonedPhysician);
    			i++;
    		}
    		// Creates the definition of planning
    		final PlanningDefinition planningConstraints =
    				new PlanningDefinition(
        					workers,
        					mainApp.getPositions(),
        					constraints);

        	// Planning for the interval defined
        	final PlanningForInterval planningImplementation =
        			planningConstraints.generatePlanningImplementation(
        					new IntervalDate(startPeriodPicker.getValue(), endPeriodPicker.getValue()));
        	
        	// Solver for this planning
        	
        	task = new PlanningGenerationTask(planningImplementation, this);

        	task.setOnFailed( (event) -> {
        		Throwable exception  =
        				event.getSource().getException() == null ?
        						new Exception("Erreur inconnue") :
        							event.getSource().getException();
        		if (exception instanceof SolutionException) {
        			Alert alert = new Alert(AlertType.INFORMATION);
        			alert.setTitle("Information");
        			alert.setHeaderText("Pas de solution");
        			alert.setContentText(exception.getMessage());
        			alert.showAndWait();
        		} else {
        			Alert alert = new Alert(AlertType.INFORMATION);
        			alert.setTitle("Information");
        			alert.setHeaderText("Erreur");
        			alert.setContentText(exception.getMessage());
        			alert.showAndWait();
        		}
    			setButtonsStatus(true, false, false);
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
        			Alert alert = new Alert(AlertType.INFORMATION);
        			alert.setTitle("Information");
        			alert.setHeaderText("Erreur");
        			alert.setContentText(e.getMessage());
        			alert.showAndWait();
    			}
        	});
        	
        	new Thread(task).start();

        	// ADAPTS BUTTONS VISIBILITY
        	setButtonsStatus(false, true, false);
        	
    	} catch (final IOException e) {
			e.printStackTrace();
		}

    }	
    
    public void handlePauseButton() {
    	// If task is null, it means the window has been closed
    	if (task != null)
    		task.stopProcessing("Stopped by user");
    }
    
    public void handleSaveButton() {
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Save Planning");
    	
    	File saveFile = fileChooser.showSaveDialog(mainApp.getPrimaryStage());
    	if (saveFile != null) {
    		try {
    			saveConfiguration(saveFile);
    		} catch (IOException e) {
    			Alert alert = new Alert(AlertType.INFORMATION);
    			alert.setTitle("Information");
    			alert.setHeaderText("Erreur");
    			alert.setContentText(e.getMessage());
    			alert.showAndWait();
    		}
    	}
    }
    
    public void saveConfiguration(File file) throws IOException {
		 Path saveFile = Paths.get(file.toURI());
		 try (CSVWriter csvWriter = new CSVWriter(Files.newBufferedWriter(
				 saveFile, Charset.forName("UTF-8"),
				 StandardOpenOption.WRITE,
				 StandardOpenOption.CREATE,
				 StandardOpenOption.TRUNCATE_EXISTING))) {
			 
			 // List of positions names and dates
			 final List<String> positionsNames = new ArrayList<String>(solution.getPositions().columnKeySet());
			 final List<LocalDate> positionDates = new ArrayList<LocalDate>(solution.getPositions().rowKeySet());
			 Collections.sort(positionsNames);
			 Collections.sort(positionDates);

			 // WRITE HEADERS
			final ArrayList<String> header = new ArrayList<>(positionsNames.size() + 1);
			header.add("");
			header.addAll(positionsNames);
			csvWriter.writeNext(header.stream().toArray(size-> new String[size]));
			
			// WRITE CONTENTS
			for (final LocalDate localDate : positionDates) {
				final ArrayList<String> content = new ArrayList<>(positionsNames.size() + 1);
				content.add(localDate.toString());
				for (final String positionName : positionsNames) {
					final Position position = solution.getPositions().get(localDate, positionName);
					content.add(position == null ? "" : position.getWorker().getName());
				}
				csvWriter.writeNext(content.toArray(new String[content.size()]));
			 }
		 }
	 }

	 public void showFeedBack(Integer nbTests, Double optimizeIndice) {
		nbTestsLabel.setText(nbTests == null ? "" : nbTests.toString());
		optimizeIndexLabel.setText(optimizeIndice == null ? "" : optimizeIndice.toString());
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


}
