package com.github.aiderpmsi.planning;

import java.time.LocalDate;
import java.util.ArrayList;
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

import com.github.aiderpmsi.planning.jours.JourChuMtp;
import com.github.aiderpmsi.planning.physician.Physician;
import com.github.aiderpmsi.planning.physician.PhysicianBuilder;

public class Planning {

	private ArrayList<Physician> physicians = new ArrayList<>();
	
	public ArrayList<Physician> getPhysicians() {
		return physicians;
	}
	
	public static void main(String[] args) {

		// NUMBER OF GENERATED DAYS
		int DAYS = 120;
		// START DATE
		LocalDate START_DATE = LocalDate.of(2014, 1, 1);
		
		// PLANNING
		Planning planning = new Planning();
		planning.getPhysicians().add(new PhysicianBuilder().setName("Med1").setTimePart(100).toPhysician());
		planning.getPhysicians().add(new PhysicianBuilder().setName("Med2").setTimePart(90).toPhysician());
		planning.getPhysicians().add(new PhysicianBuilder().setName("Med3").setTimePart(80).toPhysician());
		planning.getPhysicians().add(new PhysicianBuilder().setName("Med4").setTimePart(70).toPhysician());
		planning.getPhysicians().add(new PhysicianBuilder().setName("Med5").setTimePart(60).toPhysician());
		planning.getPhysicians().add(new PhysicianBuilder().setName("Med6").setTimePart(50).toPhysician());
		planning.getPhysicians().add(new PhysicianBuilder().setName("Med7").setTimePart(40).toPhysician());
		planning.getPhysicians().add(new PhysicianBuilder().setName("Med8").setTimePart(30).toPhysician());
	
		// SOLVER
		Solver solver = new Solver("SimplePlan");
		
		// AGENDA GENERATOR
		JourChuMtp agenda = new JourChuMtp(planning.getPhysicians());
		
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

		ArrayList<IntVar> counts = new ArrayList<>(planning.getPhysicians().size());
		ArrayList<IntVar> scalars = new ArrayList<>(planning.getPhysicians().size());
		for (int i = 0 ; i < planning.getPhysicians().size() ; i++) {
			Physician physician = planning.getPhysicians().get(i);
			IntVar count = VariableFactory.bounded("count_" + i, 0, VariableFactory.MAX_INT_BOUND, solver);
			IntVar scaledCount = VariableFactory.scale(count, 100000/physician.getTimePart());
			/** Scale vs temps de travail :
			 * cste = 100% = 100 (100000/1000)
			 * 90% = (10000/90) => 111
			 * 80% = (10000/80) => 125
			 * 10% = 10000
			 */
			solver.post(IntConstraintFactory.count(i, allDaysArray, count));
			counts.add(count);
			scalars.add(scaledCount);
			solver.post(IntConstraintFactory.arithm(scaledCount, "<=", maxnbDays));
		}

		solver.set(IntStrategyFactory.lastKConflicts(solver, 100, IntStrategyFactory.random_value(allDaysArray, new Random().nextLong())));
		SearchMonitorFactory.limitTime(solver, 60000);
		solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, maxnbDays);
		
		if (solver.isFeasible() == ESat.TRUE) {
			System.out.println("Solution trouvée");
			System.out.println("Max : " + maxnbDays.getValue());
			for (int i = 0 ; i < planning.getPhysicians().size() ; i++) {
				System.out.println(planning.getPhysicians().get(i).getName() + " : charge = " +
						counts.get(i).getValue());
			}
			for (Entry<LocalDate, HashMap<String, IntVar>> oneDay : allIntVarsBuffer.entrySet()) {
				System.out.println("date : " + oneDay.getKey());
				for (IntVar var : oneDay.getValue().values()) {
					System.out.println(var.getName() + " : " + planning.getPhysicians().get(var.getValue()));
				}
			}
		} else {
			System.out.println("Pas de solution");
		}
		
	}
	
	// DEFINITION OF A DAY
	

}
