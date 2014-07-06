package com.github.pjpo.planning.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class IntervalDate implements Comparable<IntervalDate> {

	private LocalDate start;
	
	private LocalDate end;
	
	public IntervalDate() { }
	
	public IntervalDate(LocalDate start, LocalDate end) {
		testInterval(start, end);
		this.start = start;
		this.end = end;
	}

	public boolean isInPeriod(LocalDateTime date) {
		if (date == null)
			throw new IllegalArgumentException("Date is null");
		else if (start != null && date.isBefore(start.atStartOfDay()))
			return false;
		else if (end != null && end.plusDays(1).atStartOfDay().isBefore(date))
			return false;
		else
			return true;
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
	
	public boolean isOverlapping(IntervalDate compare) {
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

	public BoundedLocalDate getBoundedStart() {
		return new BoundedLocalDate(end, start);
	}
	
	public BoundedLocalDate getBoundedEnd() {
		return new BoundedLocalDate(start, end);
	}

	public void setStart(LocalDate start) {
		testInterval(start, end);
		this.start = start;
	}
	
	public void setEnd(LocalDate end) {
		testInterval(start, end);
		this.end = end;
	}

	public void setBoundedStart(BoundedLocalDate start) {
		testInterval(start.getDate(), end);
		this.start = start.getDate();
	}
	
	public void setBoundedEnd(BoundedLocalDate end) {
		testInterval(start, end.getDate());
		this.end = end.getDate();
	}

	@Override
	public int compareTo(IntervalDate compare) {
		if (start == compare.getStart() && end == compare.getEnd())
			return 0;
		else if (start == null && compare.getStart() != null)
			return -1;
		else if (start != null && compare.getStart() == null)
			return 1;
		else {
			return start.compareTo(end);
		}
	}
	
	private void testInterval(LocalDate start, LocalDate end) throws IllegalArgumentException {
		if (start != null && end != null && start.isAfter(end)) {
			throw new IllegalArgumentException("Start date must be before end date");
		}
	}
	
	public String toString() {
		return (start == null ? "null" : start.toString()) + " - " +(end == null ? "null" : end.toString()); 
	}

}
