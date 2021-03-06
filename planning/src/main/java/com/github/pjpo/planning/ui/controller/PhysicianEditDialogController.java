package com.github.pjpo.planning.ui.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;

import com.github.pjpo.planning.model.Worker;
import com.github.pjpo.planning.ui.model.Poste;
import com.github.pjpo.planning.utils.IntervalDateTime;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class PhysicianEditDialogController {

	@FXML
	private TextField nameField;
	
	@FXML
	private TextField timePartField;
	
	@FXML
	private TextField refusedPostes;

	@FXML
    private TableView<IntervalDateTime> paidVacationsTable;
    @FXML
    private TableColumn<IntervalDateTime, String> paidVacationsStartColumn;
    @FXML
    private TableColumn<IntervalDateTime, String> paidVacationEndColumn;
    
	private ObservableList<IntervalDateTime> paidVacationsList = FXCollections.observableArrayList();
    
	@FXML
    private TableView<IntervalDateTime> unpaidVacationsTable;
    @FXML
    private TableColumn<IntervalDateTime, String> unpaidVacationsStartColumn;
    @FXML
    private TableColumn<IntervalDateTime, String> unpaidVacationEndColumn;
    
	private ObservableList<IntervalDateTime> unpaidVacationsList = FXCollections.observableArrayList();
	
	@FXML
    private TableView<Poste> neededVacTable;
    @FXML
    private TableColumn<Poste, LocalDate> neededVacDateColumn;
    @FXML
    private TableColumn<Poste, String> neededVacPosteColumn;
    
	private ObservableList<Poste> neededVacList = FXCollections.observableArrayList();
	
	private Stage dialogStage;
    
	private Worker physician;

    private boolean okClicked = false;
    
    private final DateTimeFormatter sdf = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
    
    private class StartCellFormat implements Callback<CellDataFeatures<IntervalDateTime, String>, ObservableValue<String>> {

		@Override
		public ObservableValue<String> call(
				final CellDataFeatures<IntervalDateTime, String> param) {
			final SimpleStringProperty property = new SimpleStringProperty();
			if (param.getValue().getStart() != null)
				property.setValue(sdf.format(param.getValue().getStart()));
			else
				property.setValue("Non déterminé");
			return property;
		}
    	
    }
    
    private class EndCellFormat implements Callback<CellDataFeatures<IntervalDateTime, String>, ObservableValue<String>> {

		@Override
		public ObservableValue<String> call(
				final CellDataFeatures<IntervalDateTime, String> param) {
			final SimpleStringProperty property = new SimpleStringProperty();
			if (param.getValue().getEnd() != null)
				property.setValue(sdf.format(param.getValue().getEnd()));
			else
				property.setValue("Non déterminé");
			return property;
		}
    	
    }
    
    @FXML
    private void initialize() {
    	// Initialize the paid vacations
    	paidVacationsStartColumn.setCellValueFactory(new StartCellFormat());
        paidVacationEndColumn.setCellValueFactory(new EndCellFormat());

    	// Initialize the non paid vacations
    	unpaidVacationsStartColumn.setCellValueFactory(new StartCellFormat());
        unpaidVacationEndColumn.setCellValueFactory(new EndCellFormat());

        // Initialize the needed positions
        neededVacPosteColumn.setCellValueFactory(new PropertyValueFactory<Poste, String>("poste"));
        neededVacDateColumn.setCellValueFactory(new PropertyValueFactory<Poste, LocalDate>("date"));
    }

    public void setDialogStage(final Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public void setPhysician(final Worker physician) {
        this.physician = physician;

        nameField.setText(physician.getName() == null ? "" : physician.getName());
        timePartField.setText(physician.getTimePart() == null ? "" : physician.getTimePart().toString());
        
        // == PAID VACATIONS ==
        // LINKS OBSERVABLE WITH PHYSICIAN
        paidVacationsList.setAll(physician.getPaidVacations());
        // LINKS TABLE WITH OBSERVABLE
        paidVacationsTable.setItems(paidVacationsList);
    
        // == UNPAID VACATIONS ==
        // LINKS OBSERVABLE WITH PHYSICIAN
        unpaidVacationsList.setAll(physician.getUnpaidVacations());
        // LINKS TABLE WITH OBSERVABLE
        unpaidVacationsTable.setItems(unpaidVacationsList);

        // == NEEDED VACS LIST ==
        // FILLS THE OBSERVABLE ASSOCIATED LIST
        neededVacList.clear();
        ArrayList<LocalDate> neededVacDates = new ArrayList<>(physician.getWorkedPositions().size());
        for (LocalDate date : physician.getWorkedPositions().keySet()) {
        	neededVacDates.add(date);
        }
        Collections.sort(neededVacDates);
        for (LocalDate date : neededVacDates) {
        	for (String posteName : physician.getWorkedPositions().get(date)) {
        		Poste poste = new Poste();
        		poste.setDate(date);
        		poste.setPoste(posteName);
        		neededVacList.add(poste);
        	}
        }
        // LINKS TABLE WITH OBSERVABLE
        neededVacTable.setItems(neededVacList);
        
        // == REFUSED POSTES ==
        StringBuilder refusedPostesBuilder = new StringBuilder();
        for (String poste : physician.getRefusedPositions()) {
        	refusedPostesBuilder.append(poste).append(';');
        }
        if (refusedPostesBuilder.length() != 0)
        	refusedPostesBuilder.deleteCharAt(refusedPostesBuilder.length() - 1);
        refusedPostes.setText(refusedPostesBuilder.toString());
    }

    @FXML
    private void handleNewPaidVacation() {
    	IntervalDateTime tempPaidVacation = new IntervalDateTime();
    	paidVacationsList.add(tempPaidVacation);
    }

    @FXML
    private void handleModifyPaidVacation() {
    	// GETS THE SELECTED ITEM
        int selectedIndex = paidVacationsTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
        	IntervalDateTime toModify = paidVacationsTable.getItems().get(selectedIndex);
        	DateTimeIntervalEditDialogController controller =
        			DateTimeIntervalEditDialogController.showDialog(dialogStage, "Congés payés", toModify);
        	if (controller.isOkClicked()) {
        		paidVacationsTable.getItems().set(selectedIndex, controller.getInterval());
        	}
        } else {
        	// NOTHING SELECTED
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Information");
			alert.setHeaderText("Pas de période sélectionnée");
			alert.setContentText("Merci de sélectionner une période à modifier");
			alert.showAndWait();
        }
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
			alert.setContentText("Merci de sélectionner une période à  supprimer");
			alert.showAndWait();
        }
    }

    @FXML
    private void handleNewUnpaidVacation() {
    	IntervalDateTime tempUnPaidVacation = new IntervalDateTime();
    	unpaidVacationsList.add(tempUnPaidVacation);
    }

    @FXML
    private void handleModifyUnpaidVacation() {
    	// GETS THE SELECTED ITEM
        int selectedIndex = unpaidVacationsTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
        	IntervalDateTime toModify = unpaidVacationsTable.getItems().get(selectedIndex);
        	DateTimeIntervalEditDialogController controller =
        			DateTimeIntervalEditDialogController.showDialog(dialogStage, "Congés non payés", toModify);
        	if (controller.isOkClicked()) {
        		unpaidVacationsTable.getItems().set(selectedIndex, controller.getInterval());
        	}
        } else {
        	// NOTHING SELECTED
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Information");
			alert.setHeaderText("Pas de période sélectionnée");
			alert.setContentText("Merci de sélectionner une période à modifier");
			alert.showAndWait();
        }
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
			alert.setContentText("Merci de sélectionner une période à supprimer");
			alert.showAndWait();
        }
    }

    @FXML
    private void handleNewNeededVacation() {
    	Poste poste = new Poste();
    	neededVacList.add(poste);
    }

    @FXML
    private void handleModifyNeededVacation() {
    	// GETS THE SELECTED ITEM
        int selectedIndex = neededVacTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
        	Poste toModify = neededVacTable.getItems().get(selectedIndex);
        	NeededVacationDialog controller =
        			NeededVacationDialog.showDialog(dialogStage, "Congés non payés", toModify);
        	if (controller.isOkClicked()) {
        		neededVacTable.getItems().set(selectedIndex, controller.getPoste());
        	}
        } else {
        	// NOTHING SELECTED
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Information");
			alert.setHeaderText("Pas de poste sélcetionné");
			alert.setContentText("Merci de sélectionner un poste");
			alert.showAndWait();
        }
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

	public boolean isOkClicked() {
        return okClicked;
    }
    
    @FXML
    private void handleOk() {
        if (isInputValid()) {
            physician.setName(nameField.getText());
            physician.setTimePart(Integer.decode(timePartField.getText()));
            physician.setPaidVacations(new ArrayList<>(paidVacationsTable.getItems()));
            physician.setUnpaidVacations(new ArrayList<>(unpaidVacationsTable.getItems()));
            final Multimap<LocalDate, String> workedVacs = HashMultimap.create();
            for (Poste poste : neededVacList) {
            	workedVacs.put(poste.getDate(), poste.getPoste());
            }
            physician.setWorkedVacs(workedVacs);
            final ArrayList<String> postes = new ArrayList<>();
            if (refusedPostes.getText() != null && refusedPostes.getText().length() != 0) {
	            for (final String poste : refusedPostes.getText().split(";")) {
	            	postes.add(poste);
	            }
            }
            physician.setRefusedPositions(postes);

            okClicked = true;
            dialogStage.close();
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
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
