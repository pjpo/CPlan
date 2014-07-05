package com.github.pjpo.planning.jours;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import solver.Solver;
import solver.constraints.Constraint;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import com.github.pjpo.planning.physician.Physician;
import com.github.pjpo.planning.utils.IntervalDate;
import com.github.pjpo.planning.utils.IntervalDateTime;

public class Agenda {

	/** List of days and worked periods */
	private final HashMap<LocalDate, HashMap<String, IntervalDateTime>> workingPeriods = new HashMap<>();
	
	/** Interval of agenda */
	private final IntervalDate interval;

	/** Definition of type of day */
	private final Jour typeJour;

	/**
	 * Creates an agenda for this kind of jour and a defined interval
	 * @param typeJour
	 * @param intervalDate
	 */
	public Agenda(final Jour typeJour, final IntervalDate intervalDate) {
		if (intervalDate.getStart() == null || intervalDate.getEnd() == null)
			throw new IllegalArgumentException("interval must be finite");
		this.typeJour = typeJour;
		this.interval = intervalDate;
	}

	/**
	 * Fills the days of agenda with intervals of work
	 */
	public void calculateWorkingPeriods() {
		workingPeriods.clear();
		for (LocalDate date = interval.getStart() ; !date.isAfter(interval.getEnd()) ; date = date.plusDays(1L)) {
			workingPeriods.put(date, typeJour.getPlages(date));
		}
	}

	/**
	 * Fills the solver with physicians possibilities
	 * @param solver
	 * @param physicians
	 * @param preFill
	 * @return
	 */
	public HashMap<LocalDate, HashMap<String, IntVar>> fillSolver(
			Solver solver,
			List<Physician> physicians,
			HashMap<LocalDate, HashMap<String, Integer>> preFill) {
		// WORKING PERIODS HAVE TO BE CALCULATED BEFOER
		if (workingPeriods == null)
			throw new IllegalArgumentException("Agenda has not been calculated");
		
		// CREATES THE MAP OF WORKING
		final HashMap<LocalDate, HashMap<String, IntVar>> workers = new HashMap<>(workingPeriods.size());
		
		// DEFINES THE POSSIBLE PHYSICIANS FOR EACH WORKING PERIOD
		for (final Entry<LocalDate, HashMap<String, IntervalDateTime>> intervalsForDay : workingPeriods.entrySet()) {
						
			// CREATES THE CONSTRAINTS PROGRAMMING VARIABLES FOR THIS DAY
			final HashMap<String, IntVar> workingConstraintsVars = new HashMap<>();
			workers.put(intervalsForDay.getKey(), workingConstraintsVars);
			
			// == 1 - CREATES THE LIST OF PHYSICIANS WORKING THIS DAY ==
			final ArrayList<Integer> workingPhysiciansForDay = new ArrayList<>();
			eachPhysician : for (int i = 0 ; i < physicians.size() ; i++) {

				Physician physician = physicians.get(i);
				
				// CHECK IF PHYSICIAN IS IN PAID VACATIONS
				for (IntervalDate vacation : physician.getPaidVacation()) {
					if (vacation.isInPeriod(intervalsForDay.getKey())) continue eachPhysician;
				}
				
				// CHECK IF PHYSICIAN IS IN UNPAID VACATIONS
				for (IntervalDate vacation : physician.getUnpaidVacation()) {
					if (vacation.isInPeriod(intervalsForDay.getKey())) continue eachPhysician;
				}
		
				// IF WE ARE THERE, PHYSICIAN IS WORKING THIS DATE
				workingPhysiciansForDay.add(i);
			}
			
			// == 2 - FOR EACH INTERVAL IN THIS DAY IN AGENDA, TEST IF SOME OVERRIDES EXIST ==
			eachInterval : for (final Entry<String, IntervalDateTime> intervalForDay : intervalsForDay.getValue().entrySet()) {

				// a - SEE IF WE HAVE A PRESET VALUE IN PREFILL
				if (preFill != null && preFill.get(intervalsForDay.getKey()) != null
						&& preFill.get(intervalsForDay.getKey()).get(intervalForDay.getKey()) != null) {
					workingConstraintsVars.put(intervalForDay.getKey(),
							VariableFactory.fixed(
									intervalsForDay.getKey().toString() + "_" + intervalForDay.getKey(),
									preFill.get(intervalsForDay.getKey()).get(intervalForDay.getKey()),
									solver));
					continue eachInterval;
				} else {
					// b - CHECK IF A PHYSICIAN WAS PREDEFINED FOR THIS INTERVAL
					for (int i = 0 ; i < physicians.size() ; i++) {
						if (physicians.get(i).getWorkedVacs().containsKey(intervalsForDay.getKey())
								&& physicians.get(i).getWorkedVacs().get(intervalsForDay.getKey()).contains(intervalForDay.getKey())) {
							workingConstraintsVars.put(intervalForDay.getKey(),
									VariableFactory.fixed(intervalsForDay.getKey().toString() + "_" + intervalForDay.getKey(), i, solver));
							continue eachInterval;
						}
					}
					// c(1) - IF NO PHYSICIAN FOR THIS DAY, ONLY KEEP THE PHYSICIANS WORKING FOR THIS POSTE
					final ArrayList<Integer> workingPhysiciansForDayAndInterval = new ArrayList<>(workingPhysiciansForDay.size());
					for (final Integer physicianNb : workingPhysiciansForDay) {
						if (!physicians.get(physicianNb).getRefusedPostes().contains(intervalForDay.getKey()))
							workingPhysiciansForDayAndInterval.add(physicianNb);
					}
					// c(2) - TEST WITH THESE PHYSICIANS
					workingConstraintsVars.put(intervalForDay.getKey(),
							VariableFactory.enumerated(intervalsForDay.getKey().toString() + "_" + intervalForDay.getKey(),
									toIntArray(workingPhysiciansForDayAndInterval), solver));
				}
			}
			
			// ADDS THESE CONSTRAINT VARS FOR FEEDBACK
			workers.put(intervalsForDay.getKey(), workingConstraintsVars);
		}
		
		// CREATES THE GENERAL CONSTRAINTS AND APPLY THEM TO THE SOLVER
		for (LocalDate date = interval.getStart() ; !date.isAfter(interval.getEnd()) ; date = date.plusDays(1L)) {
			for (final Constraint constraint : typeJour.getConstraints(date, workers)) {
				solver.post(constraint);
			}
		}
		
		// RETURN THE WORKERS CONSTRAINT VARIABLES
		return workers;
	}
	
	/**
	 * Returns the intervals of work defined for this agenda
	 * @return
	 */
	public HashMap<LocalDate, HashMap<String, IntervalDateTime>> getWorkingPeriods() {
		return workingPeriods;
	}

	/**
	 * Transforms an array of Integer to array of int
	 * @param array
	 * @return
	 */
	private int[] toIntArray(ArrayList<Integer> array) {
		int[] newArray = new int[array.size()];
		for (int i = 0 ; i < array.size() ; i++) {
			newArray[i] = array.get(i);
		}
		return newArray;
	}
}
