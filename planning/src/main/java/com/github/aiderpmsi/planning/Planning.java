package com.github.aiderpmsi.planning;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

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
		
		// USE A RANDOM SETTER
		solver.set(IntStrategyFactory.random_value(allDaysArray));

		// FINDS A SOLUTION
		solver.findSolution();

		if (solver.isFeasible() == ESat.TRUE) {
			// STORES THIS SOLUTION
			Solution solution = new Solution();
			solution.setPhysicians(planning.getPhysicians());
			solution.setWorkingPeriodsMap(agenda.getPlagesBuffer());
			solution.setSolutionMedIndicesMap(allIntVarsBuffer);
			
			System.out.println("Solution trouvée");
			System.out.println("Max : " + solution.getMaxWorkLoad());
			System.out.println("Min : " + solution.getMinWorkLoad());
			for (int i = 0 ; i < planning.getPhysicians().size() ; i++) {
				System.out.println(planning.getPhysicians().get(i).getName() + " : charge = " +
						solution.getWorkLoads().get(i));
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
