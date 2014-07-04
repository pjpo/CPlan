package com.github.pjpo.planning;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import solver.variables.IntVar;

import com.github.pjpo.planning.lignes.Plage;
import com.github.pjpo.planning.physician.Physician;

public class Solution {
	
	/** Stores the configuration for time working periods */
	private HashMap<LocalDate, HashMap<String, Plage>> workingPeriodsMap = null;
	
	/** Stores the physicians definitions */
	private ArrayList<Physician> physicians = null;
	
	/** Stores the solution */
	private HashMap<LocalDate, HashMap<String, Integer>> solutionMedIndicesMap = null;
	
	/** Stores the workload for each physician */
	private ArrayList<Long> workLoads = null;
	
	public HashMap<LocalDate, HashMap<String, Plage>> getWorkingPeriodsMap() {
		return workingPeriodsMap;
	}

	public void setWorkingPeriodsMap(
			HashMap<LocalDate, HashMap<String, Plage>> workingPeriodsMap) {
		if (solutionMedIndicesMap != null) {
			throw new IllegalArgumentException("Solution has been set, can't set a new config");
		} else {
			this.workingPeriodsMap = workingPeriodsMap;
		}
	}

	public HashMap<LocalDate, HashMap<String, Integer>> getSolutionMedIndicesMap() {
		return solutionMedIndicesMap;
	}


	public ArrayList<Physician> getPhysicians() {
		return physicians;
	}

	public void setPhysicians(ArrayList<Physician> physicians) {
		if (solutionMedIndicesMap != null) {
			throw new IllegalArgumentException("Solution has been set, can't set a new config");
		} else {
			this.physicians = physicians;
		}
	}

	public ArrayList<Long> getWorkLoads() {
		return workLoads;
	}

	public void setSolutionMedIndicesMap(
			HashMap<LocalDate, HashMap<String, IntVar>> solutionMedIndicesMap) {
		this.solutionMedIndicesMap = null;
		this.workLoads = null;
		// VERIFY THAT WORKINGPERIODSMAP HAS BEEN SET
		if (workingPeriodsMap == null || physicians == null)
			throw new IllegalArgumentException("workingPeriodsMap or physicians definitions has not been set");
		else {
			// NEW INDICES MAP
			HashMap<LocalDate, HashMap<String, Integer>> newSolutionMedIndicesMap =
					new HashMap<>();

			solutionMedIndicesMap.forEach(
					(LocalDate localDate, HashMap<String, IntVar> content) -> {
					// WERIFY THAT THIS LOCALDATE EXISTS IN WORKINGPERIODSMAP
					if (!workingPeriodsMap.containsKey(localDate))
						throw new IllegalArgumentException("Solution does not meet working periods configuration");
					else {
						// CREATES THIS LOCALDATE IN SOLUTION IF NEEDED
						newSolutionMedIndicesMap.put(localDate, new HashMap<>());
						// COPY CONTENT OF THE LOCALDATE
						content.forEach(
								(String def, IntVar solution) -> {
									// VERIFY THAT THIS DEFINITION EXISTS IN WORKINGPERIODSMAP
									if (!workingPeriodsMap.get(localDate).containsKey(def))
										throw new IllegalArgumentException("Solution does not meet working periods configuration");
									else {
										// VERIFIES THAT THIS PHYSICIAN EXISTS
										if ((solution.getValue() < 0) || (solution.getValue() >= physicians.size()))
											throw new IllegalArgumentException("Solution does not meet working periods configuration");
										// COPIES THE DEFINITION
										newSolutionMedIndicesMap.get(localDate).put(def, solution.getValue());
									}
								});
						// VERIFY THAT THE SOLUTION MEETS THE PERIODS CONFIGURATION FOR THIS LOCALDATE
						if (workingPeriodsMap.get(localDate).size() != newSolutionMedIndicesMap.get(localDate).size())
							throw new IllegalArgumentException("Solution does not meet working periods configuration");
					}
				}
			);
			
			// VERIFY THAT THE SOLUTION MEETS THE PERIODS CONFIGURATION
			if (workingPeriodsMap.size() != newSolutionMedIndicesMap.size())
				throw new IllegalArgumentException("Solution does not meet working periods configuration");
			
			this.solutionMedIndicesMap = newSolutionMedIndicesMap;
			
			// CREATES THE HASHMAP FOR WORKLOAD
			workLoads = new ArrayList<>(physicians.size());
			for (int i = 0 ; i < physicians.size() ; i++) {
				workLoads.add(0L);
			}

			// COUNTS THE WORKLOAD FOR EACH PHYSICIAN
			newSolutionMedIndicesMap.values().forEach(
					(HashMap<String, Integer> def) -> {
						def.values().forEach(
								(Integer physicianIndice) -> {
									// WE ADAPT THE WORKLOAD DEPENDING ON THE PART TIME :
									// MULTIPLY NB WORKS WITH 1000000/PART TIME
									Long newWorkLoad =
											workLoads.get(physicianIndice) +  Long.divideUnsigned(10000000L, physicians.get(physicianIndice).getTimePart());
									workLoads.set(physicianIndice, newWorkLoad);
								});
					});
			
			// WE ADAPT THE WORKLOAD DEPENDING ON THE NUMBER OF WORKED DAYS
			// 1 - LIST NUMBER OF WORKED DAYS
			HashSet<LocalDate> workedDays = new HashSet<>();
			newSolutionMedIndicesMap.keySet().forEach(
					(localDate) -> workedDays.add(localDate));
			
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
					if ((physician.getWorkStart() != null && localDate.isBefore(physician.getWorkStart()))
							|| (physician.getWorkEnd() != null && localDate.isAfter(physician.getWorkEnd()))) {
						// DATE IS OUTSIDE WORK RANGE, REMOVE IT FROM WORKED DAYS FOR THIS PHYSICIAN
						localDateIt.remove();
					} else {
						// TODO : IF WORK IN PERIOD, CHECK IF THERE IS THE PHYSICIAN HAS VACANCIES
					}
				}
				// 4 - DIVIDE THE WORKLOAD
				workLoads.set(i, Long.divideUnsigned(workLoads.get(i), clonedWorkedDays.size()));
			}
		}
	}
	
	public HashMap<LocalDate, HashMap<String, Integer>> lightenWorkBurden(int shake) {
		if (workLoads == null)
			throw new IllegalArgumentException("No solution has been set");
		// GETS THE MAX WORKER
		int maxWorker = getMaxWorkerPhysician();
		// GET MIN AND MAX WORKLOAD
		long minWorkLoad = getMinWorkLoad();
		long maxWorkLoad = getMaxWorkLoad();
		
		// RANDOM USED
		Random randomLongs = new Random();
		// CREATES THE NEW INDICES MAP
		HashMap<LocalDate, HashMap<String, Integer>> newSolutionMap = new HashMap<>();
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
	
	public long getMinWorkLoad() {
		if (workLoads == null)
			throw new IllegalArgumentException("No solution has been set");
		return workLoads.stream().min(Long::compare).get();
	}

	public long getMaxWorkLoad() {
		if (workLoads == null)
			throw new IllegalArgumentException("No solution has been set");
		return workLoads.stream().max(Long::compare).get();
	}
	
	public int getMaxWorkerPhysician() {
		if (workLoads == null)
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
