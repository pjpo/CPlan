package com.github.aiderpmsi.planning;

import solver.ResolutionPolicy;
import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import util.ESat;

public class SimplePlanning2 {

	public static int DAYS = 90;
	
	public static int MEDS = 3;

	public static void main(String[] args) {
		Solver solver = new Solver("SimplePlan");
		
		BoolVar[][] days = new BoolVar[MEDS][DAYS];

		IntVar counts[] = new IntVar[MEDS];
		
		IntVar maxs = VariableFactory.bounded("Charge", 0, DAYS, solver);

		IntVar ONE = VariableFactory.fixed(1, solver);
		
		// DEFINE MAXIMUM FOR EACH DAY FOR EACH MED
		for (int i = 0 ; i < MEDS ; i++) {
			counts[i] = VariableFactory.bounded("Count Med " + i, 0, DAYS, solver);
			for (int j = 0 ; j < DAYS ; j++) {
				days[i][j] = VariableFactory.bool("M_" + i + "_D_" + j, solver);
			}
			solver.post(IntConstraintFactory.sum(days[i], counts[i]));
			solver.post(IntConstraintFactory.arithm(counts[i], "<=", maxs));
		}
		
		// DEFINE THAT EACH DAY MUST BE FILLED ONLY ONCE
		for (int i = 0 ; i < DAYS ; i++) {
			BoolVar[] day = new BoolVar[MEDS];
			for (int j = 0 ; j < MEDS ; j++) {
				day[j] = days[j][i];
			}
			solver.post(IntConstraintFactory.sum(day, ONE));
		}
		
		//solver.set(IntStrategyFactory.lastKConflicts(solver, 3, IntStrategyFactory.random_value(days, new Random().nextLong())));
		//solver.findSolution();
		SearchMonitorFactory.limitTime(solver, 6000);
		solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, maxs);
		
		if (solver.isFeasible() == ESat.TRUE) {
			System.out.println("Solution trouvée");
			System.out.println("Max : " + maxs.getValue());
			for (BoolVar[] medDays : days) {
				for (BoolVar day : medDays) {
					System.out.println(day);
				}
			}
		} else {
			System.out.println("Pas de solution");
		}
		
	}

}
