package com.github.aiderpmsi.planning;

import java.util.Random;

import solver.ResolutionPolicy;
import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.IntStrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import util.ESat;

public class SimplePlanning {

	public static void main(String[] args) {
		
		int MEDS = 30;
		int DAYS = 360;
		int LINES = 7;
		
		Solver solver = new Solver("SimplePlan");
		
		IntVar max =
				VariableFactory.bounded("maxnbdays", 0, DAYS, solver);
		
		IntVar[][] days = new IntVar[DAYS][LINES];

		IntVar[] count = new IntVar[MEDS];
		
		for (int dayNb = 0 ; dayNb < DAYS ; dayNb++) {

			for (int lineNb = 0 ; lineNb < LINES ; lineNb++) {
		
				days[dayNb][lineNb] = VariableFactory.enumerated("day_" + dayNb + "_line_" + lineNb, 0, MEDS - 1, solver);

			}
			
			// IT IS THE FIRST DAY
			if (dayNb == 0) {
				IntVar[] different = new IntVar[LINES];
				for (int lineNb = 0 ; lineNb < LINES ; lineNb++) {
					different[lineNb] = days[dayNb][lineNb];
				}
				solver.post(IntConstraintFactory.alldifferent(different));
			}
			// IF IT IS A DAY, DO NOT ACCEPT A WORK THE NIGHT BEFORE
			else if (dayNb > 0 && (dayNb & 1) == 0 ) {
				IntVar[] different = new IntVar[LINES * 2];
				for (int lineNb = 0 ; lineNb < LINES ; lineNb++) {
					different[lineNb * 2] = days[dayNb][lineNb];
					different[lineNb * 2 + 1] = days[dayNb - 1][lineNb];
				}
				solver.post(IntConstraintFactory.alldifferent(different));
			}
			// IF IT IS A NIGHT, DO NOT ACCEPT A WORK THE NIGHT BEFOER
			else if (dayNb > 1 && (dayNb & 1) != 0) {
				IntVar[] different = new IntVar[LINES * 2];
				for (int lineNb = 0 ; lineNb < LINES ; lineNb++) {
					different[lineNb * 2] = days[dayNb][lineNb];
					different[lineNb * 2 + 1] = days[dayNb - 2][lineNb];
				}
				solver.post(IntConstraintFactory.alldifferent(different));
			}
		}

		IntVar[] allvacs = concat(new IntVar[LINES * DAYS], days);
		
		for (int medNb = 0 ; medNb < MEDS ; medNb++) {
			count[medNb] = VariableFactory.bounded("count_" + medNb, 0, DAYS, solver);
			solver.post(IntConstraintFactory.count(medNb, allvacs, count[medNb]));
		}
		solver.post(IntConstraintFactory.maximum(max, count));
		
		solver.set(IntStrategyFactory.lastKConflicts(solver, 100, IntStrategyFactory.random_value(allvacs, new Random().nextLong())));
		SearchMonitorFactory.limitTime(solver, 6000);
		solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, max);
		
		if (solver.isFeasible() == ESat.TRUE) {
			System.out.println("Solution trouvée");
			System.out.println("Max : " + max.getValue());
			for (IntVar[] day : days) {
				for (IntVar line : day) {
					System.out.println(line.getName() + " : " + line.getValue());
				}
			}
			for (IntVar singleCount : count) {
				System.out.println(singleCount.getName() + " : " + singleCount.getValue());
			}
		} else {
			System.out.println("Pas de solution");
		}
		
	}

	
	@SafeVarargs
	public static <T> T[] concat(T[] dest, T[] ... ts) {
		int length = 0;
		for (T[] t : ts) {
			length += t.length;
		}
		if (dest.length != length)
			throw new IllegalArgumentException("dest has not the required size");

		int position = 0;
		for (T[] t : ts) {
			System.arraycopy(t, 0, dest, position, t.length);
			position += t.length;
		}
		
		return dest;
	}
}
