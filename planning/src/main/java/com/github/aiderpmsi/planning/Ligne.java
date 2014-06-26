package com.github.aiderpmsi.planning;

import java.time.LocalDate;
import java.util.HashMap;

public interface Ligne {
	public HashMap<String, PlageLigne> getPlages(final LocalDate date);
	public String getName();
}
