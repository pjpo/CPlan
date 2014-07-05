package com.github.pjpo.planning.lignes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;

import com.github.pjpo.planning.utils.IntervalDateTime;

public class AgdeLigne implements Ligne {
	
	public HashMap<String, IntervalDateTime> getPlages(final LocalDate date) {
		HashMap<String, IntervalDateTime> plages = new HashMap<>();
		// IF DAY IS EVEN AND BETWEEN JUNE AND SEPTEMBER, GENERATE TWO PERIODS
		if ((date.getDayOfYear() & 1) != 0 && date.getMonthValue() >= 6 && date.getMonthValue() <= 9) {
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
		return "Agde";
	}

}
