package com.github.pjpo.planning;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import com.github.pjpo.planning.physician.Physician;
import com.github.pjpo.planning.utils.IntervalDateTime;

import solver.Solver;
import solver.variables.IntVar;
import util.ESat;

public class PlanningSolver {

	private final Solver solver;
	
	/** List of days and worked periods */
	private final HashMap<LocalDate, HashMap<String, IntervalDateTime>> workingPeriods;

	private final HashMap<LocalDate, HashMap<String, IntVar>> constraintVariables;
	
	private final ArrayList<Physician> physicians;

	private final LinkedList<Solution> previousAcceptedSolutions;
		
	public PlanningSolver(final Solver solver,
			final HashMap<LocalDate, HashMap<String, IntervalDateTime>> workingPeriods,
			final HashMap<LocalDate, HashMap<String, IntVar>> constraintVariables,
			final LinkedList<Solution> previousAcceptedSolutions,
			final ArrayList<Physician> physicians) throws IllegalArgumentException {

		// IF NO PHYSICIAN HAS BEEN SET, THROW EXCEPTION
		if (physicians.size() == 0)
			throw new IllegalArgumentException("Aucun médecin défini");
		
		this.solver = solver;
		this.constraintVariables = constraintVariables;
		this.physicians = physicians;
		this.workingPeriods = workingPeriods;
		this.previousAcceptedSolutions = previousAcceptedSolutions;
	}
	
	public Solution findSolution() {

		// FINDS A SOLUTION
		solver.findSolution();
		
		// IF NO SOLUTION, RETRY IF A SOLUTION ALREADY EXISTS
		if (solver.isFeasible() != ESat.TRUE && previousAcceptedSolutions.size() == 0) {
			return null;
		} else {
			Solution solution = new Solution(workingPeriods, physicians);
			solution.setSolutionMedIndicesMap(constraintVariables);
			// IF WE HAVE AT LEAST 1 SOLUTIONS IN SOLUTIONS LIST, COMPARE IT WITH THE PRECEDENT
			if (previousAcceptedSolutions.size() > 0 &&
					(solution.getMaxWorkLoad() - solution.getMinWorkLoad()) > (previousAcceptedSolutions.getLast().getMaxWorkLoad() - previousAcceptedSolutions.getLast().getMinWorkLoad())) {
					// REJECTED SOLUTION
					return null;
			}
			// IF WE ARE THERE, THIS SOLUTION IS BETTER THAN THE PRECEDENT
			else {
				return solution;
			}
		}
	}
	
	public void stopProcessing(String reason) {
		solver.getSearchLoop().interrupt(reason);
	}

	public boolean hasSolution() {
		return solver.isFeasible() == ESat.TRUE;
	}

	public boolean isUndefined() {
		return solver.isFeasible() == ESat.UNDEFINED;
	}
}