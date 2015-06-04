package com.github.pjpo.planning;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.script.ScriptException;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.loop.monitors.SearchMonitorFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.selectors.IntValueSelector;
import org.chocosolver.solver.variables.IntVar;

import com.github.pjpo.planning.jours.Agenda;
import com.github.pjpo.planning.model.Physician;
import com.github.pjpo.planning.model.PositionCode;
import com.github.pjpo.planning.model.PositionCode.Position;
import com.github.pjpo.planning.utils.IntervalDate;

/**
 * Classe principale de planning
 * @author jpc
 *
 */
public class Planning {

	/** List of used physicians */
	private final ArrayList<Physician> physicians;
	
	/** List of days and corresponding positions to fill*/
	private final HashMap<LocalDate, HashMap<String, Position>> positions = new HashMap<>();
	
	/** Agenda */
	private final Agenda agenda;
	
	private final Random random = new Random(new Date().getTime());
	
	public Planning(
			final IntervalDate intervalDate,
			final ArrayList<Physician> physicians,
			final List<PositionCode> positionsCode) {
		this.physicians = physicians;

		// Finds the positions defined for this interval in positionsCode
		for (LocalDate date = intervalDate.getStart() ; !date.isAfter(intervalDate.getEnd()) ; date = date.plusDays(1L)) {
			final HashMap<String, Position> datePositions = new HashMap<>();
			positions.put(date, datePositions);
			for (final PositionCode positionCode : positionsCode) {
				try {
					final Position position = positionCode.getPosition(date);
					if (position.isWorking())
						datePositions.put(position.getName(), position);
				} catch (ScriptException e) {
					e.printStackTrace();
				}
			}
		}
		
		// Creates the Agenda for the interval dates and corresponding positions
		this.agenda = new Agenda(intervalDate, positions);
	}

	public Agenda getAgenda() {
		return agenda;
	}

	public PlanningSolver generateSolver(final LinkedList<Solution> previousAcceptedSolutions) {
		// == 1 - SELECTS THE SHAKER ==
		// FINDS HOW MANY SOLUTIONS WE HAVE WITH THE SAME STANDARD DEVIATION OF WORKLOAD
		int idem = 1;
		double workSD = -1;
		
		// DESCENDS SOLUTIONS FROM LAST TO FIRST
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

		// 2 - GENERATES THE CONSTRAINTS SOLVER
		final Solver solver = new Solver();

		// FILLS THE SOLVER
		HashMap<LocalDate, HashMap<String, IntVar>> allIntVars;
		allIntVars = agenda.fillSolver(
				solver,
				physicians,
				previousAcceptedSolutions.size() == 0 ? null : previousAcceptedSolutions.getLast().lightenWorkBurden(idem));
		
		// GETS AN ARRAY OF EACH INTVAR
		LinkedList<IntVar> allDays = new LinkedList<>();
		for (HashMap<String, IntVar> oneDay : allIntVars.values()) {
			for (IntVar var : oneDay.values()) {
				allDays.add(var);
			}
		}
		
		IntVar[] allDaysArray = allDays.toArray(new IntVar[allDays.size()]);
		
		// USE A RANDOM CUSTOM SETTER
		solver.set(
				IntStrategyFactory.lastKConflicts(solver, 1000, IntStrategyFactory.custom(
				IntStrategyFactory.random_var_selector(new Date().getTime()), (IntValueSelector) new MyRandomStrategy(physicians), allDaysArray)));
		
		SearchMonitorFactory.limitTime(solver, 600000);
		
		// RETURN SOLVER AND VARS
		return new PlanningSolver(solver, agenda.getWorkingPositions(), allIntVars, previousAcceptedSolutions, physicians);
	}
	
}
