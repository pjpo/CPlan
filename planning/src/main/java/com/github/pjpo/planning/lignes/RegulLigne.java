package com.github.pjpo.planning.lignes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;

public class RegulLigne implements Ligne {
	
	public HashMap<String, Plage> getPlages(final LocalDate date) {
		HashMap<String, Plage> plages = new HashMap<>();
		// EACH DAY, GENERATE TWO PERIODS
		LocalDateTime workStart = date.atTime(8, 30); 
		Plage plageJour = new Plage(workStart, workStart.plusHours(10));
		plages.put(getName() + "_1", plageJour);
		Plage plageMilieu = new Plage(date.atTime(10, 00), date.plusDays(1).atTime(00, 00));
		plages.put(getName() + "_2", plageMilieu);
		Plage plageNuit = new Plage(workStart.plusHours(10), workStart.plusHours(24));
		plages.put(getName() + "_3", plageNuit);
		return plages;
	}

	@Override
	public String getName() {
		return "Regul";
	}

}
