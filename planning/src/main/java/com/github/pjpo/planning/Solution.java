package com.github.pjpo.planning;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;

import org.chocosolver.solver.variables.IntVar;

import com.github.pjpo.planning.lignes.Position;
import com.github.pjpo.planning.physician.Physician;
import com.github.pjpo.planning.utils.IntervalDateTime;

/**
 * A solution, with a list of periods,
 * can generate a new solution wich lightens the max burden and adds some shaking
 * for genetic algorithm
 * @author jpc
 *
 */
public class Solution {
	
	/** Stores the configuration for time working periods */
	private final HashMap<LocalDate, HashMap<String, Position>> workingPositions;
	
	/** Stores the physicians definitions */
	private final ArrayList<Physician> physicians;
	
	/** Stores the solution */
	private final HashMap<LocalDate, HashMap<String, Integer>> solutionMedIndicesMap = new HashMap<>();
	
	/** Stores the workload for each physician */
	private final ArrayList<Long> workLoads = new ArrayList<>();

	/** Used Random */
	private final Random randomLongs = new Random(new Date().getTime());

	/**
	 * Creates the solutions
	 * @param workingPeriodsMap
	 * @param physicians
	 */
	public Solution(final HashMap<LocalDate, HashMap<String, Position>> workingPositions,
			ArrayList<Physician> physicians) {
		this.workingPositions = workingPositions;
		this.physicians = physicians;
	}
	
	public HashMap<LocalDate, HashMap<String, Position>> getWorkingPositions() {
		return workingPositions;
	}

	public HashMap<LocalDate, HashMap<String, Integer>> getSolutionMedIndicesMap() {
		return solutionMedIndicesMap;
	}

	public ArrayList<Physician> getPhysicians() {
		return physicians;
	}

	public ArrayList<Long> getWorkLoads() {
		return workLoads;
	}

	/**
	 * Sets the solutions
	 * @param solutionMedIndicesIntVarMap
	 */
	public void setSolutionMedIndicesMap(
			final HashMap<LocalDate, HashMap<String, IntVar>> solutionMedIndicesIntVarMap) {
		solutionMedIndicesMap.clear();
		workLoads.clear();
		final HashSet<LocalDate> workedDays = new HashSet<>();
		
		for (final Entry<LocalDate, HashMap<String, IntVar>> solutionMedIndiceEntry : solutionMedIndicesIntVarMap.entrySet()) {
			// WERIFY THAT THIS LOCALDATE EXISTS IN WORKINGPERIODSMAP
			if (!workingPositions.containsKey(solutionMedIndiceEntry.getKey()))
				throw new IllegalArgumentException("Solution does not meet working periods configuration");

			// CREATES THIS NEW LOCALDATE IN SOLUTION
			solutionMedIndicesMap.put(solutionMedIndiceEntry.getKey(), new HashMap<>());
			// TAKE THIS WORKING DATE INTO ACCOUNT
			workedDays.add(solutionMedIndiceEntry.getKey());
			
			// COPY INTVARS FOR THIS LOCALDATE
			for (final Entry<String, IntVar> solutionMedIndiceEntryForInterval : solutionMedIndiceEntry.getValue().entrySet()) {
				// VERIFY THAT THIS POSTE EXISTS IN WORKINGPERIODSMAP
				if (!workingPositions.get(solutionMedIndiceEntry.getKey()).containsKey(solutionMedIndiceEntryForInterval.getKey()))
					throw new IllegalArgumentException("Solution does not meet working periods configuration");
				// VERIFY THAT THIS PHYSICIAN EXISTS
				if ((solutionMedIndiceEntryForInterval.getValue().getValue() < 0)
						|| (solutionMedIndiceEntryForInterval.getValue().getValue() >= physicians.size()))
					throw new IllegalArgumentException("Solution does not meet working periods configuration");
				// IF WE ARE THERE, THIS POSTE EXISTS AND THIS PHYSICIAN EXIST
				solutionMedIndicesMap.get(solutionMedIndiceEntry.getKey()).put(
						solutionMedIndiceEntryForInterval.getKey(),
						solutionMedIndiceEntryForInterval.getValue().getValue());
			}

			// VERIFY THAT WE DID NOT HAD SOME DEFINED WORKING PLACES IN WORKINGPERIODMAPS THAT ARE NOT IN THE SOLUTION
			if (workingPositions.get(solutionMedIndiceEntry.getKey()).size() != solutionMedIndicesMap.get(solutionMedIndiceEntry.getKey()).size())
				throw new IllegalArgumentException("Solution does not meet working periods configuration");
		}
		// VERIFY THAT WE DID NOT HAD SOME DEFINED WORKING DATES IN WORKINGPERIODMAPS THAT ARE NOT IN THE SOLUTION
		if (workingPositions.size() != solutionMedIndicesMap.size())
			throw new IllegalArgumentException("Solution does not meet working periods configuration");
		
		// INITS THE WORK LOAD
		for (int i = 0 ; i < physicians.size() ; i++) {
				workLoads.add(0L);
		}

		// 1 - COUNTS THE WORKLOAD FOR EACH PHYSICIAN AND EACH DAY
		for (final HashMap<String, Integer> postes : solutionMedIndicesMap.values()) {
			for (final Integer physicianIndice : postes.values()) {
				// WE ADAPT THE WORKLOAD DEPENDING ON THE PART TIME :
				// MULTIPLY NB WORKS WITH 1000000/PART TIME
				Long newWorkLoad =
						workLoads.get(physicianIndice) +  Long.divideUnsigned(10000000L, physicians.get(physicianIndice).getTimePart());
				workLoads.set(physicianIndice, newWorkLoad);
			}
		}
		
		// 2 - DIVIDE THE WORKLOAD BY THE NUMBER OF WORKED DAYS
		for (int i = 0 ; i < workLoads.size() ; i++) {
			// CALCULATES NUMBER OF DAYS WORKED IN PERIOD FOR THIS PHYSICIAN
			// 1 - FINDS THE NUMBER OF WORKED DAYS
			@SuppressWarnings("unchecked")
			HashSet<LocalDate> clonedWorkedDays = (HashSet<LocalDate>) workedDays.clone();
	
			// 2 - FINDS THE PHYSICIAN
			Physician physician = physicians.get(i);
			
			// 3 - REMOVES THE NOT WORKED DAYS (PAID VACATIONS)
			final Iterator<LocalDate> localDateIt = clonedWorkedDays.iterator();
			while (localDateIt.hasNext()) {
				LocalDate localDate = localDateIt.next();
				for (IntervalDateTime vacation : physician.getPaidVacation()) {
					if (vacation.isOverlapping(new IntervalDateTime(localDate.atTime(12, 00), localDate.atTime(12, 30)))) {
						// DATE IS OUTSIDE WORK RANGE, REMOVE IT FROM WORKED DAYS FOR THIS PHYSICIAN
						localDateIt.remove();
						break;
					}
				}
			}
			// 4 - DIVIDE THE WORKLOAD
			workLoads.set(i, Long.divideUnsigned(workLoads.get(i), clonedWorkedDays.size()));
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
