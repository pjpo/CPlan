package com.github.aiderpmsi.planning;

import java.time.LocalDateTime;

public class Plage {

	private LocalDateTime start = null;
	
	private LocalDateTime end = null;
	
	public Plage() { }
	
	public Plage(final LocalDateTime start, final LocalDateTime end) {
		setStart(start);
		setEnd(end);
	}
	
	public boolean overlap(final Plage plage) {
		if (start == null && plage.start == null) {
			return true;
		} else if (end == null && plage.end == null) {
			return true;
		} else if (start != null && plage.end != null) {
			return start.isBefore(plage.end);
		} else if (end != null && plage.start != null) {
			return plage.start.isBefore(end);
		} else {
			throw new RuntimeException("Implementation error");
		}
	}

	public LocalDateTime getStart() {
		return start;
	}

	public void setStart(final LocalDateTime start) {
		if (start == null || end == null || end.isAfter(start)) {
			this.start = start;
		} else {
			throw new IllegalArgumentException("Start (" + start.toString() + ") is after end (" + end.toString() + ")");
		}
	}

	public LocalDateTime getEnd() {
		return end;
	}

	public void setEnd(final LocalDateTime end) {
		if (start == null || end == null || end.isAfter(start)) {
			this.end = end;
		} else {
			throw new IllegalArgumentException("Start (" + start.toString() + ") is after end (" + end.toString() + ")");
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((end == null) ? 0 : end.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Plage other = (Plage) obj;
		if (end == null) {
			if (other.end != null)
				return false;
		} else if (!end.equals(other.end))
			return false;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		return true;
	}
	
	
}
