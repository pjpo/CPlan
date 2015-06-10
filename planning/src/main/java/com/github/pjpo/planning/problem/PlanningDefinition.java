package com.github.pjpo.planning.problem;

import java.util.HashMap;
import java.util.List;

import com.github.pjpo.planning.model.Physician;
import com.github.pjpo.planning.model.PositionCode;
import com.github.pjpo.planning.model.PositionConstraintBase;
import com.github.pjpo.planning.utils.IntervalDate;

/**
 * Definitions of the constraints for the planning :
 *  <ul>
 *   <li>Persons</li>
 *   <li>Working positions</li>
 *   <li>Inter-day and intra-day constraints</li>
 * @author jpc
 *
 */
public class PlanningDefinition {

	/** List of used physicians with their internal indice number*/
	private final HashMap<Integer, Physician> physicians;
	
	/** List of positions definitions */
	private final List<PositionCode> positionsDefinitions;
	
	/** List of intra and interday constraints */
	private final List<PositionConstraintBase> positionsConstraints;
		
	/**
	 * Creates the planning constraints from persons who can work and positions definitions
	 * @param physicians
	 * @param positionsCode
	 */
	public PlanningDefinition(
			final HashMap<Integer, Physician> physicians,
			final List<PositionCode> positionsCode,
			final List<PositionConstraintBase> positionsConstraints) {
		this.physicians = physicians;
		this.positionsDefinitions = positionsCode;
		this.positionsConstraints = positionsConstraints;
	}
	
	public final PlanningForInterval generatePlanningImplementation(final IntervalDate interval) {
		return new PlanningForInterval(interval, physicians, positionsDefinitions, positionsConstraints);
	}
	
	public HashMap<Integer, Physician> getPhysicians() {
		return physicians;
	}
	
	public List<PositionCode> getPositionsDefinitions() {
		return positionsDefinitions;
	}
	
	public List<PositionConstraintBase> getPositionsConstraints() {
		return positionsConstraints;
	}
}