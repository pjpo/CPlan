package com.github.pjpo.planning.lignes;

import java.time.LocalDate;
import java.util.HashMap;

public interface Ligne {
	public HashMap<String, Plage> getPlages(final LocalDate date);
	public String getName();
}
