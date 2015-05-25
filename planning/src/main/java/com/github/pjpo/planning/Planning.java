package com.github.pjpo.planning;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.loop.monitors.SearchMonitorFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.selectors.IntValueSelector;
import org.chocosolver.solver.variables.IntVar;

import com.github.pjpo.planning.jours.Agenda;
import com.github.pjpo.planning.physician.Physician;
import com.github.pjpo.planning.utils.IntervalDate;

/**
 * Classe principale de planning
 * @author jpc
 *
 */
public class Planning {

	/** List of used physicians */
	private final ArrayList<Physician> physicians;
	
	/** Agenda */
	private final Agenda agenda;
	
	private final Random random = new Random(new Date().getTime());
	
	public Planning(final IntervalDate intervalDate, final ArrayList<Physician> physicians) {
		this.physicians = physicians;
		this.agenda = new Agenda(intervalDate);
		this.agenda.calculateWorkingPeriods();
	}

	public Agenda getAgenda() {
		return agenda;
	}

	public PlanningSolver generateSolver(final LinkedList<Solution> previousAcceptedSolutions) {
		// == 1 - SELECTS THE SHAKER ==
		// FINDS HOW MANY SOLUTIONS WE HAVE WITH IDEM MAX WORK INDICE
		int idem = 1;
		long maxWorks = -1;
		
		// DESCENDS SOLUTIONS FROM LAST TO FIRST
		final Iterator<Solution> lastSolutions =  previousAcceptedSolutions.descendingIterator();
		while (lastSolutions.hasNext()) {
			final Solution lastSolution = lastSolutions.next();
			if (maxWorks == -1 || lastSolution.getMaxWorkLoad() == maxWorks) {
				idem++;
				maxWorks = lastSolution.getMaxWorkLoad();
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
		allIntVars = agenda.fillSolver(solver, physicians, previousAcceptedSolutions.size() == 0 ? null : previousAcceptedSolutions.getLast().lightenWorkBurden(idem));
		
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
