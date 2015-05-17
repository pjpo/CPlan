package com.github.pjpo.planning.ui.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.Callback;

import com.github.pjpo.planning.physician.Physician;
import com.github.pjpo.planning.ui.controller.utils.DateEditingCell;
import com.github.pjpo.planning.ui.controller.utils.DefaultDatePickerConverter;
import com.github.pjpo.planning.ui.controller.utils.IntervalDateEditingCell;
import com.github.pjpo.planning.ui.controller.utils.IntervalDateEditingCell.IntervalPosition;
import com.github.pjpo.planning.ui.model.Poste;
import com.github.pjpo.planning.utils.BoundedLocalDate;
import com.github.pjpo.planning.utils.IntervalDate;

public class PhysicianEditDialogController {

	@FXML
	private TextField nameField;
	
	@FXML
	private TextField timePartField;
	
	@FXML
	private TextField refusedPostes;

	@FXML
    private TableView<IntervalDate> paidVacationsTable;
    @FXML
    private TableColumn<IntervalDate, BoundedLocalDate> paidVacationsStartColumn;
    @FXML
    private TableColumn<IntervalDate, BoundedLocalDate> paidVacationEndColumn;
	private ObservableList<IntervalDate> paidVacationsList = FXCollections.observableArrayList();
    
	@FXML
    private TableView<IntervalDate> unpaidVacationsTable;
    @FXML
    private TableColumn<IntervalDate, BoundedLocalDate> unpaidVacationsStartColumn;
    @FXML
    private TableColumn<IntervalDate, BoundedLocalDate> unpaidVacationEndColumn;
	private ObservableList<IntervalDate> unpaidVacationsList = FXCollections.observableArrayList();
	
	@FXML
    private TableView<Poste> neededVacTable;
    @FXML
    private TableColumn<Poste, LocalDate> neededVacDateColumn;
    @FXML
    private TableColumn<Poste, String> neededVacPosteColumn;
	private ObservableList<Poste> neededVacList = FXCollections.observableArrayList();

	private DateTimeFormatter dateFormatter;
	
	private Stage dialogStage;
    
	private Physician physician;

    private boolean okClicked = false;
    
    @FXML
    private void initialize() {
    	
    	Callback<TableColumn<IntervalDate, BoundedLocalDate>, 
    		TableCell<IntervalDate, BoundedLocalDate>> startIntervalCellFactory
    		= (TableColumn<IntervalDate, BoundedLocalDate> p) -> new IntervalDateEditingCell(dateFormatter, IntervalPosition.START);

        	Callback<TableColumn<IntervalDate, BoundedLocalDate>, 
    		TableCell<IntervalDate, BoundedLocalDate>> endIntervalCellFactory
    		= (TableColumn<IntervalDate, BoundedLocalDate> p) -> new IntervalDateEditingCell(dateFormatter, IntervalPosition.END);
    		
    	Callback<TableColumn<Poste, LocalDate>, 
    		TableCell<Poste, LocalDate>> posteCellFactory
    		= (TableColumn<Poste, LocalDate> p) -> new DateEditingCell<Poste>(new DefaultDatePickerConverter(dateFormatter, null, null));

   		EventHandler<TableColumn.CellEditEvent<IntervalDate,BoundedLocalDate>> startEventHandler = (event) -> {
   			try {
   				event.getRowValue().setStart(event.getNewValue().getDate());
   			} catch (IllegalArgumentException e) {
   				// RESHOW OLD VALUE
   				refreshTable(event.getTableView());
   			}
   		};
   		EventHandler<TableColumn.CellEditEvent<IntervalDate,BoundedLocalDate>> endEventHandler = (event) -> {
   			try {
   				event.getRowValue().setEnd(event.getNewValue().getDate());
   			} catch (IllegalArgumentException e) {
   				// RESHOW OLD VALUE
   				refreshTable(event.getTableView());
   			}
   		};
    		
        paidVacationsStartColumn.setCellFactory(startIntervalCellFactory);
    	paidVacationsStartColumn.setOnEditCommit(startEventHandler);
    	paidVacationEndColumn.setCellFactory(endIntervalCellFactory);
    	paidVacationEndColumn.setOnEditCommit(endEventHandler);
    	
        unpaidVacationsStartColumn.setCellFactory(startIntervalCellFactory);
    	unpaidVacationsStartColumn.setOnEditCommit(startEventHandler);
    	unpaidVacationEndColumn.setCellFactory(endIntervalCellFactory);
    	unpaidVacationEndColumn.setOnEditCommit(endEventHandler);

        neededVacDateColumn.setCellFactory(posteCellFactory);
        neededVacDateColumn.setOnEditCommit( (event) ->
        		((Poste) event.getTableView().getItems().get(
        				event.getTablePosition().getRow())
        				).setDate(event.getNewValue()));
        
        neededVacPosteColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        neededVacPosteColumn.setOnEditCommit( (event) ->
        		((Poste) event.getTableView().getItems().get(
        				event.getTablePosition().getRow())
                        ).setPoste(event.getNewValue()));
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public void setPhysician(Physician physician) {
        this.physician = physician;

        nameField.setText(physician.getName() == null ? "" : physician.getName());
        timePartField.setText(physician.getTimePart() == null ? "" : physician.getTimePart().toString());
        
        // == PAID VACATIONS ==
        // LINKS OBSERVABLE WITH PHYSICIAN
        paidVacationsList.setAll(physician.getPaidVacation());
        // LINKS TABLE WITH OBSERVABLE
        paidVacationsTable.setItems(paidVacationsList);
        // LINKS THE COLUMNS
        paidVacationsStartColumn.setCellValueFactory(new PropertyValueFactory<IntervalDate, BoundedLocalDate>("boundedStart"));
        paidVacationEndColumn.setCellValueFactory(new PropertyValueFactory<IntervalDate, BoundedLocalDate>("boundedEnd"));
    
        // == UNPAID VACATIONS ==
        // LINKS OBSERVABLE WITH PHYSICIAN
        unpaidVacationsList.setAll(physician.getUnpaidVacation());
        // LINKS TABLE WITH OBSERVABLE
        unpaidVacationsTable.setItems(unpaidVacationsList);
        // LINKS THE COLUMNS
        unpaidVacationsStartColumn.setCellValueFactory(new PropertyValueFactory<IntervalDate, BoundedLocalDate>("boundedStart"));
        unpaidVacationEndColumn.setCellValueFactory(new PropertyValueFactory<IntervalDate, BoundedLocalDate>("boundedEnd"));

        // == NEEDED VACS LIST ==
        // FILLS THE OBSERVABLE ASSOCIATED LIST
        neededVacList.clear();
        ArrayList<LocalDate> neededVacDates = new ArrayList<>(physician.getWorkedVacs().size());
        for (LocalDate date : physician.getWorkedVacs().keySet()) {
        	neededVacDates.add(date);
        }
        Collections.sort(neededVacDates);
        for (LocalDate date : neededVacDates) {
        	for (String posteName : physician.getWorkedVacs().get(date)) {
        		Poste poste = new Poste();
        		poste.setDate(date);
        		poste.setPoste(posteName);
        		neededVacList.add(poste);
        	}
        }
        // LINKS TABLE WITH OBSERVABLE
        neededVacTable.setItems(neededVacList);
        // LINKS THE COLUMNS
        neededVacDateColumn.setCellValueFactory(new PropertyValueFactory<Poste, LocalDate>("date"));
        neededVacPosteColumn.setCellValueFactory(new PropertyValueFactory<Poste, String>("poste"));
        
        // == REFUSED POSTES ==
        StringBuilder refusedPostesBuilder = new StringBuilder();
        for (String poste : physician.getRefusedPostes()) {
        	refusedPostesBuilder.append(poste).append(';');
        }
        if (refusedPostesBuilder.length() != 0)
        	refusedPostesBuilder.deleteCharAt(refusedPostesBuilder.length() - 1);
        refusedPostes.setText(refusedPostesBuilder.toString());
    }

    @FXML
    private void handleNewPaidVacation() {
    	IntervalDate paidVacation = new IntervalDate();
    	paidVacationsList.add(paidVacation);
    }

    @FXML
    private void handleDeletePaidVacation() {
        int selectedIndex = paidVacationsTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
        	paidVacationsTable.getItems().remove(selectedIndex);
        } else {
        	// NOTHING SELECTED
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Information");
			alert.setHeaderText("Pas de période sélectionnée");
			alert.setContentText("Merci de sélectionner une période");
			alert.showAndWait();
        }
    }

    @FXML
    private void handleNewUnpaidVacation() {
    	IntervalDate unpaidVacation = new IntervalDate();
    	unpaidVacationsList.add(unpaidVacation);
    }

    @FXML
    private void handleDeleteUnpaidVacation() {
        int selectedIndex = unpaidVacationsTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
        	unpaidVacationsTable.getItems().remove(selectedIndex);
        } else {
      	  // NOTHING SELECTED
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Information");
			alert.setHeaderText("Pas de période sélectionnée");
			alert.setContentText("Merci de sélectionner une période");
			alert.showAndWait();
        }
    }

    @FXML
    private void handleNewNeededVacation() {
    	Poste poste = new Poste();
    	neededVacList.add(poste);
    }

    @FXML
    private void handleDeleteNeededVacation() {
        int selectedIndex = neededVacTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
        	neededVacTable.getItems().remove(selectedIndex);
        } else {
        	// NOTHING SELECTED
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Information");
			alert.setHeaderText("Pas d'élément sélectionné");
			alert.setContentText("Merci de sélectionner un élément");
			alert.showAndWait();
        }
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
            physician.setPaidVacation(new ArrayList<>(paidVacationsTable.getItems()));
            physician.setUnpaidVacation(new ArrayList<>(unpaidVacationsTable.getItems()));
            HashMap<LocalDate, ArrayList<String>> workedVacs = new HashMap<>();
            for (Poste poste : neededVacList) {
            	ArrayList<String> dateWorkedVacs = null;
            	if ((dateWorkedVacs = workedVacs.get(poste.getDate())) == null) {
            		dateWorkedVacs = new ArrayList<>();
            		workedVacs.put(poste.getDate(), dateWorkedVacs);
            	}
            	dateWorkedVacs.add(poste.getPoste());
            }
            physician.setWorkedVacs(workedVacs);
            ArrayList<String> postes = new ArrayList<>();
            if (refusedPostes.getText() != null && refusedPostes.getText().length() != 0) {
	            for (String poste : refusedPostes.getText().split(";")) {
	            	postes.add(poste);
	            }
            }
            physician.setRefusedPostes(postes);
            
            okClicked = true;
            dialogStage.close();
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private <T> void refreshTable(TableView<T> tableView) {
    	int selectedIndex = tableView.getSelectionModel().getSelectedIndex();
    	ObservableList<T> observableList = tableView.getItems();
    	tableView.setItems(null);
    	tableView.layout();
    	tableView.setItems(observableList);
    	tableView.getSelectionModel().select(selectedIndex);
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
        
        if (errorMessageBuilder.length() == 0) {
        	return true;
        } else {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Information");
			alert.setHeaderText("Invalid fields");
			alert.setContentText(errorMessageBuilder.toString());
			alert.showAndWait();
        	return false;
        }
    }

}
