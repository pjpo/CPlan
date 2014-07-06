package com.github.pjpo.planning.utils;

import java.time.LocalDate;

public class BoundedLocalDate {

	private LocalDate bound;
	
	private LocalDate date;
	
	public BoundedLocalDate() {
	}

	public BoundedLocalDate(LocalDate bound, LocalDate date) {
		this.bound = bound;
		this.date = date;
	}

	public LocalDate getBound() {
		return bound;
	}

	public void setBound(LocalDate bound) {
		this.bound = bound;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

}
