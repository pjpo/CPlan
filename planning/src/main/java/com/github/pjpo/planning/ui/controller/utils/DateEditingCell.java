package com.github.pjpo.planning.ui.controller.utils;

import java.time.LocalDate;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.util.StringConverter;

import com.github.pjpo.planning.utils.Interval;

public class DateEditingCell extends TableCell<Interval, LocalDate> {
	 
    private DatePicker datePicker = null;
   
    private StringConverter<LocalDate> stringConverter;
    
    public DateEditingCell(StringConverter<LocalDate> stringConverter) {
    	this.stringConverter = stringConverter;
        createDatePicker();
    }
   
    @Override
    public void startEdit() {
        if (!isEmpty()) {
            super.startEdit();
            setText(null);
            setGraphic(datePicker);
        }
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }
   
    @Override
    public void cancelEdit() {
        super.cancelEdit();
       
        setText(toString());
        setGraphic(null);
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    @Override
    public void updateItem(LocalDate item, boolean empty) {
        super.updateItem(item, empty);
       
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (datePicker != null) {
                	datePicker.setValue(getItem() == null ? LocalDate.now() : getItem());
                }
                setText(null);
                setGraphic(datePicker);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            } else {
            	setText(toString());
            	setContentDisplay(ContentDisplay.TEXT_ONLY);
            }
        }
    }
    
    public String toString() {
    	String converted = stringConverter.toString(getItem()); 
    	return converted == null ? "Indéfini" : converted;
    }

    private void createDatePicker() {
        datePicker = new DatePicker(getItem());
        datePicker.setMinWidth(this.getWidth() - this.getGraphicTextGap()*2);
        datePicker.focusedProperty().addListener(
        		(observable, oldValue, newValue) -> {
				if (!newValue) commitEdit(datePicker.getValue());
		});
    }
   
}

