package com.github.pjpo.planning.lignes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;

import com.github.pjpo.planning.utils.IntervalDateTime;

public class RegulLigne implements Ligne {
	
	public HashMap<String, IntervalDateTime> getPlages(final LocalDate date) {
		HashMap<String, IntervalDateTime> plages = new HashMap<>();
		// EACH DAY, GENERATE TWO PERIODS
		LocalDateTime workStart = date.atTime(8, 30); 
		IntervalDateTime plageJour = new IntervalDateTime(workStart, workStart.plusHours(10));
		plages.put(getName() + "_1", plageJour);
		IntervalDateTime plageMilieu = new IntervalDateTime(date.atTime(10, 00), date.plusDays(1).atTime(00, 00));
		plages.put(getName() + "_2", plageMilieu);
		IntervalDateTime plageNuit = new IntervalDateTime(workStart.plusHours(10), workStart.plusHours(24));
		plages.put(getName() + "_3", plageNuit);
		return plages;
	}

	public String getName() {
		return "Regul";
	}

}
