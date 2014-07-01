package com.github.aiderpmsi.planning;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import solver.variables.IntVar;

import com.github.aiderpmsi.planning.lignes.Plage;
import com.github.aiderpmsi.planning.physician.Physician;

public class Solution {
	
	/** Stores the configuration for time working periods */
	private HashMap<LocalDate, HashMap<String, Plage>> workingPeriodsMap = null;
	
	/** Stores the physicians definitions */
	private ArrayList<Physician> physicians = null;
	
	/** Stores the solution */
	private HashMap<LocalDate, HashMap<String, Integer>> solutionMedIndicesMap = null;
	
	/** Stores the workload for each physician */
	private ArrayList<Integer> workLoads = null;
	
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

	public ArrayList<Integer> getWorkLoads() {
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
										if ((solution.getValue() < 0) || (solution.getValue() >= physicians.size() - 1))
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
			for (int i = 0 ; i < workLoads.size() ; i++) {
				workLoads.add(0);
			}

			// COUNTS THE WORKLOAD FOR EACH PHYSICIAN
			newSolutionMedIndicesMap.values().forEach(
					(HashMap<String, Integer> def) -> {
						def.values().forEach(
								(Integer physicianIndice) -> {
									Integer newWorkLoad =
											workLoads.get(physicianIndice) +  Integer.divideUnsigned(100000, physicians.get(physicianIndice).getTimePart());
									workLoads.set(physicianIndice, newWorkLoad);
								});
					});
		}
	}
	
	public HashMap<LocalDate, HashMap<String, Integer>> lightenWorkBurden() {
		if (workLoads == null)
			throw new IllegalArgumentException("No solution has been set");
		// GETS THE MAX WORKER
		int maxWorker = getMaxWorkerPhysician();
		// GET MIN AND MAX WORKLOAD
		int minWorkLoad = getMinWorkLoad();
		int maxWorkLoad = getMaxWorkLoad();
		
		// RANDOM USED
		Random randomsInts = new Random();
		
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
								if (value == maxWorker && randomsInts.nextInt(maxWorkLoad) > minWorkLoad) {
									newSolutionMap.get(localDate).put(key, null);
								}
								// ELSE REMOVE THE PHYSICIAN DEPENDING ON RANDOM
								else if (randomsInts.nextInt(maxWorkLoad * physicians.size()) < minWorkLoad) {
									newSolutionMap.get(localDate).put(key, value);
								} else {
									newSolutionMap.get(localDate).put(key, null);
								}
					});
		});
		
		return newSolutionMap;
	}
	
	public int getMinWorkLoad() {
		if (workLoads == null)
			throw new IllegalArgumentException("No solution has been set");
		return workLoads.stream().min(Integer::compare).get();
	}

	public int getMaxWorkLoad() {
		if (workLoads == null)
			throw new IllegalArgumentException("No solution has been set");
		return workLoads.stream().max(Integer::compare).get();
	}
	
	public int getMaxWorkerPhysician() {
		if (workLoads == null)
			throw new IllegalArgumentException("No solution has been set");
		int maxLoad = -1;
		int maxWorker = -1;
		for (int i = 0 ; i < physicians.size() ; i++) {
			if (workLoads.get(i) > maxLoad)
				maxWorker = i;
		}
		return maxWorker;
	}
}
