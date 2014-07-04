package com.github.pjpo.planning.physician;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

import com.github.pjpo.planning.utils.DaysPeriod;

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

	public ArrayList<DaysPeriod> getPaidVacation() {
		return paidVacation;
	}

	public void setPaidVacation(ArrayList<DaysPeriod> paidVacation) {
		this.paidVacation = paidVacation;
	}

	public ArrayList<DaysPeriod> getUnpaidVacation() {
		return unpaidVacation;
	}

	public void setUnpaidVacation(ArrayList<DaysPeriod> unpaidVacation) {
		this.unpaidVacation = unpaidVacation;
	}

	public HashMap<LocalDate, ArrayList<String>> getWorkedVacs() {
		return workedVacs;
	}

	public void setWorkedVacs(HashMap<LocalDate, ArrayList<String>> workedVacs) {
		this.workedVacs = workedVacs;
	}

}
