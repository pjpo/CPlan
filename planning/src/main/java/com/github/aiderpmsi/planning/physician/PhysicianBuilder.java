package com.github.aiderpmsi.planning.physician;

import java.time.LocalDate;

public class PhysicianBuilder extends PhysicianBase {
	
	public PhysicianBuilder setName(String name) {
		this.name = name;
		return this;
	}

	public PhysicianBuilder setTimePart(Integer timePart) {
		this.timePart = timePart;
		return this;
	}
	
	public PhysicianBuilder setWorkStart(LocalDate workStart) {
		this.workStart = workStart;
		return this;
	}
	
	public PhysicianBuilder setWorkEnd(LocalDate workEnd) {
		this.workEnd = workEnd;
		return this;
	}

	public Physician toPhysician() {
		Physician physician = new Physician();
		physician.setName(name);
		physician.setTimePart(timePart);
		return physician;
	}
}
