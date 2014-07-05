package com.github.pjpo.planning.physician;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

import com.github.pjpo.planning.utils.IntervalDate;

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

	public ArrayList<IntervalDate> getPaidVacation() {
		return paidVacation;
	}

	public void setPaidVacation(ArrayList<IntervalDate> paidVacation) {
		this.paidVacation = paidVacation;
	}

	public ArrayList<IntervalDate> getUnpaidVacation() {
		return unpaidVacation;
	}

	public void setUnpaidVacation(ArrayList<IntervalDate> unpaidVacation) {
		this.unpaidVacation = unpaidVacation;
	}

	public HashMap<LocalDate, ArrayList<String>> getWorkedVacs() {
		return workedVacs;
	}

	public void setWorkedVacs(HashMap<LocalDate, ArrayList<String>> workedVacs) {
		this.workedVacs = workedVacs;
	}

	public ArrayList<String> getRefusedPostes() {
		return refusedPostes;
	}

	public void setRefusedPostes(ArrayList<String> refusedPostes) {
		this.refusedPostes = refusedPostes;
	}

}
