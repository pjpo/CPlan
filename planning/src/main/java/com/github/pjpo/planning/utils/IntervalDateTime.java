package com.github.pjpo.planning.utils;

import java.time.LocalDateTime;

public class IntervalDateTime implements Comparable<IntervalDateTime> {

	private LocalDateTime start;
	
	private LocalDateTime end;
	
	public IntervalDateTime() { }
	
	public IntervalDateTime(LocalDateTime start, LocalDateTime end) {
		testInterval(start, end);
		this.start = start;
		this.end = end;
	}

	public boolean isInPeriod(LocalDateTime date) {
		if (date == null)
			throw new IllegalArgumentException("Date is null");
		else if (start != null && date.isBefore(start))
			return false;
		else if (end != null && date.isAfter(end))
			return false;
		else
			return true;
	}

	public boolean isOverlapping(IntervalDateTime compare) {
		if (
				((start != null && compare.getEnd() != null && !compare.getEnd().isBefore(start)) || start == null || compare.getEnd() == null)
				&&
				((end != null && compare.getStart() != null && !end.isBefore(compare.getStart())) || end == null || compare.getStart() == null)) {
			return true;
		} else 
			return false;
	}
	
	public LocalDateTime getStart() {
		return start;
	}
	
	public LocalDateTime getEnd() {
		return end;
	}

	public void setStart(LocalDateTime start) {
		testInterval(start, end);
		this.start = start;
	}
	
	public void setEnd(LocalDateTime end) {
		testInterval(start, end);
		this.end = end;
	}

	@Override
	public int compareTo(IntervalDateTime compare) {
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
	
	private void testInterval(LocalDateTime start, LocalDateTime end) throws IllegalArgumentException {
		if (start != null && end != null && end.isAfter(start)) {
			throw new IllegalArgumentException("Start date must be before end date");
		}
	}

}
