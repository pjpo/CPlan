package com.github.pjpo.planning.problem;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import javax.script.ScriptException;

import com.github.pjpo.planning.model.Position;
import com.github.pjpo.planning.model.PositionConstraintBase;
import com.github.pjpo.planning.model.PositionDefinition;
import com.github.pjpo.planning.model.Worker;
import com.github.pjpo.planning.utils.IntervalDate;
import com.google.common.collect.HashBasedTable;

/**
 * Uses the constraints of the planning ({@link PlanningDefinition}) to define the content of the planning for an interval
 * @author jpc
 *
 */
public class PlanningForInterval {

	// ==== Informative fields (external) ====
	
	/** List of used physicians */
	private final HashMap<Integer, Worker> physicians;
	
	/** List of intra and interday constraints */
	private final List<PositionConstraintBase> positionsConstraints;

	// ==== Calculated invariable fields (reference) ====
	
	/** List of positions By Date and name (indexed positions)*/
	private final HashBasedTable<LocalDate, String, Position> positions;

	// ==== Variable Fields ====
				
	/** Workload SD of solutions already found */
	private Double previousWorkLoad = null;
	
	/** Previous undefined workers for previous solutions */
	private Long previousUndefinedPositions = null;
	
	/** Last good solution */
	private Solution solution = null;
	
	/**
	 * Creates a planning with a planning definition for a given interval of dates
	 * @param intervalDate
	 * @param 
	 */
	public PlanningForInterval(
			final IntervalDate intervalDate,
			final HashMap<Integer, Worker> physicians,
			final List<PositionDefinition> positionsDefinitions,
			final List<PositionConstraintBase> positionsConstraints) {
		
		// Verifies that the boundings of planning have been defined
		if (intervalDate.getStart() == null || intervalDate.getEnd() == null)
			throw new IllegalArgumentException("interval must be finite");
		
		// Sets the values of class fields
		this.physicians = physicians;
		this.positionsConstraints = positionsConstraints;
		
		// Creates the positions arraytable with predefined size
		positions = HashBasedTable.create();

		// Calculates the positions for this interval and store them in positions and index it in positionsByDate
		for (LocalDate date = intervalDate.getStart() ; !date.isAfter(intervalDate.getEnd()) ; date = date.plusDays(1L)) {
			for (final PositionDefinition positionCode : positionsDefinitions) {
				try {
					final Position position = positionCode.getPosition(date);
					if (position.getIsActive())
						positions.put(date, position.getName(), position);
				} catch (ScriptException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Tries to find a new solution
	 * @return true if a solution has been found, false otherwise
	 */
	public boolean findNewSolution() {
		// Creates the planning solver from :
		// - The physicians definitions
		// - The existing positions
		// - The constraints
		// - The already found solutions
		final PlanningForIntervalSolver solver =
				new PlanningForIntervalSolver(physicians, positions, positionsConstraints, solution);
		
		// Finds a new solution
		final Solution newSolution = solver.findSolution();
		
		// if no solution, return false
		if (newSolution == null) {
			return false;
		} else {
			// See if this solution is better than the precedent solution
			final Long newUndefinedPositions = newSolution.getUndefinedWorkers();
			final Double newWorkLoadSD = newSolution.getWorkLoadSD();
			// 1 - Try to decrease the number of undefined positions
			if (solution != null && newUndefinedPositions < previousUndefinedPositions) {
				// Accepted solution
				this.solution = newSolution;
				previousUndefinedPositions = newUndefinedPositions;
				previousWorkLoad = newWorkLoadSD;
			}
			// 2 - Try to decrease the workloadsd
			else if (solution != null && newUndefinedPositions == previousUndefinedPositions && 
					newWorkLoadSD < previousWorkLoad) {
				// Accepted solution
				this.solution = newSolution;
				previousUndefinedPositions = newUndefinedPositions;
				previousWorkLoad = newWorkLoadSD;
			}
			// If no other solution, add this one
			else if (solution == null) {
				this.solution = newSolution;
				previousUndefinedPositions = newUndefinedPositions;
				previousWorkLoad = newWorkLoadSD;
			}
			return true;
		}		
	}

	/**
	 * Returns the workload standard deviation for the previously found solution
	 * @return
	 */
	public Double getWorkLoadSD() {
		return previousWorkLoad;
	}
	
	/**
	 * returns the last best solution found
	 * @return
	 */
	public Solution getSolution() {
		return solution;
	}
}
