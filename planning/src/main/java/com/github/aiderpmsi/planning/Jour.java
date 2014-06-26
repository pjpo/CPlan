package com.github.aiderpmsi.planning;

import java.time.LocalDate;
import solver.Solver;

public interface Jour {
	public void generatePlages(LocalDate date, Solver solver); 
	public void generateConstraints(LocalDate date, Solver solver);
}
