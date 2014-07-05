package com.github.pjpo.planning;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;

import solver.variables.IntVar;

import com.github.pjpo.planning.physician.Physician;
import com.github.pjpo.planning.utils.IntervalDate;
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
	private final HashMap<LocalDate, HashMap<String, IntervalDateTime>> workingPeriodsMap;
	
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
	public Solution(final HashMap<LocalDate, HashMap<String, IntervalDateTime>> workingPeriodsMap,
			ArrayList<Physician> physicians) {
		this.workingPeriodsMap = workingPeriodsMap;
		this.physicians = physicians;
	}
	
	public HashMap<LocalDate, HashMap<String, IntervalDateTime>> getWorkingPeriodsMap() {
		return workingPeriodsMap;
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
			if (!workingPeriodsMap.containsKey(solutionMedIndiceEntry.getKey()))
				throw new IllegalArgumentException("Solution does not meet working periods configuration");

			// CREATES THIS NEW LOCALDATE IN SOLUTION
			solutionMedIndicesMap.put(solutionMedIndiceEntry.getKey(), new HashMap<>());
			// TAKE THIS WORKING DATE INTO ACCOUNT
			workedDays.add(solutionMedIndiceEntry.getKey());
			
			// COPY INTVARS FOR THIS LOCALDATE
			for (final Entry<String, IntVar> solutionMedIndiceEntryForInterval : solutionMedIndiceEntry.getValue().entrySet()) {
				// VERIFY THAT THIS POSTE EXISTS IN WORKINGPERIODSMAP
				if (!workingPeriodsMap.get(solutionMedIndiceEntry.getKey()).containsKey(solutionMedIndiceEntryForInterval.getKey()))
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
			if (workingPeriodsMap.get(solutionMedIndiceEntry.getKey()).size() != solutionMedIndicesMap.get(solutionMedIndiceEntry.getKey()).size())
				throw new IllegalArgumentException("Solution does not meet working periods configuration");
		}
		// VERIFY THAT WE DID NOT HAD SOME DEFINED WORKING DATES IN WORKINGPERIODMAPS THAT ARE NOT IN THE SOLUTION
		if (workingPeriodsMap.size() != solutionMedIndicesMap.size())
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
			
			// 3 - REMOVES THE NOT WORKED DAYS
			Iterator<LocalDate> localDateIt = clonedWorkedDays.iterator();
			while (localDateIt.hasNext()) {
				LocalDate localDate = localDateIt.next();
				for (IntervalDate vacation : physician.getPaidVacation()) {
					if (vacation.isInPeriod(localDate)) {
						// DATE IS OUTSIDE WORK RANGE, REMOVE IT FROM WORKED DAYS FOR THIS PHYSICIAN
						localDateIt.remove();
						break;
					}
				}
				// 4 - DIVIDE THE WORKLOAD
				workLoads.set(i, Long.divideUnsigned(workLoads.get(i), clonedWorkedDays.size()));
			}
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

		// GETS THE MAX WORKER
		int maxWorker = getMaxWorkerPhysician();
		// GET MIN AND MAX WORKLOAD
		long minWorkLoad = getMinWorkLoad();
		long maxWorkLoad = getMaxWorkLoad();
		
		// CREATES THE NEW INDICES MAP
		final HashMap<LocalDate, HashMap<String, Integer>> newSolutionMap = new HashMap<>();
		solutionMedIndicesMap.forEach(
				(LocalDate localDate, HashMap<String, Integer> content) -> {
					// NEW LOCALDATE
					newSolutionMap.put(localDate, new HashMap<>());
					// FILLS THE NEW LOCAL DATE
					content.forEach(
							(String key, Integer value) -> {
								// RANDOMLY REMOVES THIS WORK PERIOD IF WORKER WORKS TOO MUCH
								// IF DIFFERENCE BETWEEN MAX AND MIN WORKLOAD IS LOW, WE WILL NOT REMOVE A LOT OF THEM
								if (value == maxWorker && nextLong(randomLongs, maxWorkLoad) > minWorkLoad) {
									newSolutionMap.get(localDate).put(key, null);
								}
								// ELSE REMOVE THE PHYSICIAN DEPENDING ON RANDOM
								else if (nextLong(randomLongs, 10 + shake) > 10) {
									newSolutionMap.get(localDate).put(key, null);
								} else {
									newSolutionMap.get(localDate).put(key, value);
								}
					});
		});
		
		//Planning.prettyPrintInteger(newSolutionMap, physicians);
		
		return newSolutionMap;
	}
	
	/**
	 * Gets the minimum work load
	 * @return
	 */
	public long getMinWorkLoad() {
		if (workLoads.size() == 0)
			throw new IllegalArgumentException("No solution has been set");
		return workLoads.stream().min(Long::compare).get();
	}

	/**
	 * Gets the maximum work load
	 * @return
	 */
	public long getMaxWorkLoad() {
		if (workLoads.size() == 0)
			throw new IllegalArgumentException("No solution has been set");
		return workLoads.stream().max(Long::compare).get();
	}
	
	/**
	 * Returns the max worker
	 * @return
	 */
	public int getMaxWorkerPhysician() {
		if (workLoads.size() == 0)
			throw new IllegalArgumentException("No solution has been set");
		long maxLoad = -1;
		int maxWorker = -1;
		for (int i = 0 ; i < physicians.size() ; i++) {
			if (workLoads.get(i) > maxLoad) {
				maxWorker = i;
				maxLoad = workLoads.get(i);
			}
		}
		return maxWorker;
	}
	
	/**
	 * Returns a new random in range
	 * @param rng
	 * @param n
	 * @return
	 */
	private long nextLong(Random rng, long n) {
		if (n<=0)
            throw new IllegalArgumentException("n must be positive");

		long bits, val;
		do {
			bits = (rng.nextLong() << 1) >>> 1;
			val = bits % n;
		} while (bits-val+(n-1) < 0L);
		return val;
	}
}
