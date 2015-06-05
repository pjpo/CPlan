package com.github.pjpo.planning;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import javax.script.ScriptException;

import com.github.pjpo.planning.model.PositionCode;
import com.github.pjpo.planning.model.PositionCode.Position;
import com.github.pjpo.planning.utils.IntervalDate;

/**
 * Uses the constraints of the planning ({@link PlanningConstraints}) to define the content of the planning for an interval
 * @author jpc
 *
 */
public class PlanningImplementation {

	/** List of days and corresponding positions */
	private final HashMap<LocalDate, HashMap<String, Position>> positions = new HashMap<>();
	
	/** Interval of agenda */
	private final IntervalDate interval;
	
	/** Reference to planning constraints */
	private final PlanningConstraints planningConstraints;

	/** Random number generator */
	private final Random random = new Random(new Date().getTime()); 
			
	/** Previous solutions found */
	private final LinkedList<Solution> previousAcceptedSolutions = new LinkedList<>();
	
	/**
	 * Creates an agenda for this kind of jour and a defined interval
	 * @param typeJour definition of the positions
	 * @param intervalDate
	 * @param positions 
	 */
	public PlanningImplementation(
			final IntervalDate intervalDate,
			final PlanningConstraints planningConstraints) {
		// Verifies that the boundings of planning have been defined
		if (intervalDate.getStart() == null || intervalDate.getEnd() == null)
			throw new IllegalArgumentException("interval must be finite");
		
		// Sets the values of clas fields
		this.interval = intervalDate;
		this.planningConstraints = planningConstraints;
		
		// Calculates the positions for this interval
		for (LocalDate date = intervalDate.getStart() ; !date.isAfter(intervalDate.getEnd()) ; date = date.plusDays(1L)) {
			final HashMap<String, Position> datePositions = new HashMap<>();
			positions.put(date, datePositions);
			for (final PositionCode positionCode : planningConstraints.getPositionsDefinitions()) {
				try {
					final Position position = positionCode.getPosition(date);
					if (position.isWorking())
						datePositions.put(position.getName(), position);
				} catch (ScriptException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public Solution findNewSolution() {
		// Creates the new Solver from last solution and an indice of shaking which depends on the previous solutions
		int idem = 1;
		double workSD = -1;
			
		// From last solution to first
		final Iterator<Solution> lastSolutions =  previousAcceptedSolutions.descendingIterator();
		while (lastSolutions.hasNext()) {
			final Solution lastSolution = lastSolutions.next();
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
		
		final PlanningSolver planningSolver = new PlanningSolver(idem, this);
		
		final Solution newSolution = planningSolver.findSolution();
		
		// IF WE HAVE AT LEAST 1 SOLUTIONS IN SOLUTIONS LIST, COMPARE IT WITH THE PRECEDENT
		if (getPreviousAcceptedSolutions().size() > 0 &&
				newSolution.getWorkLoadSD() > getPreviousAcceptedSolutions().getLast().getWorkLoadSD()) {
				// REJECTED SOLUTION
				return null;
		}
		// IF WE ARE THERE, THIS SOLUTION IS BETTER THAN THE PRECEDENT
		else {
			getPreviousAcceptedSolutions().add(newSolution);
			return newSolution;
		}
	}

	public HashMap<LocalDate, HashMap<String, Position>> getPositions() {
		return positions;
	}

	public IntervalDate getInterval() {
		return interval;
	}

	public PlanningConstraints getPlanningConstraints() {
		return planningConstraints;
	}

	public LinkedList<Solution> getPreviousAcceptedSolutions() {
		return previousAcceptedSolutions;
	}
}
