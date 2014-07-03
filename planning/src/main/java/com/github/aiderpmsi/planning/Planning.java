package com.github.aiderpmsi.planning;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import solver.Solver;
import solver.search.strategy.IntStrategyFactory;
import solver.search.strategy.selectors.IntValueSelector;
import solver.variables.IntVar;
import util.ESat;

import com.github.aiderpmsi.planning.jours.Agenda;
import com.github.aiderpmsi.planning.jours.Jour;
import com.github.aiderpmsi.planning.jours.JourChuMtp;
import com.github.aiderpmsi.planning.physician.Physician;
import com.github.aiderpmsi.planning.physician.PhysicianBuilder;

public class Planning {

	private ArrayList<Physician> physicians;
	private Agenda agenda;
	
	public Planning(LocalDate startDate, LocalDate endDate, ArrayList<Physician> physicians, Jour typeJour) {
		this.physicians = physicians;
		this.agenda = new Agenda(typeJour);
		this.agenda.setBounds(startDate, endDate);
		this.agenda.calculateWorkingPeriods();
	}

	public Agenda getAgenda() {
		return agenda;
	}
	
	public Entry<Solver, HashMap<LocalDate, HashMap<String, IntVar>>> generateSolver(Solution solution, int shake) {
		Solver solver = new Solver();

		// FILLS THE SOLVER
		HashMap<LocalDate, HashMap<String, IntVar>> allIntVars;
		if (solution != null)
			 allIntVars = agenda.fillSolver(solver, physicians, solution.lightenWorkBurden(shake));
		else
			 allIntVars = agenda.fillSolver(solver, physicians);
		
		// GETS AN ARRAY OF EACH INTVAR
		LinkedList<IntVar> allDays = new LinkedList<>();
		for (HashMap<String, IntVar> oneDay : allIntVars.values()) {
			for (IntVar var : oneDay.values()) {
				allDays.add(var);
			}
		}
		
		IntVar[] allDaysArray = allDays.toArray(new IntVar[allDays.size()]);
		
		// USE A RANDOM CUSTOM SETTER
		solver.set(IntStrategyFactory.custom(
				IntStrategyFactory.random_var_selector(new Date().getTime()), (IntValueSelector) new MyRandomStrategy(physicians), allDaysArray));
		
		// RETURN SOLVER AND VARS
		return new Entry<Solver, HashMap<LocalDate, HashMap<String, IntVar>>>() {
			@Override
			public Solver getKey() { return solver;}
			@Override
			public HashMap<LocalDate, HashMap<String, IntVar>> getValue() {
				return allIntVars;
			}
			@Override
			public HashMap<LocalDate, HashMap<String, IntVar>> setValue(
					HashMap<LocalDate, HashMap<String, IntVar>> value) {
				throw new IllegalArgumentException("Not modifiable object");
			}
			
		};
	}

	public static void main(String[] args) throws IOException {

		// NUMBER OF GENERATED DAYS
		int DAYS = 120;
		// START DATE
		LocalDate START_DATE = LocalDate.of(2014, 1, 1);
		
		// PLANNING
		ArrayList<Physician> physicians = new ArrayList<>();
		physicians.add(new PhysicianBuilder().setName("Med1").setTimePart(100).toPhysician());
		physicians.add(new PhysicianBuilder().setName("Med2").setTimePart(95).toPhysician());
		physicians.add(new PhysicianBuilder().setName("Med3").setTimePart(90).toPhysician());
		physicians.add(new PhysicianBuilder().setName("Med4").setTimePart(85).toPhysician());
		physicians.add(new PhysicianBuilder().setName("Med5").setTimePart(80).toPhysician());
		physicians.add(new PhysicianBuilder().setName("Med6").setTimePart(75).toPhysician());
		physicians.add(new PhysicianBuilder().setName("Med7").setTimePart(70).toPhysician());
		physicians.add(new PhysicianBuilder().setName("Med8").setTimePart(65).toPhysician());
		physicians.add(new PhysicianBuilder().setName("Med9").setTimePart(60).toPhysician());
		physicians.add(new PhysicianBuilder().setName("Med10").setTimePart(55).toPhysician());
		physicians.add(new PhysicianBuilder().setName("Med11").setTimePart(55).toPhysician());
	
		// PLANNING
		Planning planning = new Planning(START_DATE, START_DATE.plusDays(DAYS), physicians, new JourChuMtp());

		// LIST OF SOLUTIONS
		LinkedList<Solution> solutions = new LinkedList<>();
		
		// GENERATES A PLANNING SOLVER
		for (int i = 0 ; i < 1000000 ; i++) {
			// TELL HOW MANY IDEM SOLUTIONS WE HAVE
			int idem = 1;
			long maxWorks = -1;
			Iterator<Solution> lastSolutions =  solutions.descendingIterator();
			
			while (lastSolutions.hasNext()) {
				Solution lastSolution = lastSolutions.next();
				if (maxWorks == -1 || lastSolution.getMaxWorkLoad() == maxWorks) {
					idem++;
					maxWorks = lastSolution.getMaxWorkLoad();
				} else {
					break;
				}
			}

			// THE MORe WE HAVE IDEM SOLUTIONS, THE MORE WE HAVE TO SHAKE THE SOLUTION
			Entry<Solver, HashMap<LocalDate, HashMap<String, IntVar>>> entry = planning.generateSolver(
					solutions.size() != 0 ? solutions.getLast() : null, idem);
		
			// FINDS A SOLUTION
			entry.getKey().findSolution();
			
			// IF NO SOLUTION, RETRY IF A SOLUTION ALREADY EXISTS
			if (entry.getKey().isFeasible() != ESat.TRUE) {
				if (solutions.size() == 0)
					throw new IOException("No solution");
				else {
					System.out.println("Temporary no solution");
				}
			} else {
				Solution solution = new Solution();
				solution.setPhysicians(physicians);
				solution.setWorkingPeriodsMap(planning.getAgenda().getWorkingPeriods());
				solution.setSolutionMedIndicesMap(entry.getValue());
				// IF WE HAVE AT LEAST 1 SOLUTIONS IN SOLUTIONS LIST, COMPARE IT WITH THE PRECEDENT
				if (solutions.size() > 0 &&
						(solution.getMaxWorkLoad() - solution.getMinWorkLoad()) > (solutions.getLast().getMaxWorkLoad() - solutions.getLast().getMinWorkLoad())) {
						// REJECTED SOLUTION
						System.out.println("Solution trouvée non retenue");
						System.out.println("Max : " + solution.getMaxWorkLoad() + (solutions.size() > 0 ? (" (Best = " + solutions.getLast().getMaxWorkLoad() + ")") : "") );
						System.out.println("Min : " + solution.getMinWorkLoad() + (solutions.size() > 0 ? (" (Best = " + solutions.getLast().getMinWorkLoad() + ")") : "") );
				}
				// TELL HOW MANY IDEM SOLUTIONS WE HAVE
				else {
					System.out.println("Solution trouvée retenue");
					System.out.println("Max : " + solution.getMaxWorkLoad() + (solutions.size() > 0 ? (" (Best = " + solutions.getLast().getMaxWorkLoad() + ")") : "") );
					System.out.println("Min : " + solution.getMinWorkLoad() + (solutions.size() > 0 ? (" (Best = " + solutions.getLast().getMinWorkLoad() + ")") : "") );
					solutions.add(solution);
					prettyPrintIntVar(entry.getValue(), physicians);
				}
			}
		}
		
	}
	
	public static void prettyPrintIntVar(HashMap<LocalDate, HashMap<String, IntVar>> solution, ArrayList<Physician> physicians) {
		for (Entry<LocalDate, HashMap<String, IntVar>> oneDay : solution.entrySet()) {
			System.out.println("date : " + oneDay.getKey());
			for (IntVar var : oneDay.getValue().values()) {
				System.out.println(var.getName() + " : " + physicians.get(var.getValue()).getName());
			}
		}
	}		
		
	public static void prettyPrintInteger(HashMap<LocalDate, HashMap<String, Integer>> solution, ArrayList<Physician> physicians) {
		for (Entry<LocalDate, HashMap<String, Integer>> oneDay : solution.entrySet()) {
			System.out.println("date : " + oneDay.getKey());
			for (Entry<String, Integer> var : oneDay.getValue().entrySet()) {
				System.out.println(var.getKey() + " : " + (var.getValue() == null ? "null" : var.getValue()));
			}
		}
	}		

}
