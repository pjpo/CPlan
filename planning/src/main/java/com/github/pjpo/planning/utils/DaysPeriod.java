package com.github.pjpo.planning.utils;

import java.time.LocalDate;

public class DaysPeriod {

	private LocalDate start;
	
	private LocalDate end;
	
	public DaysPeriod(LocalDate start, LocalDate end) {
		this.start = start;
		this.end = end;
	}
	
	public boolean isInPeriod(LocalDate date) {
		if (date == null)
			throw new IllegalArgumentException("Date is null");
		else if (start != null && date.isBefore(start))
			return false;
		else if (end != null && date.isAfter(end))
			return false;
		else
			return true;
	}

	
	public boolean isOverlapping(DaysPeriod compare) {
		if (
				((start != null && compare.getEnd() != null && !compare.getEnd().isBefore(start)) || start == null || compare.getEnd() == null)
				&&
				((end != null && compare.getStart() != null && !end.isBefore(compare.getStart())) || end == null || compare.getStart() == null)) {
			return true;
		} else 
			return false;
	}
	
	public LocalDate getStart() {
		return start;
	}
	
	public LocalDate getEnd() {
		return end;
	}
	
	
}
