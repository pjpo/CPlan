package com.github.aiderpmsi.planning;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;

public class FmcLigne implements Ligne {
	
	public HashMap<String, Plage> getPlages(final LocalDate date) {
		HashMap<String, Plage> plages = new HashMap<>();
		// EACH DAY, GENERATE TWO PERIODS
		LocalDateTime workStart = LocalDateTime.from(date).plusHours(8).plusMinutes(30); 
		Plage plageJour = new Plage(workStart, workStart.plusHours(10));
		plages.put(getName() + "_1", plageJour);
		Plage plageNuit = new Plage(workStart.plusHours(10), workStart.plusHours(24));
		plages.put(getName() + "_1", plageNuit);
		return plages;
	}

	@Override
	public String getName() {
		return "Fmc";
	}

}
