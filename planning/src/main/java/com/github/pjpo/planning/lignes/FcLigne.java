package com.github.pjpo.planning.lignes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;

public class FcLigne implements Ligne {
	
	public HashMap<String, Plage> getPlages(final LocalDate date) {
		HashMap<String, Plage> plages = new HashMap<>();
		// GENERATE ONE PERIOD IF WE ARE AN OPEN DAY
		switch(date.getDayOfWeek()) {
		case MONDAY:
		case TUESDAY:
		case WEDNESDAY:
		case THURSDAY:
		case FRIDAY:
			LocalDateTime workStart = date.atTime(8, 30); 
			Plage plageJour = new Plage(workStart, workStart.plusHours(10));
			plages.put(getName() + "_1", plageJour);
			break;
		default:
			// DO NOTHING
		}
		return plages;
	}

	@Override
	public String getName() {
		return "Fc";
	}

}
