package com.github.pjpo.planning.lignes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;

import com.github.pjpo.planning.utils.IntervalDateTime;

public class GangesLigne implements Ligne {
	
	public HashMap<String, IntervalDateTime> getPlages(final LocalDate date) {
		HashMap<String, IntervalDateTime> plages = new HashMap<>();
		// IF DAY IS ODD, GENERATE TWO PERIODS
		if ((date.getDayOfYear() & 1) == 0) {
			LocalDateTime workStart = date.atTime(8, 30); 
			IntervalDateTime plageJour = new IntervalDateTime(workStart, workStart.plusHours(10));
			plages.put(getName() + "_1", plageJour);
			IntervalDateTime plageNuit = new IntervalDateTime(workStart.plusHours(10), workStart.plusHours(24));
			plages.put(getName() + "_2", plageNuit);
		}
		return plages;
	}

	@Override
	public String getName() {
		return "Ganges";
	}

}