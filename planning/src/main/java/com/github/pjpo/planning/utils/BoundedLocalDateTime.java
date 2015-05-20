package com.github.pjpo.planning.utils;

import java.time.LocalDateTime;

public class BoundedLocalDateTime {

	private LocalDateTime bound;
	
	private LocalDateTime date;
	
	public BoundedLocalDateTime() {
	}

	public BoundedLocalDateTime(LocalDateTime bound, LocalDateTime date) {
		this.bound = bound;
		this.date = date;
	}

	public LocalDateTime getBound() {
		return bound;
	}

	public void setBound(LocalDateTime bound) {
		this.bound = bound;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

}
