package com.github.aiderpmsi.planning;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.search.strategy.IntStrategyFactory;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.VariableFactory;

public class Planning {

	/**
	 * Copie de tous les champs de la vue de infocentre avec application de processus 
	 * @param args
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 */
	public static void main(String[] args) {
		// 1 . DEFINES LIST OF DOCS
		HashMap<Integer, String> docs = new HashMap<>();
		docs.put(1, "Med1");
		docs.put(2, "Med2");
		docs.put(3, "Med3");
		//docs.put(4, "Med4");
		//docs.put(5, "Med5");
		//docs.put(6, "Med6");
		//docs.put(7, "Med7");
		//docs.put(8, "Med8");

		// 2. DEFINES LIST OF POSSIBILITIES
		HashMap<String, HashSet<String>> possibilities = new HashMap<>();
		// MED 1
		{
			HashSet<String> medPoss = new HashSet<>();
			medPoss.add("D_2_1_FMC");
			possibilities.put("Med1", medPoss);
		}

		// 3 . DEFINES LIST OF IMPOSSIBILITIES
		HashMap<String, HashSet<String>> impossibilities = new HashMap<>();
		// MED 2
		{
			HashSet<String> medImposs = new HashSet<>();
			medImposs.add("D_3_0");
			medImposs.add("D_3_1");
			medImposs.add("D_4_0");
			medImposs.add("D_4_1");
			medImposs.add("D_5_0");
			medImposs.add("D_5_1");
			impossibilities.put("Med2", medImposs);
		}

		for (int i = 10 ; i < 1000 ; i++) {

			Solver solver = new Solver("MedPlans");
			
			List<?>[] charges = generateSolver(solver, docs, possibilities, impossibilities);
			
			IntVar maxCharge = VariableFactory.fixed("Charge", i, solver);
			
			solver.post(IntConstraintFactory.maximum(maxCharge, charges[1].toArray(new IntVar[charges[1].size()])));
		
			List<IntVar> allallVars = new ArrayList<>(charges[0].size() + charges[1].size());
			for (List<?> list : charges) {
				for (Object value : list) {
					if (value instanceof IntVar) {
						IntVar cValue = (IntVar) value;
						allallVars.add(cValue);
					}
				}
			}
			
			solver.set(IntStrategyFactory.lastConflict(solver, IntStrategyFactory.random_bound(allallVars.toArray(new IntVar[allallVars.size()]))));
			
			System.out.println("Solving");
			
			if (solver.findSolution()) {
				System.out.println("Solution trouvée : ");
				System.out.println("Charge : " + i);
				for (Variable var : solver.getVars()) {
					IntVar intVar = (IntVar) var;
					if (var.getName().startsWith("D")) {
						System.out.println(intVar.getName() + " : " + docs.get(intVar.getValue()));
					} else {
						System.out.println(intVar.getName() + " : " + intVar.getValue());
					}
				}
				break;
			} else {
				System.out.println("No solution for charge = " + i);
			}
			
		}
				
	}
	
	public static final List<?>[] generateSolver(Solver solver, HashMap<Integer, String> docs,
			HashMap<String, HashSet<String>> possibilities, HashMap<String, HashSet<String>> impossibilities) {

		// 2. CREATES THE PLACES IN AGENDA
		List<List<List<IntVar>>> agenda = new ArrayList<>();
		
		int agendaSize = 0;
		
		// GENERATES A PLAN FOR EACH DAY
		for (int day = 0 ; day < 15 ; day++) {
			// CREATES THE LIST OF PLANNINGS FOR THIS DAY
			List<List<IntVar>> dayplans = new ArrayList<>();
			
			// FOR EACH DAY, GENERATES :
			// - FMC
			List<IntVar> dayFmc = new ArrayList<>();
			for (int i = 0 ; i < 2 ; i++) {
				String id = "D_" + day + "_" + i + "_FMC";
				dayFmc.add(generatePossibleVars(solver, id, docs, possibilities, impossibilities));
				agendaSize++;
			}
			dayplans.add(dayFmc);

			// - SI
			List<IntVar> daySi = new ArrayList<>();
			for (int i = 0 ; i < 2 ; i++) {
				String id = "D_" + day + "_" + i + "_SI";
				daySi.add(generatePossibleVars(solver, id, docs, possibilities, impossibilities));
				agendaSize++;
			}
			dayplans.add(daySi);
			
			agenda.add(dayplans);
			
			// ADDS A CONSTRAINT THAT NOBODY CAN BE AT TWO POSITIONS AT THE SAME TIME
			if (day == 0) {
				solver.post(IntConstraintFactory.alldifferent(new IntVar[] {dayFmc.get(0), daySi.get(0)}));
				solver.post(IntConstraintFactory.alldifferent(new IntVar[] {dayFmc.get(1), daySi.get(1)}));
			}
			// ADDS A CONSTRAINT THAT WE CAN'T WORK NIGHT OR DAY OF WE WERE ON NIGHT DAY BEFORE
			else if (day != 0) {
				List<IntVar> dayFmcBefore = agenda.get(day - 1).get(0);
				List<IntVar> daySiBefore = agenda.get(day - 1).get(1);
				solver.post(IntConstraintFactory.alldifferent(new IntVar[] {dayFmc.get(0), dayFmcBefore.get(1), daySi.get(0), daySiBefore.get(1)}));
				solver.post(IntConstraintFactory.alldifferent(new IntVar[] {dayFmc.get(1), dayFmcBefore.get(1), daySi.get(1), daySiBefore.get(1)}));
			}
		}

		
		// 4. Define the search strategy
		List<IntVar> allVars = new ArrayList<>(agendaSize);
		for (List<List<IntVar>> day : agenda) {
			for (List<IntVar> poste : day) {
				for (IntVar vac : poste) {
					allVars.add(vac);
				}
			}
		}
		
		
		// GENERATES A CONSTRAINT ON NUMBER OF VACS FOR EACH DOC
		List<IntVar> chargeList = new ArrayList<>();
		for (Entry<Integer, String> doc : docs.entrySet()) {
			// DEFINES THE CHARGE OF WORK
			IntVar charge = VariableFactory.bounded("Charge_" + doc.getValue(), 0, 100000, solver);
			chargeList.add(charge);
			solver.post(IntConstraintFactory.count(doc.getKey(), allVars.toArray(new IntVar[allVars.size()]), charge));
		}

		List<?>[] intVars = new List[2];
		intVars[0] = allVars;
		intVars[1] = chargeList;
		
		return intVars;
	}

	public static final IntVar generatePossibleVars(final Solver solver,
			final String id,
			final HashMap<Integer, String> docs,
			final HashMap<String, HashSet<String>> possibilities,
			final HashMap<String, HashSet<String>> impossibilities) {

		IntVar var = null;
		// CHECKS THE POSSIBILITIES
		for (Entry<Integer, String> entry : docs.entrySet()) {
			if (possibilities.containsKey(entry.getValue()) && possibilities.get(entry.getValue()).contains(id)) {
				var = VariableFactory.fixed(id, entry.getKey(), solver);
				 break;
			}
		}
		
		// IF THERE WAS NO POSSIBILITY, CHECKS THE IMPOSSIBILITIES
		if (var == null) {
			HashSet<Integer> medsPoss= new HashSet<>(docs.keySet());
			for (Entry<Integer, String> entry : docs.entrySet()) {
				if (impossibilities.containsKey(entry.getValue())) {
					for (String impossibility : impossibilities.get(entry.getValue())) {
						if (id.startsWith(impossibility)) {
							medsPoss.remove(entry.getKey());
						}
					}
				}
			}
			Integer[] possibleVars = medsPoss.toArray(new Integer[medsPoss.size()]);
			int[] intPossibleVars = new int[possibleVars.length];
			for (int j = 0 ; j < possibleVars.length ; j++) {
				intPossibleVars[j] = possibleVars[j];
			}
			var = VariableFactory.enumerated(id, intPossibleVars, solver);
		}
		
		return var;
	}
	
}
