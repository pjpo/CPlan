package com.github.pjpo.planning.lignes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;

import com.github.pjpo.planning.utils.IntervalDateTime;

public class UHCDLigne implements Ligne {
	
	public HashMap<String, IntervalDateTime> getPlages(final LocalDate date) {
		HashMap<String, IntervalDateTime> plages = new HashMap<>();
		// GENERATE ONE PERIOD IF WE ARE AN OPEN DAY
		switch(date.getDayOfWeek()) {
		case MONDAY:
		case TUESDAY:
		case WEDNESDAY:
		case THURSDAY:
		case FRIDAY:
			LocalDateTime workStart = date.atTime(8, 30); 
			IntervalDateTime plageJour = new IntervalDateTime(workStart, workStart.plusHours(10));
			plages.put(getName() + "_1", plageJour);
			break;
		default:
			// DO NOTHING
		}
		return plages;
	}

	public String getName() {
		return "Uhcd";
	}

}
