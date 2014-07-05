package com.github.pjpo.planning.lignes;

import java.time.LocalDate;
import java.util.HashMap;

import com.github.pjpo.planning.utils.IntervalDateTime;

public interface Ligne {
	public HashMap<String, IntervalDateTime> getPlages(final LocalDate date);
	public String getName();
}
