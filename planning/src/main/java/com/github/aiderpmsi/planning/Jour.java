package com.github.aiderpmsi.planning;

import java.time.LocalDate;
import java.util.List;

import solver.Solver;
import solver.constraints.Constraint;
import solver.variables.IntVar;

public interface Jour {
	public List<IntVar> getPlages(LocalDate date, Solver solver);
	public List<Constraint> getConstraints(Jour precedingDay, Solver solver);
}
