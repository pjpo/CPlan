package com.github.pjpo.planning.jours;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import solver.Solver;
import solver.variables.IntVar;
import solver.variables.VariableFactory;


import com.github.pjpo.planning.lignes.Plage;
import com.github.pjpo.planning.physician.Physician;
import com.github.pjpo.planning.utils.DaysPeriod;

public class Agenda {

	/** List of days and worked periods */
	private HashMap<LocalDate, HashMap<String, Plage>> workingPeriods = null;
	
	private LocalDate from = null;
	
	private LocalDate to = null;

	private Jour typeJour;
	
	public Agenda(Jour typeJour) {
		this.typeJour = typeJour;
	}
	
	/**
	 * Bounds are inclusive
	 * @param from
	 * @param to
	 */
	public void setBounds(LocalDate from, LocalDate to) {
		workingPeriods = null;
		if (from.isAfter(to))
			throw new IllegalArgumentException("Starting bound is after end bound");
		this.from = from;
		this.to = to;
	}
	
	public void calculateWorkingPeriods() {
		workingPeriods = null;
		if (from == null || to == null)
			throw new IllegalArgumentException("Bounds have to been set");
		workingPeriods = new HashMap<>();
		for (LocalDate date = from ; !date.isAfter(to) ; date = date.plusDays(1L)) {
			workingPeriods.put(date, typeJour.getPlages(date));
		}
	}
	
	public HashMap<LocalDate, HashMap<String, IntVar>> fillSolver(Solver solver, List<Physician> physicians) {
		if (workingPeriods == null)
			throw new IllegalArgumentException("Agenda has not been calculated");
		// CREATES THE MAP OF DATES
		HashMap<LocalDate, HashMap<String, IntVar>> workers = new HashMap<>(workingPeriods.size());
		// CREATES THE INTVARS
		workingPeriods.forEach(
				(date, content) -> {
					HashMap<String, IntVar> newHashMap = new HashMap<>();
					workers.put(date, newHashMap);
					content.forEach((key, plage) -> newHashMap.put(key, VariableFactory.bounded(date.toString() + "_" + key, 0, physicians.size() - 1, solver)));
				});
		// CREATES THE GENERAL CONSTRAINTS AND APPLY THEM TO THE SOLVER
		for (LocalDate date = from ; !date.isAfter(to) ; date = date.plusDays(1L)) {
			typeJour.getConstraints(date, workers).forEach(
					(constraint) -> solver.post(constraint));
		}
		// RETURN THE VARS
		return workers;
	}

	public HashMap<LocalDate, HashMap<String, IntVar>> fillSolver(
			Solver solver,
			List<Physician> physicians,
			HashMap<LocalDate, HashMap<String, Integer>> preFill) {
		if (workingPeriods == null)
			throw new IllegalArgumentException("Agenda has not been calculated");
		// CREATES THE MAP OF DATES
		HashMap<LocalDate, HashMap<String, IntVar>> workers = new HashMap<>(workingPeriods.size());
		// CREATES THE INTVARS
		workingPeriods.forEach(
				(date, content) -> {
					HashMap<String, IntVar> newHashMap = new HashMap<>();
					workers.put(date, newHashMap);
					// CHECKS THE PHYSICIANS WORKING  IF THIS PHYSICIAN WORKS THIS DAY
					ArrayList<Integer> workingPhysicians = new ArrayList<>();
					eachPhysician : for (int i = 0 ; i < physicians.size() ; i++) {
						// CHECKS IF THE PHYSICIAN IS DURING CONTRACT
						if (new DaysPeriod(physicians.get(i).getWorkStart(), physicians.get(i).getWorkEnd()).isInPeriod(date)) {
							// IF UNDER CONTRACT, TEST PAID VACANCIES
							for (DaysPeriod paidVacation : physicians.get(i).getPaidVacation()) {
								if (paidVacation.isInPeriod(date)) {
									continue eachPhysician;
								}
							}
							// TEST UNPAID VACANCES
							for (DaysPeriod unpaidVacation : physicians.get(i).getUnpaidVacation()) {
								if (unpaidVacation.isInPeriod(date)) {
									continue eachPhysician;
								}
							}
							// IF WE ARE THERE, PHYSICIAN IS WORKING
							workingPhysicians.add(i);
						}
					}
					content.forEach((key, plage) -> {
						// TRY TO SEE IF WE HAVE A PRESET VALUE IN PREFILL
						IntVar newIntVar = null;
						if (preFill.get(date) != null && preFill.get(date).get(key) != null) {
							newIntVar = VariableFactory.fixed(date.toString() + "_" + key, preFill.get(date).get(key), solver);
						} else {
							// CHECK IF A PHYSICIAN WAS PREDEFINED FOR THIS DAY AND THIS POSTE
							for (int i = 0 ; i < physicians.size() ; i++) {
								if (physicians.get(i).getWorkedVacs().containsKey(date) && physicians.get(i).getWorkedVacs().get(date).contains(key)) {
									newIntVar = VariableFactory.fixed(date.toString() + "_" + key, i, solver);
								}
							}
							// IF NO PHYSICIAN WAS PREDEFINED, TEST WITH ANY
							if (newIntVar == null)
								newIntVar = VariableFactory.enumerated(date.toString() + "_" + key, toIntArray(workingPhysicians), solver);
						}
						newHashMap.put(key, newIntVar);
					});
				});
		// CREATES THE GENERAL CONSTRAINTS AND APPLY THEM TO THE SOLVER
		for (LocalDate date = from ; !date.isAfter(to) ; date = date.plusDays(1L)) {
			typeJour.getConstraints(date, workers).forEach(
					(constraint) -> solver.post(constraint));
		}
		
		// RETURN THE VARS
		return workers;
	}
	
	
	public HashMap<LocalDate, HashMap<String, Plage>> getWorkingPeriods() {
		return workingPeriods;
	}

	private int[] toIntArray(ArrayList<Integer> array) {
		int[] newArray = new int[array.size()];
		for (int i = 0 ; i < array.size() ; i++) {
			newArray[i] = array.get(i);
		}
		return newArray;
	}
}
