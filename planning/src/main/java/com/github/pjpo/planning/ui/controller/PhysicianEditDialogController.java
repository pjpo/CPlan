package com.github.pjpo.planning.ui.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.controlsfx.dialog.Dialogs;

import com.github.pjpo.planning.physician.Physician;
import com.github.pjpo.planning.ui.controller.utils.DateEditingCell;
import com.github.pjpo.planning.ui.controller.utils.DefaultDatePickerConverter;
import com.github.pjpo.planning.utils.Interval;

public class PhysicianEditDialogController {

	@FXML
	private TextField nameField;
	
	@FXML
	private TextField timePartField;
	
	@FXML
	private DatePicker startWorkPicker;
	
	@FXML
	private DatePicker endWorkPicker;
	
	@FXML
    private TableView<Interval> paidVacationsTable;
    @FXML
    private TableColumn<Interval, LocalDate> paidVacationsStartColumn;
    @FXML
    private TableColumn<Interval, LocalDate> paidVacationEndColumn;
	
	private ObservableList<Interval> paidVacationsList = FXCollections.observableArrayList();
    
	private DateTimeFormatter dateFormatter;
	
	private Stage dialogStage;
    
	private Physician physician;

    private boolean okClicked = false;
    
    @FXML
    private void initialize() {
    	Callback<TableColumn<Interval, LocalDate>, 
           TableCell<Interval, LocalDate>> cellFactory
               = (TableColumn<Interval, LocalDate> p) -> new DateEditingCell(new DefaultDatePickerConverter(dateFormatter, null, null));

        paidVacationsStartColumn.setCellFactory(cellFactory);
    	paidVacationsStartColumn.setOnEditCommit( (event) ->
                ((Interval) event.getTableView().getItems().get(
                		event.getTablePosition().getRow())
                        ).setStart(event.getNewValue()));
    	paidVacationEndColumn.setCellFactory(cellFactory);
    	paidVacationEndColumn.setOnEditCommit( (event) ->
                ((Interval) event.getTableView().getItems().get(
                		event.getTablePosition().getRow())
                        ).setEnd(event.getNewValue()));
    	
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public void setPhysician(Physician physician) {
        this.physician = physician;

        nameField.setText(physician.getName() == null ? "" : physician.getName());
        timePartField.setText(physician.getTimePart() == null ? "" : physician.getTimePart().toString());
        startWorkPicker.setValue(physician.getWorkStart());
        endWorkPicker.setValue(physician.getWorkEnd());
        
        // LINKS OBSERVABLE WITH PHYSICIAN
        paidVacationsList.setAll(physician.getPaidVacation());
        // LINKS TABLE WITH OBSERVABLE
        paidVacationsTable.setItems(paidVacationsList);
        // LINKS THE COLUMNS
        paidVacationsStartColumn.setCellValueFactory(new PropertyValueFactory<Interval, LocalDate>("start"));
        paidVacationEndColumn.setCellValueFactory(new PropertyValueFactory<Interval, LocalDate>("end"));
    
    }

    @FXML
    private void handleNewPaidVacation() {
    	Interval paidVacation = new Interval(null, null);
    	paidVacationsList.add(paidVacation);
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
            physician.setWorkStart(startWorkPicker.getValue());
            physician.setWorkEnd(endWorkPicker.getValue());
            physician.setPaidVacation(new ArrayList<>(paidVacationsTable.getItems()));

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
        
        if (startWorkPicker.getValue() != null && endWorkPicker.getValue() != null) {
        	// CHECK IF END OF WORK IS GREATER THAN START OF WORK
        	if (!endWorkPicker.getValue().isAfter(startWorkPicker.getValue())) {
        		errorMessageBuilder.append("Fin de période de travail doit être supérieur à début de période de travail!\n");
        	}
        }
        
        if (errorMessageBuilder.length() == 0) {
        	return true;
        } else {
        	Dialogs.create()
        	.owner(dialogStage)
        	.title("Information Dialog")
            .masthead("Invalid fields")
            .message(errorMessageBuilder.toString())
            .showError();
        	return false;
        }
    }
    
}
