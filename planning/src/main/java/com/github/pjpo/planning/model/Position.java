package com.github.pjpo.planning.model;

import org.chocosolver.solver.variables.IntVar;

import com.github.pjpo.planning.utils.IntervalDateTime;

/**
 * Represents a position in planning, having multiple fields :
 * <ul>
 *   <li>External datas : name of position, date, workload, ... </li>
 *   <li>Internal datas : an {@link IntVar} which represents the internal representation in Choco Solver</li>
 * </ul>
 * @author jp@dm.lan
 *
 */
public class Position implements Cloneable {

	/** Name of this position */
	private String name;

	/** Bounds of the position */
	private IntervalDateTime bounds;

	/** Is the position active this day or not */
	private Boolean isActive;
	
	/** Workload for this position (can be different from time bounds) */
	private Integer workLoad;
	
	/** Internal representation for Choco for this position */
	private IntVar internalChocoRepresentation;
	
	/** Worker at this position */
	private Worker worker;
		
	public IntervalDateTime getBounds() {
		return bounds;
	}

	public void setBounds(final IntervalDateTime bounds) {
		this.bounds = bounds;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(final Boolean isActive) {
		this.isActive = isActive;
	}

	public Integer getWorkLoad() {
		return workLoad;
	}

	public void setWorkLoad(final Integer workLoad) {
		this.workLoad = workLoad;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public IntVar getInternalChocoRepresentation() {
		return internalChocoRepresentation;
	}

	public void setInternalChocoRepresentation(final IntVar internalChocoRepresentation) {
		this.internalChocoRepresentation = internalChocoRepresentation;
	}
	
	public Worker getWorker() {
		return worker;
	}

	public void setWorker(final Worker worker) {
		this.worker = worker;
	}
	
	@Override
	public Object clone() {
		final Position clonedPosition = new Position();
		clonedPosition.setBounds(bounds);
		clonedPosition.setIsActive(isActive);
		clonedPosition.setWorkLoad(workLoad);
		clonedPosition.setName(name);
		clonedPosition.setInternalChocoRepresentation(internalChocoRepresentation);
		clonedPosition.setWorker(worker);
		return clonedPosition;
	}
	
}
