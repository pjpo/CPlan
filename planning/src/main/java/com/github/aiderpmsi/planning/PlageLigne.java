package com.github.aiderpmsi.planning;

import java.time.LocalDateTime;

import solver.variables.IntVar;

public class PlageLigne extends Plage {

	private String name;
	
	private IntVar intVar;
	
	public IntVar getIntVar() {
		return intVar;
	}

	public void setIntVar(IntVar intVar) {
		this.intVar = intVar;
	}

	public PlageLigne(String name) {
		this.name = name;
	}
	
	public PlageLigne(String name, LocalDateTime start, LocalDateTime end) {
		super(start, end);
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getEnd() == null) ? 0 : getEnd().hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((getStart() == null) ? 0 : getStart().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PlageLigne other = (PlageLigne) obj;
		if (getEnd() == null) {
			if (other.getEnd() != null)
				return false;
		} else if (!getEnd().equals(other.getEnd()))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (getStart() == null) {
			if (other.getStart() != null)
				return false;
		} else if (!getStart().equals(other.getStart()))
			return false;
		return true;
	}

}
