package com.github.pjpo.planning.model;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.stream.Collectors;

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

	public Solution(
			final HashMap<Integer, Physician> physicians,
			final HashBasedTable<LocalDate, String, Position> positions) {

		this.positions = positions;
		this.workLoads = new HashMap<>(physicians.size());

		for (Cell<LocalDate, String, Position> position : positions.cellSet()) {
			// Gets the solution from solver of the selected physician
			final int selectedWorker = position.getValue().getInternalChocoRepresentation().getValue();
			// Sets the working physician in the solution
			position.getValue().setWorker(physicians.get(selectedWorker));
		}
		
		// INITS THE WORK LOAD
		for (Entry<Integer, Physician> physician : physicians.entrySet()) {
			workLoads.put(physician.getKey(), 0L);
		}

		// 1 - COUNTS THE WORKLOAD FOR EACH PHYSICIAN AND EACH DAY
		for (Cell<LocalDate, String, Position> position : positions.cellSet()) {
			final Long newWorkLong = workLoads.get(position.getValue().getWorker().getInternalIndice()) +
					// Here, we use a scaling of the time part defined in Position, and then
					// adapt depending on the time part
					Long.divideUnsigned(10000000L * position.getValue().getWorkLoad(), position.getValue().getWorker().getTimePart());
			workLoads.put(position.getValue().getWorker().getInternalIndice(), newWorkLong);
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
			final Physician physician = physicians.get(workLoad.getKey());
			
			// 3 - REMOVES THE NOT WORKED DAYS (PAID VACATIONS)
			final Iterator<LocalDate> localDateIt = clonedWorkedDays.iterator();
			while (localDateIt.hasNext()) {
				final LocalDate localDate = localDateIt.next();
				for (final IntervalDateTime vacation : physician.getPaidVacation()) {
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
	}
	
	public HashBasedTable<LocalDate, String, Position> getPositions() {
		return positions;
	}
	
	public Long getWorkLoad(Physician physician) {
		return workLoads.get(physician.getInternalIndice());
	}

	
	/**
	 * Gets the Standard Deviation of workload
	 * @return
	 */
	public double getWorkLoadSD() {
		// Mean value
		final Double mean = getMeanWorkLoad();
		// Now, sum of square difference to mean
		final Double sumSquare = 0D;
		workLoads.values().stream().collect(Collectors.summingDouble((value) -> Math.pow(value.doubleValue() - mean, 2D)));
		// return the sqrt of sumSquare divided by num of elements - 1 (SD)
		return Math.sqrt(sumSquare / ((double) workLoads.size() - 1D));
	}

	/**
	 * Gets the mean workLoad
	 * @return
	 */
	public double getMeanWorkLoad() {
		if (workLoads.size() == 0)
			throw new IllegalArgumentException("No solution has been set");
		return workLoads.values().stream().collect(Collectors.averagingDouble((value) -> value));
	}

}