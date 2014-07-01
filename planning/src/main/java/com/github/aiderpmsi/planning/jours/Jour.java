package com.github.aiderpmsi.planning.jours;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import solver.constraints.Constraint;
import solver.variables.IntVar;

import com.github.aiderpmsi.planning.lignes.Plage;

public interface Jour {
	public HashMap<String, Plage> getPlages(LocalDate date); 
	public List<Constraint> getConstraints(
			LocalDate date, HashMap<LocalDate, HashMap<String, IntVar>> workers);
}
