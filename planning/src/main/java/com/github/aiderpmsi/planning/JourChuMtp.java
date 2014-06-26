package com.github.aiderpmsi.planning;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import solver.Solver;
import solver.constraints.Constraint;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

public class JourChuMtp implements Jour {

	private Ligne[] lignes = new Ligne[] {
			new LunelLigne()
	};
	
	/** Buffers the list of config datas for each day */
	HashMap<LocalDate, HashMap<IntVar>> intVarBuffer = new HashMap<>();
	
	/** Buffers the list of 
	/** List of docs */
	HashMap<Integer, String> docs;
	
	public JourChuMtp(HashMap<Integer, String> docs) {
		this.docs = docs;
	}
	
	@Override
	public List<IntVar> getPlages(LocalDate date, Solver solver) {
		
		List<IntVar> intVars = new ArrayList<>();
		
		// GET PLAGES FROM LIGNES
		for (Ligne ligne : lignes) {
			// GETS THE 
			for (Entry<String, PlageLigne> plageLigne : ligne.getPlages(date).entrySet()) {
				IntVar var = VariableFactory
			}
		}
	}

	@Override
	public List<Constraint> getConstraints(Jour precedingDay, Solver solver) {
		// TODO Auto-generated method stub
		return null;
	}

}
