package com.github.aiderpmsi.planning;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Random;

import com.github.aiderpmsi.planning.jours.JourChuMtp;

import solver.ResolutionPolicy;
import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.IntStrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import util.ESat;

public class Planning {

	public static void main(String[] args) {

		// NUMBER OF GENERATED DAYS
		int DAYS = 120;
		// START DATE
		LocalDate START_DATE = LocalDate.of(2014, 1, 1);
		
		// DOCS
		HashMap<Integer, String> docs = new HashMap<>();
		docs.put(1, "Med 1");
		docs.put(2, "Med 2");
		docs.put(3, "Med_3");
		docs.put(4, "Med_4");
		docs.put(5, "Med_5");
		docs.put(6, "Med_6");
		docs.put(7, "Med_7");
		
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
		IntVar maxnbDays = VariableFactory.bounded("maxnbdays", 0, VariableFactory.MAX_INT_BOUND, solver);

		HashMap<Integer, IntVar> counts = new LinkedHashMap<>();
		HashMap<Integer, IntVar> scalars = new LinkedHashMap<>();
		for (Integer docIndice : docs.keySet()) {
			IntVar count = VariableFactory.bounded("count_" + docIndice, 0, VariableFactory.MAX_INT_BOUND, solver);
			IntVar scaledCount = VariableFactory.scale(count, 100);
			/** Scale vs temps de travail :
			 * cste = 100% = 100 (100000/1000)
			 * 90% = (10000/90) => 111
			 * 80% = (10000/80) => 125
			 * 10% = 10000
			 */
			solver.post(IntConstraintFactory.count(docIndice, allDaysArray, count));
			counts.put(docIndice, count);
			scalars.put(docIndice, scaledCount);
			solver.post(IntConstraintFactory.arithm(scaledCount, "<=", maxnbDays));
		}

		solver.set(IntStrategyFactory.lastKConflicts(solver, 100, IntStrategyFactory.random_value(allDaysArray, new Random().nextLong())));
		SearchMonitorFactory.limitTime(solver, 6000);
		solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, maxnbDays);
		
		if (solver.isFeasible() == ESat.TRUE) {
			System.out.println("Solution trouvée");
			System.out.println("Max : " + maxnbDays.getValue());
			for (Entry<Integer, IntVar> count : counts.entrySet()) {
				System.out.println(docs.get(count.getKey()) + " : charge = " + count.getValue().getValue());
			}
			for (Entry<LocalDate, HashMap<String, IntVar>> oneDay : allIntVarsBuffer.entrySet()) {
				System.out.println("date : " + oneDay.getKey());
				for (IntVar var : oneDay.getValue().values()) {
					System.out.println(var.getName() + " : " + docs.get(var.getValue()));
				}
			}
		} else {
			System.out.println("Pas de solution");
		}
		
	}
	
	// DEFINITION OF A DAY
	

}
