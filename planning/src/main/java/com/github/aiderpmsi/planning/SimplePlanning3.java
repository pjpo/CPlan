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

public class SimplePlanning3 {

	public static void main(String[] args) {
		
		int MEDS = 10;
		int DAYS = 80;
		
		Solver solver = new Solver("SimplePlan");
		
		IntVar max =
				VariableFactory.bounded("maxnbdays", 0, DAYS, solver);
		
		IntVar[] days = new IntVar[DAYS];

		IntVar[] count = new IntVar[MEDS];
		
		for (int i = 0 ; i < DAYS ; i++) {
			days[i] = VariableFactory.enumerated("day_" + i, 0, MEDS - 1, solver);
		}
		
		for (int i = 0 ; i < MEDS ; i++) {
			count[i] = VariableFactory.bounded("count_" + i, 0, DAYS, solver);
			solver.post(IntConstraintFactory.count(i, days, count[i]));
			solver.post(IntConstraintFactory.arithm(count[i], "<=", max));
		}
		
		solver.set(IntStrategyFactory.lastKConflicts(solver, 100, IntStrategyFactory.random_value(days, new Random().nextLong())));
		SearchMonitorFactory.limitTime(solver, 6000);
		solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, max);
		
		if (solver.isFeasible() == ESat.TRUE) {
			System.out.println("Solution trouvée");
			System.out.println("Max : " + max.getValue());
			for (IntVar day : days) {
				System.out.println(day.getName() + " : " + day.getValue());
			}
			for (IntVar singleCount : count) {
				System.out.println(singleCount.getName() + " : " + singleCount.getValue());
			}
		} else {
			System.out.println("Pas de solution");
		}
		
	}
	
	// DEFINITION OF A DAY
	

}
