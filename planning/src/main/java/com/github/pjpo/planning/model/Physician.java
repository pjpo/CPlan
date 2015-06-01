package com.github.pjpo.planning.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

import com.github.pjpo.planning.utils.IntervalDateTime;

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

	public ArrayList<IntervalDateTime> getPaidVacation() {
		return paidVacation;
	}

	public void setPaidVacation(ArrayList<IntervalDateTime> paidVacation) {
		this.paidVacation = paidVacation;
	}

	public ArrayList<IntervalDateTime> getUnpaidVacation() {
		return unpaidVacation;
	}

	public void setUnpaidVacation(ArrayList<IntervalDateTime> unpaidVacation) {
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
