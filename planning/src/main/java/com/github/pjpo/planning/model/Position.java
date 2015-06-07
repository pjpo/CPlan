package com.github.pjpo.planning.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
	
	private LocalDate date;
	
	private LocalDateTime startWork;
	
	private LocalDateTime endWork;

	private IntervalDateTime plage;

	private Boolean isWorking;
	
	private Integer workLoad;
	
	private String name;
	
	private IntVar internalChocoRepresentation;
	
	private Physician worker;
		
	public Position(final LocalDate date, final LocalDateTime startWork,
			final LocalDateTime endWork, final IntervalDateTime plage,
			final Boolean isWorking, final Integer workLoad, final String name) {
		this.date = date;
		this.startWork = startWork;
		this.endWork = endWork;
		this.plage = plage;
		this.isWorking = isWorking;
		this.workLoad = workLoad;
		this.name = name;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public LocalDateTime getStartWork() {
		return startWork;
	}

	public void setStartWork(LocalDateTime startWork) {
		this.startWork = startWork;
	}

	public LocalDateTime getEndWork() {
		return endWork;
	}

	public void setEndWork(LocalDateTime endWork) {
		this.endWork = endWork;
	}

	public IntervalDateTime getPlage() {
		return plage;
	}

	public void setPlage(IntervalDateTime plage) {
		this.plage = plage;
	}

	public Boolean getIsWorking() {
		return isWorking;
	}

	public void setIsWorking(Boolean isWorking) {
		this.isWorking = isWorking;
	}

	public Integer getWorkLoad() {
		return workLoad;
	}

	public void setWorkLoad(Integer workLoad) {
		this.workLoad = workLoad;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public IntVar getInternalChocoRepresentation() {
		return internalChocoRepresentation;
	}

	public void setInternalChocoRepresentation(
			IntVar internalChocoRepresentation) {
		this.internalChocoRepresentation = internalChocoRepresentation;
	}
	
	public Physician getWorker() {
		return worker;
	}

	public void setWorker(Physician worker) {
		this.worker = worker;
	}
	
	@Override
	public Object clone() {
		final Position clonedPosition = new Position(date, startWork, endWork, plage, isWorking, workLoad, name);
		clonedPosition.setInternalChocoRepresentation(internalChocoRepresentation);
		clonedPosition.setWorker(worker);
		return clonedPosition;
	}
	
}
