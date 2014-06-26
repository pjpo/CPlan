package com.github.aiderpmsi.planning;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
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

		// NUMBER OF GENERATED DAYS
		int DAYS = 10;
		// START DATE
		LocalDate START_DATE = LocalDate.of(2014, 1, 1);
		
		// DOCS
		HashMap<Integer, String> docs = new HashMap<>();
		docs.put(1, "Med 1");
		docs.put(2, "Med 2");
		docs.put(3, "Med_3");
		
		// SOLVER
		Solver solver = new Solver("SimplePlan");
		
		// AGENDA GENERATOR
		JourChuMtp agenda = new JourChuMtp(docs);
		
		// GENERATE INTVARS FOR EACH DAY
		for (LocalDate date = START_DATE ; date.isBefore(START_DATE.plusDays(DAYS)) ; date = date.plusDays(1)) {
			agenda.generatePlages(date, solver);
		}
		
		// GENERATE CONSTRAINTS FOR EACH DAY
		for (LocalDate date = START_DATE ; date.isBefore(START_DATE.plusDays(DAYS)) ; date = date.plusDays(1)) {
			agenda.generateConstraints(date, solver);
		}

		// GETS AN ARRAY OF EACH INTVAR
		LinkedList<IntVar> allDays = new LinkedList<>();
		HashMap<LocalDate, HashMap<String, IntVar>> allIntVarsBuffer = agenda.getIntVarsBuffer();
		for (HashMap<String, IntVar> oneDay : allIntVarsBuffer.values()) {
			for (IntVar var : oneDay.values()) {
				allDays.add(var);
			}
		}
		IntVar[] allDaysArray = allDays.toArray(new IntVar[allDays.size()]);
		
		// NOW, SELECT MAX NB DAYS FOR EACH DOC
		IntVar maxnbDays = VariableFactory.bounded("maxnbdays", 0, DAYS * 2, solver);

		for (Integer docIndice : docs.keySet()) {
			IntVar count = VariableFactory.bounded("count_" + docIndice, 0, DAYS * 2, solver);
			solver.post(IntConstraintFactory.count(docIndice, allDaysArray, count));
			solver.post(IntConstraintFactory.arithm(count, "<=", maxnbDays));
		}

		solver.set(IntStrategyFactory.lastKConflicts(solver, 100, IntStrategyFactory.random_value(allDaysArray, new Random().nextLong())));
		SearchMonitorFactory.limitTime(solver, 6000);
		solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, maxnbDays);
		
		if (solver.isFeasible() == ESat.TRUE) {
			System.out.println("Solution trouvée");
			System.out.println("Max : " + maxnbDays.getValue());
			for (Entry<LocalDate, HashMap<String, IntVar>> oneDay : allIntVarsBuffer.entrySet()) {
				System.out.println("date : " + oneDay.getKey());
				for (IntVar var : oneDay.getValue().values()) {
					System.out.println(var.getName() + " : " + var.getValue());
				}
			}
		} else {
			System.out.println("Pas de solution");
		}
		
	}
	
	// DEFINITION OF A DAY
	

}
