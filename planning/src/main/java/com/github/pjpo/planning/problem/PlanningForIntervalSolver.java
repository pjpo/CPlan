package com.github.pjpo.planning.problem;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.loop.monitors.SearchMonitorFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.selectors.IntValueSelector;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.ESat;

import com.github.pjpo.planning.CPlanRandomStrategy;
import com.github.pjpo.planning.constraintsrules.PositionConstraintBase;
import com.github.pjpo.planning.model.Physician;
import com.github.pjpo.planning.model.Position;
import com.github.pjpo.planning.model.Solution;
import com.github.pjpo.planning.utils.IntervalDateTime;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table.Cell;

public class PlanningForIntervalSolver {
	
	/** Choco solver */
	private final Solver solver;
	
	/** Private HashTable for storing intvars from choco solver */
	private final HashBasedTable<LocalDate, String, Position> positions = HashBasedTable.create();
	
	/** Random number generator */
	private final Random random = new Random(new Date().getTime()); 

	public PlanningForIntervalSolver(
			final HashMap<Integer, Physician> physicians,
			final HashBasedTable<LocalDate, String, Position> positions,
			final List<PositionConstraintBase> positionsConstraints,
			final LinkedList<HashBasedTable<LocalDate, String, Position>> previousAcceptedSolutions) {
		
		// Creates choco solver
		this.solver = new Solver();
		
		// If a previous accepted solution exists, use it in order to clone the previous solution, else use the table
		// of positions as a reference
		// Clones the positions in order to modify them depending on the solver
		for (Cell<LocalDate, String, Position> position : previousAcceptedSolutions.size() > 0 ? previousAcceptedSolutions.getLast().cellSet() : positions.cellSet()) {
			this.positions.put(position.getRowKey(), position.getColumnKey(), (Position) position.getValue().clone());
		}

		// Finds how many id solutions are existing :
		int idem = 1;
		double workSD = -1;
		final Iterator<HashBasedTable<LocalDate, String, Position>> lastSolutions = previousAcceptedSolutions.descendingIterator();
		while (lastSolutions.hasNext()) {
			final HashBasedTable<LocalDate, String, Position> lastSolution = lastSolutions.next();
			if (workSD == -1 || lastSolution.getWorkLoadSD() == workSD) {
				idem++;
				workSD = lastSolution.getWorkLoadSD();
			} else {
				break;
			}
		}
		
		// IN 10% OF CASES, INCREASES THE SHAKER EXPONENT (TRY TO JUMP BETWEEN TWO MINIMAS SOLUTIONS LIKE ENTROPY)
		if (random.nextInt(10) == 0)
			idem = idem * idem;
		
		// Alters the last found solution in order to try to equilibrate workload
		l
		HashMap<LocalDate, HashMap<String, Integer>> preFill = previousAcceptedSolutions.size() == 0 ? null :
			previousAcceptedSolutions.getLast().lightenWorkBurden(idem);
		
		// For each day / position, use prefill if needed, and the create the constraints
		planningImplementation.getPositions().forEach((day, positions) -> {

			// Positions for this day (Choco uses IntVar for positions)
			final HashMap<String, IntVar> dayConstraintsVariables = new HashMap<>();
			constraintVariables.put(day, dayConstraintsVariables);
			
			// for each position, set which one can work
			positions.forEach((positionName, position) -> {
				// Do we have already somebody positioned in last solution modified with shaking? If yes, 
				if (preFill != null && preFill.get(day) != null &&
						preFill.get(day).containsKey(positionName)) {
					dayConstraintsVariables.put(positionName,
							VariableFactory.fixed(positionName + "_" + day, preFill.get(day).get(positionName), solver));
				}
				else {
					final List<Physician> physicians = planningImplementation.getPlanningConstraints().getPhysicians();

					// Do we have already somebody positioned to work in preferences ?
					for (int i = 0 ; i < physicians.size() ; i++) {
						if (physicians.get(i).getWorkedVacs().containsKey(day)
								&& physicians.get(i).getWorkedVacs().get(day).contains(positionName)) {
							dayConstraintsVariables.put(positionName,
									VariableFactory.fixed(
											positionName + "_" + day, i, solver));
							return;
						}
					}
					
					// Here, list the people not in vacation this day
					final LinkedList<Integer> workers = new LinkedList<>();
					eachPhysician : for (int i = 0 ; i < physicians.size() ; i++) {

						final Physician physician = physicians.get(i);
						
						// CHECK IF THIS PHYSICIAN HAS THE RIGHT TO WORK AT THIS POSITION
						if (physicians.get(i).getRefusedPostes().contains(positionName))
							continue eachPhysician;					
						
						// CHECK IF PHYSICIAN IS IN PAID VACATIONS
						for (final IntervalDateTime vacation : physician.getPaidVacation()) {
							if (vacation.isOverlapping(position.getPlage()))
								continue eachPhysician;
						}
						
						// CHECK IF PHYSICIAN IS IN UNPAID VACATIONS
						for (final IntervalDateTime vacation : physician.getUnpaidVacation()) {
							if (vacation.isOverlapping(position.getPlage()))
								continue eachPhysician;
						}
						
						// THIS PHYSICIAN CAN WORK AT THIS POSITION AT THIS DAY
						workers.add(i);
						
					}					
					// c(2) - TEST WITH THESE PHYSICIANS
					dayConstraintsVariables.put(positionName,
							VariableFactory.enumerated(
									positionName + "_" + day, workers.stream().mapToInt((value) -> value).toArray(), solver));


				}
			});
					
		});
		
		// CREATES THE GENERAL CONSTRAINTS AND APPLY THEM TO THE SOLVER
		for (LocalDate date = planningImplementation.getInterval().getStart() ; !date.isAfter(planningImplementation.getInterval().getEnd()) ; date = date.plusDays(1L)) {
			
		}
		
		// GETS AN ARRAY OF EACH INTVAR
		LinkedList<IntVar> allDays = new LinkedList<>();
		for (HashMap<String, IntVar> oneDay : constraintVariables.values()) {
			for (IntVar var : oneDay.values()) {
				allDays.add(var);
			}
		}
		
		IntVar[] allDaysArray = allDays.toArray(new IntVar[allDays.size()]);
		
		// USE A RANDOM CUSTOM SETTER
		solver.set(
				IntStrategyFactory.lastKConflicts(solver, 1000, IntStrategyFactory.custom(
				IntStrategyFactory.random_var_selector(new Date().getTime()), (IntValueSelector) new CPlanRandomStrategy(planningImplementation.getPlanningConstraints().getPhysicians()), allDaysArray)));
		
		SearchMonitorFactory.limitTime(solver, 600000);

	}
	
	public Solution findSolution() {

		// FINDS A SOLUTION
		solver.findSolution();
				
		// IF NO SOLUTION, RETRY IF A SOLUTION ALREADY EXISTS
		if (solver.isFeasible() != ESat.TRUE && planningImplementation.getPreviousAcceptedSolutions().size() == 0) {
			return null;
		} else {
			final Solution solution = new Solution(planningImplementation.getPositions(), planningImplementation.getPlanningConstraints().getPhysicians());
			solution.setSolutionMedIndicesMap(constraintVariables);
			return solution;
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

	public LinkedList<Solution> getPreviousAcceptedSolutions() {
		return previousAcceptedSolutions;
	}
}
