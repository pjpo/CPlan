package com.github.pjpo.planning.problem;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.github.pjpo.planning.model.Worker;
import com.github.pjpo.planning.model.Position;
import com.github.pjpo.planning.utils.IntervalDateTime;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table.Cell;

/**
 * A solution, with a list of periods,
 * can generate a new solution wich lightens the max burden and adds some shaking
 * for genetic algorithm
 * @author jpc
 *
 */
public class Solution {
	
	/** Stores the positions */	
	private final HashBasedTable<LocalDate, String, Position> positions;
	
	/** Stores the workload of each worker */
	private final HashMap<Integer, Long> workLoads;

	/** Standard Deviation of workload */
	private final Double workLoadSD;
	
	/** Mean WorkLoad */
	private final Double meanWorkLoad;
	
	/** Number of undefined positions */
	private final Long undefinedPositionsNb;
	
	/**Undefined worker */
	private final Worker undefinedWorker = new Worker() {{
		setInternalIndice(-1);
		setName("Undefined");
	}};
	
	/**
	 * Creates the composite solution object from a list of employees and a
	 * list of positions
	 * @param physicians
	 * @param positions positions from solver (with choco internal value)
	 */
	public Solution(
			final HashMap<Integer, Worker> physicians,
			final HashBasedTable<LocalDate, String, Position> positions) {

		this.positions = positions;
		this.workLoads = new HashMap<>(physicians.size());

		for (final Cell<LocalDate, String, Position> position : positions.cellSet()) {
			// Gets the solution from solver of the selected physician
			final int selectedWorker = position.getValue().getInternalChocoRepresentation().getValue();
			if (physicians.containsKey(selectedWorker)) {
				// Sets the working physician in the solution
				position.getValue().setWorker(physicians.get(selectedWorker));
			} else {
				position.getValue().setWorker(undefinedWorker);
			}
		}
		
		// INITS THE WORK LOAD
		for (final Entry<Integer, Worker> physician : physicians.entrySet()) {
			workLoads.put(physician.getKey(), 0L);
		}

		// 1 - COUNTS THE WORKLOAD FOR EACH PHYSICIAN AND EACH DAY
		for (final Cell<LocalDate, String, Position> position : positions.cellSet()) {
			// Only keep the physicians (real people)
			if (position.getValue().getWorker().getInternalIndice() > 0) {
				final Long newWorkLong = workLoads.get(position.getValue().getWorker().getInternalIndice()) +
						// Here, we use a scaling of the time part defined in Position, and then
						// adapt depending on the time part
						Long.divideUnsigned(10000000L * position.getValue().getWorkLoad(), position.getValue().getWorker().getTimePart());
				workLoads.put(position.getValue().getWorker().getInternalIndice(), newWorkLong);
			}
		}
		
		// As a reference, creates a hashSet with all the worked days in solution :
		final HashSet<LocalDate> workedDays = new HashSet<>(positions.rowKeySet());
		
		// 2 - DIVIDE THE WORKLOAD BY THE NUMBER OF WORKED DAYS
		for (Entry<Integer, Long> workLoad : workLoads.entrySet()) {
			// CALCULATES NUMBER OF DAYS WORKED IN PERIOD FOR THIS PHYSICIAN
			// 1 - FINDS THE NUMBER OF WORKED DAYS
			@SuppressWarnings("unchecked")
			final HashSet<LocalDate> clonedWorkedDays = (HashSet<LocalDate>) workedDays.clone();
	
			// 2 - FINDS THE PHYSICIAN
			final Worker physician = physicians.get(workLoad.getKey());
			
			// 3 - REMOVES THE NOT WORKED DAYS (PAID VACATIONS)
			final Iterator<LocalDate> localDateIt = clonedWorkedDays.iterator();
			while (localDateIt.hasNext()) {
				final LocalDate localDate = localDateIt.next();
				for (final IntervalDateTime vacation : physician.getPaidVacations()) {
					if (vacation.isOverlapping(new IntervalDateTime(localDate.atTime(12, 00), localDate.atTime(12, 30)))) {
						// DATE IS OUTSIDE WORK RANGE, REMOVE IT FROM WORKED DAYS FOR THIS PHYSICIAN
						localDateIt.remove();
						break;
					}
				}
			}

			// 4 - DIVIDE THE WORKLOAD
			workLoad.setValue(Long.divideUnsigned(workLoad.getValue(), clonedWorkedDays.size()));
		}
		
		// Sets the mean workload
		if (workLoads.size() == 0)
			meanWorkLoad = 0D;
		else
			meanWorkLoad = workLoads.values().stream().collect(Collectors.averagingDouble((value) -> value));
		
		// Sets the Standard deviation of workloads
		if (workLoads.size() < 1)
			workLoadSD = Double.MAX_VALUE;
		else
			workLoadSD = Math.sqrt(
					workLoads.values().stream().collect(Collectors.summingDouble((value) -> Math.pow(value.doubleValue() - meanWorkLoad, 2D))) /
					((double) workLoads.size() - 1D));
		
		// Sets the number of undefined positions
		undefinedPositionsNb = positions.values().stream().filter((position) -> position.getWorker().getInternalIndice() == -1).count();
	}
	
	/**
	 * Returns the positions for the planning
	 * @return
	 */
	public HashBasedTable<LocalDate, String, Position> getPositions() {
		return positions;
	}
	
	/**
	 * Returns the workload for a specific employee
	 * @param physician
	 * @return
	 */
	public Long getWorkLoad(final Worker physician) {
		return workLoads.get(physician.getInternalIndice());
	}

	
	/**
	 * Gets the Standard Deviation of workload
	 * @return
	 */
	public double getWorkLoadSD() {
		return workLoadSD;
	}

	/**
	 * Gets the mean workLoad
	 * @return
	 */
	public double getMeanWorkLoad() {
		return meanWorkLoad;
	}

	/**
	 * Gets the number of positions where no worker has been found to be able to work
	 * @return
	 */
	public long getUndefinedWorkers() {
		return undefinedPositionsNb;
	}
}
