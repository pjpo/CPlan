package com.github.pjpo.planning.ui.controller;

import java.io.File;
import java.io.IOException;
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;

import org.controlsfx.dialog.Dialogs;

import au.com.bytecode.opencsv.CSVWriter;

import com.github.pjpo.planning.Planning;
import com.github.pjpo.planning.Solution;
import com.github.pjpo.planning.SolutionException;
import com.github.pjpo.planning.jours.JourChuMtp;
import com.github.pjpo.planning.ui.PlanningMainUIApp;
import com.github.pjpo.planning.ui.controller.utils.DefaultDatePickerConverter;
import com.github.pjpo.planning.utils.IntervalDate;
import com.github.pjpo.planning.utils.IntervalDateTime;

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
	@SuppressWarnings("unused")
	private DateTimeFormatter dateFormatter;
	
	private Solution solution = null;

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
    	Planning planning = new Planning(new IntervalDate(
    			startPeriodPicker.getValue(),
    			endPeriodPicker.getValue()),
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
    		try {
				if (task.get() == null || task.get().size() == 0)
					setButtonsStatus(true, false, false);
				else {
					setButtonsStatus(true, false, true);
					solution = task.get().getLast();
				}
			} catch (Exception e) {
    			Dialogs.create()
    			.owner(mainApp.getPrimaryStage())
    			.masthead("Erreur")
    			.showException(e);
			}
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
    
	 public void handleSaveButton() {
		 FileChooser fileChooser = new FileChooser();
		 fileChooser.setTitle("Save Planning");
		 
		 File saveFile = fileChooser.showSaveDialog(mainApp.getPrimaryStage());
		 if (saveFile != null) {
			 try {
				 saveConfiguration(saveFile);
			 } catch (IOException e) {
				 Dialogs.create()
				 .owner(mainApp.getPrimaryStage())
				 .showException(e);
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
			 
			 // SEARCHES THE LIST OF POSTES AND LIST OF DATES
			 HashSet<String> postes = new HashSet<>();
			 ArrayList<LocalDate> dates = new ArrayList<>();
			 for (Entry<LocalDate, HashMap<String, IntervalDateTime>> entry : solution.getWorkingPeriodsMap().entrySet()) {
				 dates.add(entry.getKey());
				 for (String poste : entry.getValue().keySet()) {
					 postes.add(poste);
				 }
			 }
			 // SORTS LIST OF POSTES AND DATES
			 ArrayList<String> postesArray = new ArrayList<>(postes);
			 Collections.sort(postesArray);
			 Collections.sort(dates);
			 
			 // WRITE HEADERS
			ArrayList<String> header = new ArrayList<>(postesArray.size() + 1);
			header.add("");
			header.addAll(postesArray);
			csvWriter.writeNext(header.toArray(new String[header.size()]));
			
			// WRITE CONTENTS
			for (LocalDate localDate : dates) {
				ArrayList<String> content = new ArrayList<>(postesArray.size() + 1);
				content.add(localDate.toString());
				HashMap<String, Integer> physiciansNb = solution.getSolutionMedIndicesMap().get(localDate);
				for (String poste : postesArray) {
					content.add(physiciansNb.containsKey(poste) ? solution.getPhysicians().get(physiciansNb.get(poste)).getName() : "");
				}
				csvWriter.writeNext(content.toArray(new String[content.size()]));
			 }
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


}
