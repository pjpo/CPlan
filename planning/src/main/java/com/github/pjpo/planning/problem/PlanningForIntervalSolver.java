package com.github.pjpo.planning.problem;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.loop.monitors.SearchMonitorFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.ESat;

import com.github.pjpo.planning.model.Position;
import com.github.pjpo.planning.model.PositionConstraintBase;
import com.github.pjpo.planning.model.PositionConstraintRuleElement;
import com.github.pjpo.planning.model.PositionDifferentConstraint;
import com.github.pjpo.planning.model.PositionEqualConstraint;
import com.github.pjpo.planning.model.Worker;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table.Cell;

public class PlanningForIntervalSolver {
	
	/** Choco solver */
	private final Solver solver;
	
	/** Private HashTable for storing intvars from choco solver */
	private final HashBasedTable<LocalDate, String, Position> positions = HashBasedTable.create();
	
	/** Private Hashtable for storing positions depending on intvar (index) */
	private final HashMap<IntVar, Position> intVarPositions = new HashMap<>();
	
	/** Random number generator */
	private final Random random = new Random(new Date().getTime()); 
	
	// Defined Workers
	private final HashMap<Integer, Worker> physicians;
	
	public PlanningForIntervalSolver(
			final HashMap<Integer, Worker> physicians,
			final HashBasedTable<LocalDate, String, Position> positions,
			final List<PositionConstraintBase> positionsConstraints,
			final Solution previousSolution) {
		
		// inits fields
		this.physicians = physicians;
		
		// Creates choco solver
		this.solver = new Solver();
		
		// If a previous accepted solution exists, use it in order to clone the previous solution, else use the table
		// of positions as a reference
		// Clones the positions in order to modify them depending on the solver
		for (final Cell<LocalDate, String, Position> position : previousSolution != null ? previousSolution.getPositions().cellSet() : positions.cellSet()) {
			this.positions.put(position.getRowKey(), position.getColumnKey(), (Position) position.getValue().clone());
		}
		
		// If previous solutions exist, alters the positions cloned to adapt burden of work
		if (previousSolution != null) {
			// WorkLoad of last solution
			final double lastMeanWork = previousSolution.getMeanWorkLoad();
			
			// Sets the shaker indice randomly depends on (id solutions + 1)
			int randomInt = random.nextInt(10);
			int exponent = random.nextInt(10);
			int shaker = Double.valueOf(Math.pow(randomInt, exponent)).intValue() + 1;
			
			// lighten burden of work
			for (final Cell<LocalDate, String, Position> position : this.positions.cellSet()) {
				// If Worker is a pseudo worker, removes the physician
				if (position.getValue().getWorker().getInternalIndice() < 0) {
					position.getValue().setWorker(null);
				} else {
					// For this worker, see the number of mean workload for his workload
					final double numWorkLoads = previousSolution.getWorkLoad(position.getValue().getWorker()).doubleValue() / lastMeanWork;
					// Removes the physician depending on lastMeanWork and shaker
					double randomIndice = random.nextDouble() * shaker * numWorkLoads;
					if (randomIndice != 0) randomIndice = Math.sqrt(randomIndice);
					if (randomIndice >= 2) position.getValue().setWorker(null);
					System.out.println(randomIndice);
				}
			}
		}

		// For each position, give any possibility
		// The setting of the predefined employees... are done in the random strategy
		for (final Cell<LocalDate, String, Position> position : this.positions.cellSet()) {
			// If we have already a worker, use it and sets the IntVar as fixed
			if (position.getValue().getWorker() != null) {
				position.getValue().setInternalChocoRepresentation(
						VariableFactory.fixed(position.getColumnKey() + "_" + position.getRowKey(),
								position.getValue().getWorker().getInternalIndice(), solver));
			}
			// If no worker is defined, anyone can be defined at this position (depending on randomstrategy)
			else {
				position.getValue().setInternalChocoRepresentation(
						VariableFactory.bounded(position.getColumnKey() + "_" + position.getRowKey(), 0, Integer.MAX_VALUE - 1, solver));
			}
			
			// Indexes the positions depending on intvar
			intVarPositions.put(position.getValue().getInternalChocoRepresentation(), position.getValue());
		}
		
		
		// CREATES THE GENERAL CONSTRAINTS AND APPLY THEM TO THE SOLVER for each date
		final LinkedList<IntVar> internalPositions = new LinkedList<>();
		for (final LocalDate date : positions.rowKeySet()) {
			for (final PositionConstraintBase constraint : positionsConstraints) {
				// Clears list of IntVar
				internalPositions.clear();
				for (final PositionConstraintRuleElement element : constraint.getRuleElements()) {
					// Date of element selected
					final LocalDate targetDate = date.plusDays(element.getDeltaDays());
					// Targeted position
					final Position position = this.positions.get(targetDate, element.getPositionName());
					if (position != null) {
						// Internal Position (representation for Solver)
						final IntVar internalPosition = this.positions.get(targetDate, element.getPositionName()).getInternalChocoRepresentation();
						internalPositions.add(internalPosition);
					}
				}
				// Create the constraint 
				if (constraint instanceof PositionEqualConstraint) {
					IntVar previousElement = null;
					for (final IntVar element : internalPositions) {
						if (previousElement != null) {
							solver.post(
									IntConstraintFactory.arithm(previousElement, "=", element));
						}
						previousElement = element;
					}
				} else if (constraint instanceof PositionDifferentConstraint) {
					final IntVar[] intVars = new IntVar[internalPositions.size()];
					internalPositions.toArray(intVars);
					solver.post(IntConstraintFactory.alldifferent(intVars, "BC"));
				}
			}
		}
		
		// Sets the custom strategy
		final CPlanRandomStrategy cPlanRandomStrategy = new CPlanRandomStrategy(physicians, getIntVarPositions());
		// All IntVars
		final IntVar[] intVars = new IntVar[this.positions.size()];
		int i = 0;
		for (final Position position : this.positions.values()) {
			intVars[i] = position.getInternalChocoRepresentation();
			i++;
		}
		final IntStrategy strategy = 
				IntStrategyFactory.custom(
						IntStrategyFactory.random_var_selector(new Date().getTime()),
						cPlanRandomStrategy,
						intVars);
		// sets the strategy for the solver
		solver.set(IntStrategyFactory.lastKConflicts(solver, 100, strategy));
		// Limits the 
		SearchMonitorFactory.limitTime(solver, 600000);

	}
	
	public Solution findSolution() {

		// FINDS A SOLUTION
		solver.findSolution();
				
		// IF NO SOLUTION, RETRY IF A SOLUTION ALREADY EXISTS
		if (solver.isFeasible() != ESat.TRUE) {
			return null;
		} else {
			final Solution solution = new Solution(physicians, positions);
			return solution;
		}
	}

	public HashMap<IntVar, Position> getIntVarPositions() {
		return intVarPositions;
	}

}
