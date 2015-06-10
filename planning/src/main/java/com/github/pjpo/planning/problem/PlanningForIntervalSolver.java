package com.github.pjpo.planning.problem;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.loop.monitors.SearchMonitorFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.selectors.IntValueSelector;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.ESat;

import com.github.pjpo.planning.CPlanRandomStrategy;
import com.github.pjpo.planning.constraintsrules.PositionConstraintBase;
import com.github.pjpo.planning.constraintsrules.PositionDifferentConstraint;
import com.github.pjpo.planning.constraintsrules.PositionEqualConstraint;
import com.github.pjpo.planning.model.Physician;
import com.github.pjpo.planning.model.Position;
import com.github.pjpo.planning.model.Solution;
import com.github.pjpo.planning.utils.IntervalDateTime;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table.Cell;

public class PlanningForIntervalSolver {
	
	/** Choco solver */
	private final Solver solver;
	
	/** Private HashTable for storing intvars from choco solver */
	private final HashBasedTable<LocalDate, String, Position> positions = HashBasedTable.create();
	
	/** Random number generator */
	private final Random random = new Random(new Date().getTime()); 
	
	/** Previous workloadSds for previous solutions */
	private final LinkedList<Double> previousWordLoadSDs;
	
	// Defined Workers
	private final HashMap<Integer, Physician> physicians;
	
	public PlanningForIntervalSolver(
			final HashMap<Integer, Physician> physicians,
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
			final int shaker = random.nextInt(10) == 0 ? (nbIdSolutions + 1) * (nbIdSolutions + 1) : nbIdSolutions + 1;
			
			// lighten burden of work
			for (final Cell<LocalDate, String, Position> position : this.positions.cellSet()) {
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

		// Fill the choco solver with the defined positions
		this.positions.cellSet().forEach((position) ->  {
			// If we have already a worker, use it and sets the IntVar
			if (position.getValue().getWorker() != null) {
				position.getValue().setInternalChocoRepresentation(
						VariableFactory.fixed(position.getColumnKey() + "_" + position.getRowKey(),
								position.getValue().getWorker().getInternalIndice(), solver));
			}
			// If we don't have a worker, see which one is possible to work at this date
			else {
				// Does somebody has to work this day ?
				for (final Entry<Integer, Physician> physician : physicians.entrySet()) {
					if (physician.getValue().getWorkedVacs().containsEntry(position.getColumnKey(), position.getRowKey())) {
						position.getValue().setInternalChocoRepresentation(
								VariableFactory.fixed(position.getColumnKey() + "_" + position.getRowKey(),
										physician.getValue().getInternalIndice(), solver));
						return;
					}
				}

				// List people able to work at this position
				final LinkedList<Physician> workersAbleToWork = new LinkedList<>();
				eachPhysician : for (final Entry<Integer, Physician> physician : physicians.entrySet()) {
					
					// CHECK IF THIS PHYSICIAN HAS THE RIGHT TO WORK AT THIS POSITION
					if (physician.getValue().getRefusedPostes().contains(position.getColumnKey()))
						continue eachPhysician;					
					
					// CHECK IF PHYSICIAN IS IN PAID VACATIONS
					for (final IntervalDateTime vacation : physician.getValue().getPaidVacation()) {
						if (vacation.isOverlapping(position.getValue().getPlage()))
							continue eachPhysician;
					}
					
					// CHECK IF PHYSICIAN IS IN UNPAID VACATIONS
					for (final IntervalDateTime vacation : physician.getValue().getUnpaidVacation()) {
						if (vacation.isOverlapping(position.getValue().getPlage()))
							continue eachPhysician;
					}
					
					// THIS PHYSICIAN CAN WORK AT THIS POSITION AT THIS DAY
					workersAbleToWork.add(physician.getValue());
				}

				if (workersAbleToWork.size() == 0) {
					throw new IllegalArgumentException("No worker able to work the " + position.getRowKey());
				} else {
					position.getValue().setInternalChocoRepresentation(
							VariableFactory.enumerated(position.getColumnKey() + "_" + position.getRowKey(),
									workersAbleToWork.stream().mapToInt((value) -> value.getInternalIndice()).toArray(), solver));
				}
			}
		});
		
		// Sets the constraints
		
		
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
							IntConstraintFactory.arithm(previousElement, "=", element);
						}
						previousElement = element;
					}
				} else if (constraint instanceof PositionDifferentConstraint) {
					IntConstraintFactory.alldifferent(internalPositions.toArray(new IntVar[internalPositions.size()]));
				}
			});
		});
		
		// Sets the custom strategy
		final IntStrategy strategy = 
				IntStrategyFactory.custom(
						IntStrategyFactory.random_var_selector(new Date().getTime()),
						(IntValueSelector) new CPlanRandomStrategy(physicians),
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

}
