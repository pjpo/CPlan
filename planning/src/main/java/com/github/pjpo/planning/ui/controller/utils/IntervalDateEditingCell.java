package com.github.pjpo.planning.ui.controller.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.util.Callback;

import com.github.pjpo.planning.utils.BoundedLocalDate;
import com.github.pjpo.planning.utils.IntervalDate;

public class IntervalDateEditingCell extends TableCell<IntervalDate, BoundedLocalDate> {
	 
	public enum IntervalPosition {
		START, END;
	}
	
	private IntervalPosition position;
	
    private DatePicker datePicker = null;
   
    private DateTimeFormatter dateFormatter;
    
    public IntervalDateEditingCell(DateTimeFormatter dateFormatter, IntervalPosition position) {
    	this.dateFormatter = dateFormatter;
    	this.position = position;
        createDatePicker();
    }
   
    @Override
    public void startEdit() {
        if (!isEmpty()) {
            super.startEdit();
            setText(null);
            datePicker.setValue(getItem().getDate());
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
    public void updateItem(BoundedLocalDate item, boolean empty) {
        super.updateItem(item, empty);
       
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (datePicker != null) {
                	datePicker.setValue(getItem() == null ? LocalDate.now() : getItem().getDate());
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
    	String converted = getItem() == null || getItem().getDate() == null ? null : dateFormatter.format(getItem().getDate());
    	return converted == null ? "Indéfini" : converted;
    }

    private void createDatePicker() {
    	final Callback<DatePicker, DateCell> dayCellFactory = 
                (datePicker) -> {return new MyDateCell(this, position);};

        datePicker = new DatePicker();
        datePicker.setDayCellFactory(dayCellFactory);
        datePicker.setMinWidth(this.getWidth() - this.getGraphicTextGap()*2);
        datePicker.focusedProperty().addListener(
        		(observable, oldValue, newValue) -> {
        			System.out.println("oldvalue : " + oldValue);
        			System.out.println("newvalue : " + newValue);
				if (!newValue) {
					System.out.println("Commit : " + datePicker.getValue());
					commitEdit(new BoundedLocalDate(null, datePicker.getValue()));
				}
		});
    }
   
    public class MyDateCell extends DateCell {
    	
    	private IntervalPosition position;
    	
    	private IntervalDateEditingCell idec;
    	
    	public MyDateCell(IntervalDateEditingCell idec, IntervalPosition position) {
    		this.position = position;
    		this.idec = idec;
    	}
    	
        @Override
        public void updateItem(LocalDate item, boolean empty) {
            super.updateItem(item, empty);

            if (position == IntervalPosition.START && idec.getItem().getBound() != null) {
            	if (item.isAfter(idec.getItem().getBound())) {
            		setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;");
            	}
            } else if (position == IntervalPosition.END && idec.getItem().getBound() != null) {
            	if (item.isBefore(idec.getItem().getBound())) {
            		setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;");
            	}
            }
        }
    }
}

