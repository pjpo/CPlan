package com.github.pjpo.planning.ui.model;

import java.time.LocalDate;

public class Poste {
	private LocalDate date;
	private String poste;
	
	public LocalDate getDate() {
		return date;
	}
	
	public void setDate(LocalDate date) {
		this.date = date;
	}
	
	public String getPoste() {
		return poste;
	}
	
	public void setPoste(String poste) {
		this.poste = poste;
	}
}