package com.github.pjpo.planning.physician;

import java.time.LocalDate;
import java.util.ArrayList;

import com.github.pjpo.planning.utils.IntervalDateTime;

public class PhysicianBuilder extends PhysicianBase {
	
	public PhysicianBuilder setName(String name) {
		this.name = name;
		return this;
	}

	public PhysicianBuilder setTimePart(Integer timePart) {
		this.timePart = timePart;
		return this;
	}
	
	public PhysicianBuilder addPaidVacation(IntervalDateTime period) {
		paidVacation.add(period);
		return this;
	}
	
	public PhysicianBuilder addUnpaidVacation(IntervalDateTime period) {
		unpaidVacation.add(period);
		return this;
	}
	
	public PhysicianBuilder addWorkedVac(LocalDate date, String poste) {
		ArrayList<String> postes;
		if ((postes = workedVacs.get(date)) == null) {
			postes = new ArrayList<>();
			workedVacs.put(date, postes);
		}
		postes.add(poste);
		return this;
	}

	public PhysicianBuilder addRefusedPoste(String poste) {
		refusedPostes.add(poste);
		return this;
	}

	public Physician toPhysician() {
		Physician physician = new Physician();
		physician.setName(name);
		physician.setTimePart(timePart);
		physician.setPaidVacation(paidVacation);
		physician.setUnpaidVacation(unpaidVacation);
		physician.setWorkedVacs(workedVacs);
		physician.setRefusedPostes(refusedPostes);
		return physician;
	}
}
