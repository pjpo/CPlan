package com.github.pjpo.planning.ui.controller.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javafx.scene.control.DatePicker;
import javafx.util.StringConverter;

public class DefaultDatePickerConverter extends StringConverter<LocalDate> {

	private DateTimeFormatter dateFormatter;

	private DatePicker beforeDatePicker;

	private DatePicker afterDatePicker;
	
	public DefaultDatePickerConverter(
			DateTimeFormatter dateFormatter,
			DatePicker beforeDatePicker,
			DatePicker afterDatePicker) {

		this.dateFormatter = dateFormatter;
		this.beforeDatePicker = beforeDatePicker;
		this.afterDatePicker = afterDatePicker;
	}
	
	@Override
	public String toString(LocalDate object) {
		if (object == null)
			return null;
		else
			return dateFormatter.format(object);
	}
	
	@Override public LocalDate fromString(String string) {
		if (string == null) {
			return beforeDatePicker.getValue().plusDays(1);
		} else {
			LocalDate newDate = dateFormatter.parse(string, LocalDate::from);
			if (beforeDatePicker != null && !newDate.isAfter(beforeDatePicker.getValue())) {
				beforeDatePicker.setValue(newDate.minusDays(1));
			}
			if (afterDatePicker != null && !newDate.isBefore(afterDatePicker.getValue())) {
				afterDatePicker.setValue(newDate.plusDays(1));
			}
			return newDate;
		}
	}
}