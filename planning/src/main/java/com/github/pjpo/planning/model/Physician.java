package com.github.pjpo.planning.model;

import java.time.LocalDate;
import java.util.List;

import com.github.pjpo.planning.utils.IntervalDateTime;
import com.google.common.collect.Multimap;

public class Physician {

	private String name;
	
	private Integer timePart;
		
	private List<IntervalDateTime> paidVacation;
	
	private List<IntervalDateTime> unpaidVacation;

	private Multimap<LocalDate, String> workedVacs;
	
	private List<String> refusedPostes;
	
	private Integer internalIndice;

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

	public List<IntervalDateTime> getPaidVacation() {
		return paidVacation;
	}

	public void setPaidVacation(List<IntervalDateTime> paidVacation) {
		this.paidVacation = paidVacation;
	}

	public List<IntervalDateTime> getUnpaidVacation() {
		return unpaidVacation;
	}

	public void setUnpaidVacation(List<IntervalDateTime> unpaidVacation) {
		this.unpaidVacation = unpaidVacation;
	}

	public Multimap<LocalDate, String> getWorkedVacs() {
		return workedVacs;
	}

	public void setWorkedVacs(Multimap<LocalDate, String> workedVacs) {
		this.workedVacs = workedVacs;
	}

	public List<String> getRefusedPostes() {
		return refusedPostes;
	}

	public void setRefusedPostes(List<String> refusedPostes) {
		this.refusedPostes = refusedPostes;
	}

	public int getInternalIndice() {
		return internalIndice;
	}

	public void setInternalIndice(int internalIndice) {
		this.internalIndice = internalIndice;
	}

}
