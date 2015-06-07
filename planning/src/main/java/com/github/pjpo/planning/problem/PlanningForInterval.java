package com.github.pjpo.planning.problem;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.script.ScriptException;

import com.github.pjpo.planning.constraintsrules.PositionConstraintBase;
import com.github.pjpo.planning.model.Physician;
import com.github.pjpo.planning.model.Position;
import com.github.pjpo.planning.model.PositionCode;
import com.github.pjpo.planning.model.Solution;
import com.github.pjpo.planning.utils.IntervalDate;
import com.google.common.collect.ArrayTable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;

/**
 * Uses the constraints of the planning ({@link PlanningDefinition}) to define the content of the planning for an interval
 * @author jpc
 *
 */
public class PlanningForInterval {

	// ==== Informative fields (external) ====
	
	/** Interval of planning */
	private final IntervalDate interval;
	
	/** List of used physicians */
	private final HashMap<Integer, Physician> physicians;
	
	/** List of positions definitions */
	private final List<PositionCode> positionsDefinitions;
	
	/** List of intra and interday constraints */
	private final List<PositionConstraintBase> positionsConstraints;

	// ==== Calculated invariable fields (reference) ====
	
	/** List of positions By Date and name (indexed positions)*/
	private final HashBasedTable<LocalDate, String, Position> positions;

	/** Random number generator */
	private final Random random = new Random(new Date().getTime()); 

	// ==== Variable Fields ====
				
	/** Solutions already found */
	private final LinkedList<Solution> previousAcceptedSolutions = new LinkedList<>();
	
	/**
	 * Creates a planning with a planning definition for a given interval of dates
	 * @param intervalDate
	 * @param 
	 */
	public PlanningForInterval(
			final IntervalDate intervalDate,
			final HashMap<Integer, Physician> physicians,
			final List<PositionCode> positionsDefinitions,
			final List<PositionConstraintBase> positionsConstraints) {
		
		// Verifies that the boundings of planning have been defined
		if (intervalDate.getStart() == null || intervalDate.getEnd() == null)
			throw new IllegalArgumentException("interval must be finite");
		
		// Sets the values of class fields
		this.interval = intervalDate;
		this.physicians = physicians;
		this.positionsDefinitions = positionsDefinitions;
		this.positionsConstraints = positionsConstraints;
		
		// Creates the positions arraytable with predefined size
		positions = HashBasedTable.create();

		// Calculates the positions for this interval and store them in positions and index it in positionsByDate
		for (LocalDate date = intervalDate.getStart() ; !date.isAfter(intervalDate.getEnd()) ; date = date.plusDays(1L)) {
			for (final PositionCode positionCode : positionsDefinitions) {
				try {
					final Position position = positionCode.getPosition(date);
					if (position.getIsWorking())
						positions.put(date, position.getName(), position);
				} catch (ScriptException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void findNewSolution() {
		// Creates the planning solver from :
		// - The physicians definitions
		// - The existing positions
		// - The constraints
		// - The already found solutions
		final PlanningForIntervalSolver solver =
				new PlanningForIntervalSolver(physicians, positions, positionsConstraints, previousAcceptedSolutions);
		
		// Finds a new solution
		final Solution newSolution = solver.findSolution();
		
		// See if this solution is better than the precedent solution
		if (previousAcceptedSolutions.size() > 0 &&
				newSolution.getWorkLoadSD() <= previousAcceptedSolutions.getLast().getWorkLoadSD()) {
				// Accepted solution
			previousAcceptedSolutions.add(newSolution);
		}
	}

}
