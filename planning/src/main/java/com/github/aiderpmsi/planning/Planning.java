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
	
	public Solution findSolution(
			LinkedList<Solution> previousAcceptedSolutions) throws SolutionException {
		// FINDS HOW MANY SOLUTIONS WE HAVE WITH IDEM MAX WORK INDICE
		int idem = 1;
		long maxWorks = -1;
		
		// DESCENDS SOLUTIONS FROM LAST TO FIRST
		Iterator<Solution> lastSolutions =  previousAcceptedSolutions.descendingIterator();
		while (lastSolutions.hasNext()) {
			Solution lastSolution = lastSolutions.next();
			if (maxWorks == -1 || lastSolution.getMaxWorkLoad() == maxWorks) {
				idem++;
				maxWorks = lastSolution.getMaxWorkLoad();
			} else {
				break;
			}
		}
		
		// GENERATES A NEW SOLVER FROM THE LAST SOLUTION (IF WE HAVE ONE)
		// THE MORE WE HAVE IDEM SOLUTIONS, THE MORE WE HAVE TO SHAKE THE SOLUTION
		Entry<Solver, HashMap<LocalDate, HashMap<String, IntVar>>> entry = generateSolver(
				previousAcceptedSolutions.size() != 0 ? previousAcceptedSolutions.getLast() : null, idem);
		
		// FINDS A SOLUTION
		entry.getKey().findSolution();
		
		// IF NO SOLUTION, RETRY IF A SOLUTION ALREADY EXISTS
		if (entry.getKey().isFeasible() != ESat.TRUE && previousAcceptedSolutions.size() == 0) {
				throw new SolutionException("No solution");
		} else {
			Solution solution = new Solution();
			solution.setPhysicians(physicians);
			solution.setWorkingPeriodsMap(getAgenda().getWorkingPeriods());
			solution.setSolutionMedIndicesMap(entry.getValue());
			// IF WE HAVE AT LEAST 1 SOLUTIONS IN SOLUTIONS LIST, COMPARE IT WITH THE PRECEDENT
			if (previousAcceptedSolutions.size() > 0 &&
					(solution.getMaxWorkLoad() - solution.getMinWorkLoad()) > (previousAcceptedSolutions.getLast().getMaxWorkLoad() - previousAcceptedSolutions.getLast().getMinWorkLoad())) {
					// REJECTED SOLUTION
					return null;
			}
			// IF WE ARE THERE, THIS SOLUTION IS BETTER THAN THE PRECEDENT
			else {
				return solution;
			}
		}
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
			try {
				Solution solution = planning.findSolution(solutions);

				System.out.println("Essai num " + i);
				
				if (solution != null) {
					System.out.println("Solution trouvée retenue");
					System.out.println("Max : " + solution.getMaxWorkLoad() + (solutions.size() > 0 ? (" (Best = " + solutions.getLast().getMaxWorkLoad() + ")") : "") );
					System.out.println("Min : " + solution.getMinWorkLoad() + (solutions.size() > 0 ? (" (Best = " + solutions.getLast().getMinWorkLoad() + ")") : "") );
					solutions.add(solution);
					prettyPrintInteger(solution.getSolutionMedIndicesMap(), physicians);
				} else if (solutions.size() != 0) {
					System.out.println("Last Max : " + solutions.getLast().getMaxWorkLoad());
					System.out.println("Last Min : " + solutions.getLast().getMinWorkLoad());
				}
			} catch (SolutionException e) {
				System.out.println("No solution");
				break;
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

	@FunctionalInterface
	public interface TemporaryNoSolutionCallback {
		public void callback();
	}
	
	@FunctionalInterface
	public interface NewSolutionCallback {
		public void callback(Solution solution, ArrayList<Physician> physicians);
	}
	
	@FunctionalInterface
	public interface NewSolutionSearchCallback {
		public void callback();
	}
}
