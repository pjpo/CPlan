package com.github.pjpo.planning.physician;

import java.time.LocalDate;

public class Physician extends PhysicianBase {

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getTimePart() {
		return timePart;
	}

	public void setTimePart(Integer timePart) {
		this.timePart = timePart;
	}
	

	public LocalDate getWorkStart() {
		return workStart;
	}

	public void setWorkStart(LocalDate workStart) {
		this.workStart = workStart;
	}

	public LocalDate getWorkEnd() {
		return workEnd;
	}

	public void setWorkEnd(LocalDate workEnd) {
		this.workEnd = workEnd;
	}

}
