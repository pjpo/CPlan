package com.github.pjpo.planning.jours;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import solver.Solver;
import solver.constraints.Constraint;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import com.github.pjpo.planning.lignes.Position;
import com.github.pjpo.planning.physician.Physician;
import com.github.pjpo.planning.utils.IntervalDate;

/**
 * Creates and keeps in memory the working periods for an interval and a day configuration
 * @author jpc
 *
 */
public class Agenda {

	/** List of days and corresponding positions */
	private final HashMap<LocalDate, HashMap<String, Position>> workingPositions = new HashMap<>();
	
	/** Interval of agenda */
	private final IntervalDate interval;

	/**
	 * Creates an agenda for this kind of jour and a defined interval
	 * @param typeJour definition of the positions
	 * @param intervalDate
	 */
	public Agenda(final IntervalDate intervalDate) {
		if (intervalDate.getStart() == null || intervalDate.getEnd() == null)
			throw new IllegalArgumentException("interval must be finite");
		this.interval = intervalDate;
	}

	/**
	 * Fills the days of agenda with possible positions
	 */
	public void calculateWorkingPeriods() {
		workingPositions.clear();
		for (LocalDate date = interval.getStart() ; !date.isAfter(interval.getEnd()) ; date = date.plusDays(1L)) {
			workingPositions.put(date, JourChuMtp.getPositions(date));
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
			final Solver solver,
			final List<Physician> physicians,
			final HashMap<LocalDate, HashMap<String, Integer>> preFill) {
		// WORKING PERIODS HAVE TO BE CALCULATED BEFOER
		if (workingPositions == null)
			throw new IllegalArgumentException("Agenda has not been calculated");
		
		// CREATES THE MAP OF WORKING
		final HashMap<LocalDate, HashMap<String, IntVar>> workers = new HashMap<>(workingPositions.size());
		
		// == 1 - Check for each day
		for (final Entry<LocalDate, HashMap<String, Position>> positionsInDayEntry : workingPositions.entrySet()) {
						
			// CREATES THE CONSTRAINTS PROGRAMMING VARIABLES FOR THIS DAY
			final HashMap<String, IntVar> workingConstraintsVars = new HashMap<>();
			workers.put(positionsInDayEntry.getKey(), workingConstraintsVars);
			
			// == 2 - Check each position of this day
			eachposition : for (final Entry<String, Position> positionEntry : positionsInDayEntry.getValue().entrySet()) {

				// == a - Check if we have already a person positioned at this day in prefill
				if (preFill != null && preFill.get(positionsInDayEntry.getKey()) != null
						&& preFill.get(positionsInDayEntry.getKey()).get(positionEntry.getKey()) != null) {
					// Add the constraint for the solver
					workingConstraintsVars.put(
							positionEntry.getKey(),
							VariableFactory.fixed(
									positionsInDayEntry.getKey().toString() + "_" + positionEntry.getKey(),
									preFill.get(positionsInDayEntry.getKey()).get(positionEntry.getKey()),
									solver
							));
					// look for next position in day
					continue eachposition;
				}
				
					
				// ==> continue here
				// == 2 - Check for each physician if he MUST work at this position this day
				for (int i = 0 ; i < physicians.size() ; i++) {
					
					if (preFill != null && preFill.get(intervalsForDay.getKey()) != null
							&& preFill.get(intervalsForDay.getKey()).get(position.getName()) != null) {
						workingConstraintsVars.put(position.getName(),
								VariableFactory.fixed(
										intervalsForDay.getKey().toString() + "_" + position.getName(),
										preFill.get(intervalsForDay.getKey()).get(position.getName()),
										solver));

				}
			}
			
			// == 1 - CREATES THE LIST OF PHYSICIANS WORKING THIS DAY ==
			final LinkedList<Integer> workingPhysiciansForDay = new LinkedList<>();
			eachPhysician : for (int i = 0 ; i < physicians.size() ; i++) {

				final Physician physician = physicians.get(i);
				
				// CHECK IF PHYSICIAN IS IN PAID VACATIONS
				for (final IntervalDate vacation : physician.getPaidVacation()) {
					if (vacation.isInPeriod(intervalsForDay.getKey())) continue eachPhysician;
				}
				
				// CHECK IF PHYSICIAN IS IN UNPAID VACATIONS
				for (final IntervalDate vacation : physician.getUnpaidVacation()) {
					if (vacation.isInPeriod(intervalsForDay.getKey())) continue eachPhysician;
				}
		
				// IF WE ARE THERE, PHYSICIAN IS WORKING THIS DATE
				workingPhysiciansForDay.add(i);
			}
			
			// == 2 - FOR EACH POSITION FOR THIS DAY, TEST IF SOME PHYSICIANS ARE PREDEFINED AS BEING WORKING ==
			eachInterval : for (final Position position : intervalsForDay.getValue().values()) {

				// a - SEE IF WE HAVE A PRESET VALUE IN PREFILL
				if (preFill != null && preFill.get(intervalsForDay.getKey()) != null
						&& preFill.get(intervalsForDay.getKey()).get(position.getName()) != null) {
					workingConstraintsVars.put(position.getName(),
							VariableFactory.fixed(
									intervalsForDay.getKey().toString() + "_" + position.getName(),
									preFill.get(intervalsForDay.getKey()).get(position.getName()),
									solver));
					continue eachInterval;
				} else {
					// b - CHECK IF A PHYSICIAN WAS PREDEFINED FOR THIS INTERVAL
					for (int i = 0 ; i < physicians.size() ; i++) {
						if (physicians.get(i).getWorkedVacs().containsKey(intervalsForDay.getKey())
								&& physicians.get(i).getWorkedVacs().get(intervalsForDay.getKey()).contains(position.getName())) {
							workingConstraintsVars.put(position.getName(),
									VariableFactory.fixed(intervalsForDay.getKey().toString() + "_" + position.getName(), i, solver));
							continue eachInterval;
						}
					}
					// c(1) - IF NO PHYSICIAN FOR THIS DAY, ONLY KEEP THE PHYSICIANS WORKING FOR THIS POSTE
					final LinkedList<Integer> workingPhysiciansForDayAndInterval = new LinkedList<>();
					for (final Integer physicianNb : workingPhysiciansForDay) {
						if (!physicians.get(physicianNb).getRefusedPostes().contains(position.getName()))
							workingPhysiciansForDayAndInterval.add(physicianNb);
					}
					// c(2) - TEST WITH THESE PHYSICIANS
					workingConstraintsVars.put(position.getName(),
							VariableFactory.enumerated(intervalsForDay.getKey().toString() + "_" + position.getName(),
									toIntArray(workingPhysiciansForDayAndInterval), solver));
				}
			}
			
			// ADDS THESE CONSTRAINT VARS FOR FEEDBACK
			workers.put(intervalsForDay.getKey(), workingConstraintsVars);
		}
		
		// CREATES THE GENERAL CONSTRAINTS AND APPLY THEM TO THE SOLVER
		for (LocalDate date = interval.getStart() ; !date.isAfter(interval.getEnd()) ; date = date.plusDays(1L)) {
			for (final Constraint constraint : JourChuMtp.getConstraints(date, workers)) {
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
	public HashMap<LocalDate, HashMap<String, Position>> getWorkingPositions() {
		return workingPositions;
	}

	/**
	 * Transforms a collection of Integer to array of int
	 * @param array
	 * @return
	 */
	private int[] toIntArray(Collection<Integer> collection) {
		final int[] newArray = new int[collection.size()];
		int i = 0;
		for (final Integer integer: collection) {
			newArray[i++] = integer;
		}
		return newArray;
	}
}
