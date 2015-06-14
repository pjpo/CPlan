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
	
	/** Previous workloadSds for previous solutions */
	private final LinkedList<Double> previousWordLoadSDs;
	
	// Defined Workers
	private final HashMap<Integer, Worker> physicians;
	
	public PlanningForIntervalSolver(
			final HashMap<Integer, Worker> physicians,
			final HashBasedTable<LocalDate, String, Position> positions,
			final List<PositionConstraintBase> positionsConstraints,
			final Solution previousSolution,
			final LinkedList<Double> previousWordLoadSDs) {
		
		// inits fields
		this.physicians = physicians;
		this.previousWordLoadSDs = previousWordLoadSDs;
		
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
			final double lastWorkSd = previousSolution.getWorkLoadSD();
			final double lastMeanWork = previousSolution.getMeanWorkLoad();
			
			// Id solutions
			final LinkedList<Double> idSolutions = new LinkedList<Double>();
			previousWordLoadSDs.stream().filter(
					(workLoadSD) -> {
						idSolutions.add(workLoadSD);
						return previousWordLoadSDs.getFirst() == lastWorkSd;
					}).findFirst();
			final int nbIdSolutions = idSolutions.size();

			// Sets the shaker indice randomly depends on (id solutions + 1)
			int randomInt = random.nextInt(10);
			int shaker = 0;
			if (randomInt == 0) {
				shaker = (nbIdSolutions + 1) * (nbIdSolutions + 1);
			} else if (randomInt == 1) {
				shaker = Double.valueOf(Math.pow(shaker, 2)).intValue() + 1;
			} else {
				shaker = nbIdSolutions + 1;
			}
			
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
					if (randomIndice >= 2) {
						position.getValue().setWorker(null);
					}
				}
			}
		}

		// For each position, give any possibility
		// The setting of the predefined employees... are done in the random strategy
		this.positions.cellSet().forEach((position) ->  {
			// If we have already a worker, use it and sets the IntVar as fixed
			if (position.getValue().getWorker() != null) {
				position.getValue().setInternalChocoRepresentation(
						VariableFactory.fixed(position.getColumnKey() + "_" + position.getRowKey(),
								position.getValue().getWorker().getInternalIndice(), solver));
			}
			// If no worker is defined, anyone can be defined at this position (depending on randomstrategy)
			else {
				position.getValue().setInternalChocoRepresentation(
						VariableFactory.bounded(position.getColumnKey() + "_" + position.getRowKey(), 0, Integer.MAX_VALUE, solver));
			}
			
			// Indexes the positions depending on intvar
			intVarPositions.put(position.getValue().getInternalChocoRepresentation(), position.getValue());
		});
		
		
		// CREATES THE GENERAL CONSTRAINTS AND APPLY THEM TO THE SOLVER for each date
		this.positions.rowKeySet().forEach((date) -> {
			positionsConstraints.forEach((constraint) -> {
				// Find each IntVar
				final LinkedList<IntVar> internalPositions = new LinkedList<>();
				constraint.getRuleElements().forEach((element) -> {
					// Date of element selected
					final LocalDate targetDate = date.plusDays(element.getDeltaDays());
					// Targeted position
					final Position position = this.positions.get(targetDate, element.getPositionName());
					if (position != null) {
						// Internal Position (representation for Solver)
						final IntVar internalPosition = this.positions.get(targetDate, element.getPositionName()).getInternalChocoRepresentation();
						internalPositions.add(internalPosition);
					}
				});
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
					solver.post(IntConstraintFactory.alldifferent(internalPositions.toArray(new IntVar[internalPositions.size()])));
				}
			});
		});
		
		// Sets the custom strategy
		final CPlanRandomStrategy cPlanRandomStrategy = new CPlanRandomStrategy(physicians, getIntVarPositions());
		final IntStrategy strategy = 
				IntStrategyFactory.custom(
						IntStrategyFactory.random_var_selector(new Date().getTime()),
						cPlanRandomStrategy,
						this.positions.values().stream().map((position) -> position.getInternalChocoRepresentation()).toArray(IntVar[]::new));
		// sets the strategy for the solver
		solver.set(IntStrategyFactory.lastKConflicts(solver, 1000, strategy));
		// Limits the 
		SearchMonitorFactory.limitTime(solver, 600000);

	}
	
	public Solution findSolution() {

		// FINDS A SOLUTION
		solver.findSolution();
				
		// IF NO SOLUTION, RETRY IF A SOLUTION ALREADY EXISTS
		if (solver.isFeasible() != ESat.TRUE && previousWordLoadSDs.size() == 0) {
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
