package com.github.aiderpmsi.planning.physician;

public class PhysicianBuilder extends PhysicianBase {
	
	public PhysicianBuilder setName(String name) {
		this.name = name;
		return this;
	}

	public PhysicianBuilder setTimePart(Integer timePart) {
		this.timePart = timePart;
		return this;
	}
	
	public Physician toPhysician() {
		Physician physician = new Physician();
		physician.setName(name);
		physician.setTimePart(timePart);
		return physician;
	}
}
