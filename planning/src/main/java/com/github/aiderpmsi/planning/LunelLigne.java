package com.github.aiderpmsi.planning;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class LunelLigne implements Ligne {

	DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern("YYYY-MM-dd").toFormatter();
	
	public HashMap<String, PlageLigne> getPlages(final LocalDate date) {
		HashMap<String, PlageLigne> plages = new HashMap<>();
		// IF DAY IS EVEN, GENERATE TWO PERIODS
		if ((date.getDayOfYear() & 1) != 0) {
			String dateS = formatter.format(date);
			LocalDateTime workStart = LocalDateTime.from(date).plusHours(8).plusMinutes(30); 
			PlageLigne plageJour = new PlageLigne(dateS + "_1_" + getName(), workStart, workStart.plusHours(10));
			plages.put(dateS + "_1_" + getName(), plageJour);
			PlageLigne plageNuit = new PlageLigne(dateS + "_2_" + getName(), workStart.plusHours(10), workStart.plusHours(24));
			plages.put(dateS + "_1_" + getName(), plageNuit);
		}
		return plages;
	}

	@Override
	public String getName() {
		return "Lunel";
	}

}
