package com.github.pjpo.planning.jours;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import solver.constraints.Constraint;
import solver.variables.IntVar;

import com.github.pjpo.planning.utils.IntervalDateTime;

public interface Jour {
	public HashMap<String, IntervalDateTime> getPlages(LocalDate date); 
	public List<Constraint> getConstraints(
			LocalDate date, HashMap<LocalDate, HashMap<String, IntVar>> workers);
}
