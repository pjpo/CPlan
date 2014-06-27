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
		int DAYS = 30;
		// START DATE
		LocalDate START_DATE = LocalDate.of(2014, 1, 1);
		
		// PLANNING
		Planning planning = new Planning();
		planning.getPhysicians().add(new PhysicianBuilder().setName("Med1").setTimePart(100).toPhysician());
		planning.getPhysicians().add(new PhysicianBuilder().setName("Med2").setTimePart(95).toPhysician());
		planning.getPhysicians().add(new PhysicianBuilder().setName("Med3").setTimePart(90).toPhysician());
		planning.getPhysicians().add(new PhysicianBuilder().setName("Med4").setTimePart(85).toPhysician());
		planning.getPhysicians().add(new PhysicianBuilder().setName("Med5").setTimePart(80).toPhysician());
		planning.getPhysicians().add(new PhysicianBuilder().setName("Med6").setTimePart(75).toPhysician());
		planning.getPhysicians().add(new PhysicianBuilder().setName("Med7").setTimePart(70).toPhysician());
		planning.getPhysicians().add(new PhysicianBuilder().setName("Med8").setTimePart(65).toPhysician());
		planning.getPhysicians().add(new PhysicianBuilder().setName("Med9").setTimePart(60).toPhysician());
		planning.getPhysicians().add(new PhysicianBuilder().setName("Med10").setTimePart(55).toPhysician());
		planning.getPhysicians().add(new PhysicianBuilder().setName("Med11").setTimePart(55).toPhysician());
	
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
			// WE SCALE THE NUMBER OF WORKING PLANNINGS IN ORDER TO TAKE CARE OF PART TIME WORKING
			// BUT THIS MEANS WE CAN'T TAKE INTO ACCOUNT MORE THAN 2147 WORKING PLAGES PER WORKER
			Physician physician = planning.getPhysicians().get(i);
			
			// COUNTS NUMBER OF WORKS
			IntVar count = VariableFactory.bounded("count_" + i, 0, VariableFactory.MAX_INT_BOUND / 10000, solver);
			solver.post(IntConstraintFactory.count(i, allDaysArray, count));
			counts.add(count);

			// SCLAES THIS NUMBER OF WORKS
			/** Scale vs temps de travail :
			 * cste = 100% = 100 (100000/1000)
			 * 90% = (10000/90) => 111
			 * 80% = (10000/80) => 125
			 * 10% => 1000
			 * 1% => 10000
			 */
			int scale = 10000 / physician.getTimePart();
			IntVar scaledCount = VariableFactory.scale(count, scale);
			solver.post(IntConstraintFactory.arithm(scaledCount, "<=", maxnbDays));
			scalars.add(scaledCount);
		}

		//solver.set(IntStrategyFactory.lastKConflicts(solver, 100, IntStrategyFactory.));
		SearchMonitorFactory.limitTime(solver, 12000000);
		solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, maxnbDays);
		
		if (solver.isFeasible() == ESat.TRUE) {
			System.out.println("Solution trouvée");
			System.out.println("Max : " + maxnbDays.getValue());
			for (int i = 0 ; i < planning.getPhysicians().size() ; i++) {
				System.out.println(planning.getPhysicians().get(i).getName() + " : charge = " +
						counts.get(i).getValue());
				System.out.println(planning.getPhysicians().get(i).getName() + " : scaled count = " +
						scalars.get(i).getValue());
			}
			for (Entry<LocalDate, HashMap<String, IntVar>> oneDay : allIntVarsBuffer.entrySet()) {
				System.out.println("date : " + oneDay.getKey());
				for (IntVar var : oneDay.getValue().values()) {
					System.out.println(var.getName() + " : " + planning.getPhysicians().get(var.getValue()).getName());
				}
			}
		} else {
			System.out.println("Pas de solution");
		}
		
	}
	
	// DEFINITION OF A DAY
	

}
