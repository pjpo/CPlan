package com.github.pjpo.planning;

import java.util.List;
import com.github.pjpo.planning.constraintsrules.PositionConstraintBase;
import com.github.pjpo.planning.model.Physician;
import com.github.pjpo.planning.model.PositionCode;
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
public class PlanningConstraints {

	/** List of used physicians */
	private final List<Physician> physicians;
	
	/** List of positions definitions */
	private final List<PositionCode> positionsDefinitions;
	
	/** List of intra and interday constraints */
	private final List<PositionConstraintBase> positionsConstraints;
		
	/**
	 * Creates the planning constraints from persons who can work and positions definitions
	 * @param physicians
	 * @param positionsCode
	 */
	public PlanningConstraints(
			final List<Physician> physicians,
			final List<PositionCode> positionsCode,
			final List<PositionConstraintBase> positionsConstraints) {
		this.physicians = physicians;
		this.positionsDefinitions = positionsCode;
		this.positionsConstraints = positionsConstraints;
	}
	
	public final PlanningImplementation generatePlanningImplementation(final IntervalDate interval) {
		return new PlanningImplementation(interval, this);
	}
	
	public List<Physician> getPhysicians() {
		return physicians;
	}
	
	public List<PositionCode> getPositionsDefinitions() {
		return positionsDefinitions;
	}
	
	public List<PositionConstraintBase> getPositionsConstraints() {
		return positionsConstraints;
	}
}