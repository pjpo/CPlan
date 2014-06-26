package com.github.aiderpmsi.planning.lignes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;

public class SILigne implements Ligne {
	
	public HashMap<String, Plage> getPlages(final LocalDate date) {
		HashMap<String, Plage> plages = new HashMap<>();
		// EACH DAY, GENERATE TWO PERIODS
		LocalDateTime workStart = date.atTime(8, 30); 
		Plage plageJour = new Plage(workStart, workStart.plusHours(10));
		plages.put(getName() + "_1", plageJour);
		Plage plageNuit = new Plage(workStart.plusHours(10), workStart.plusHours(24));
		plages.put(getName() + "_2", plageNuit);
		return plages;
	}

	@Override
	public String getName() {
		return "Si";
	}

}
