package com.github.pjpo.planning.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.chocosolver.solver.variables.IntVar;

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
	
	/** Stores the physicians definitions */
	private final HashMap<Integer, Physician> physicians;
	
	/** Stores the positions */	
	private final HashBasedTable<LocalDate, String, Position> positions;
	
	/** Stores the workload of each worker */
	private final HashMap<Integer, Long> workLoads;
	
	/** Used Random */
	private final Random random = new Random(new Date().getTime());

	public Solution(
			final HashMap<Integer, Physician> physicians,
			final HashBasedTable<LocalDate, String, Position> positions) {
		this.physicians = physicians;
		this.positions = positions;
		this.workLoads = new HashMap<>(physicians.size());
	}
	
	/**
	 * When the choco solver finishes the solving, we have a table of intvars we have to store in the current table
	 * and set the workload of each worker
	 * @param solutionMedIndicesIntVarMap
	 */
	public void setSolutionMedIndicesMap(final HashBasedTable<LocalDate, String, IntVar> chocoSolutions) {

		for (Cell<LocalDate, String, IntVar> chocoSolution : chocoSolutions.cellSet()) {
			// Gets the indice of the selected physician
			final int selectedWorker = chocoSolution.getValue().getValue();
			// Sets the working physician in the solution
			positions.get(chocoSolution.getRowKey(), chocoSolution.getColumnKey())
				.setWorker(physicians.get(selectedWorker));
		}
		
		// INITS THE WORK LOAD
		for (Entry<Integer, Physician> physician : physicians.entrySet()) {
			workLoads.put(physician.getKey(), 0L);
		}

		// 1 - COUNTS THE WORKLOAD FOR EACH PHYSICIAN AND EACH DAY
		for (Cell<LocalDate, String, Position> position : positions.cellSet()) {
			final Long newWorkLong = workLoads.get(position.getValue().getWorker().getInternalIndice()) +
					Long.divideUnsigned(10000000L, position.getValue().getWorker().getTimePart());
			workLoads.put(position.getValue().getWorker().getInternalIndice(), newWorkLong)
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

	/**
	 * Lighten the burden of max worker
	 * @param shake
	 * @return
	 */
	public HashMap<LocalDate, HashMap<String, Integer>> lightenWorkBurden(final int shake) {
		if (workLoads.size() == 0)
			throw new IllegalArgumentException("No solution has been set");

		// GET MEAN OF workload 
		final double meanWorkLoad = getMeanWorkLoad();
		
		// CREATES THE NEW INDICES MAP WITH LIGHTEN BURDEN
		final HashMap<LocalDate, HashMap<String, Integer>> lightenSolution = new HashMap<>();
		for (final Entry<LocalDate, HashMap<String, Integer>> actualSolution : solutionMedIndicesMap.entrySet()) {
			// TAKES THIS DATE INTO ACCOUNT
			lightenSolution.put(actualSolution.getKey(), new HashMap<>());
			
			// LIGHTEN BURDEN
			for(final Entry<String, Integer> actualSolutionForPlage : actualSolution.getValue().entrySet()) {
				
				// REMOVE THE PHYSICIAN DEPENDING ON DIFFERENCE BETWEEN HIS WORKLOAD AND THE MEAN WORKLOAD IF HE WORKS TOO MUCH
				if (workLoads.get(actualSolutionForPlage.getValue()).doubleValue() > meanWorkLoad &&
						nextDouble(randomLongs, workLoads.get(actualSolutionForPlage.getValue()) + (double) shake) > meanWorkLoad) {
					lightenSolution.get(actualSolution.getKey()).put(actualSolutionForPlage.getKey(), null);					
				} else {
					lightenSolution.get(actualSolution.getKey()).put(actualSolutionForPlage.getKey(), actualSolutionForPlage.getValue());
				}
			}
		}
		
		//Planning.prettyPrintInteger(newSolutionMap, physicians);
		
		return lightenSolution;
	}
	
	/**
	 * Gets the Standard Deviation of workload
	 * @return
	 */
	public double getWorkLoadSD() {
		// Mean value
		Double mean = getMeanWorkLoad();
		// Now, sum of square difference to mean
		Double sumSquare = 0D;
		for (Long value : workLoads) {
			sumSquare += Math.pow(value.doubleValue() - mean, 2D);
		}
		// Divide it by num of element - 1
		sumSquare = sumSquare / ((double) workLoads.size() - 1D);
		// return the sqrt
		return Math.sqrt(sumSquare);
	}

	/**
	 * Gets the mean workLoad
	 * @return
	 */
	public double getMeanWorkLoad() {
		if (workLoads.size() == 0)
			throw new IllegalArgumentException("No solution has been set");
		// Calculation sum value
		Long sum = 0L;
		for (Long value : workLoads) {
			sum += value;
		}
		// Now, mean value
		return sum.doubleValue() / (double) workLoads.size();
	}

	/**
	 * Returns a new random in range
	 * @param rng
	 * @param n
	 * @return
	 */
	private double nextDouble(Random rng, double n) {
		if (n<=0)
            throw new IllegalArgumentException("n must be positive");

		return rng.nextDouble() * n;
	}
}
