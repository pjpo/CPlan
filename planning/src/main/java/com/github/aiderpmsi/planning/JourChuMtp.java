package com.github.aiderpmsi.planning;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

public class JourChuMtp implements Jour {

	private Ligne[] lignes = new Ligne[] {
			new LunelLigne()
	};
	
	/** Buffers the list of config datas for each day */
	HashMap<LocalDate, HashMap<String, Plage>> plagesBuffer = new HashMap<>();
	
	/** Buffers the list of intVars for each day */
	HashMap<LocalDate, HashMap<String, IntVar>> intVarsBuffer = new LinkedHashMap<>();
	
	/** List of docs */
	HashMap<Integer, String> docs;
	
	public JourChuMtp(HashMap<Integer, String> docs) {
		this.docs = docs;
	}
	
	@Override
	public void generatePlages(LocalDate date, Solver solver) {
		
		HashMap<String, Plage> plagesForDayBuffer = null;
		HashMap<String, IntVar> varsForDayBuffer = null;
		
		// CREATE THE BUFFERS FOR THIs DATE IF NOT EXIST
		if ((plagesForDayBuffer = plagesBuffer.get(date)) == null) {
			plagesForDayBuffer = new HashMap<>();
			varsForDayBuffer = new HashMap<>();
			plagesBuffer.put(date, plagesForDayBuffer);
			intVarsBuffer.put(date, varsForDayBuffer);
		}
		// ELSE THROW ILLEGAL ARGUMENT : DATE HAS ALREADY BEEN GENERATED
		else {
			throw new IllegalArgumentException("date " + date + " has already been generated");
		}

		// CREATES ONCE THE LIST OF DOCS (PREVENTS IT FROM BEEING RECALCULATED EVERY ITERATION)
		int[] docsIndices = new int[docs.size()];
		int position = 0;
		for (Integer docKey : docs.keySet()) {
			docsIndices[position++] = docKey;
		}
		
		// GET PLAGES FROM LIGNES
		for (Ligne ligne : lignes) {
			// GETS THE PLAGES FOR THIS LIGNE
			HashMap<String, Plage> plagesForLigne = ligne.getPlages(date);

			// BUFFER EACH ENTRY AND CREATE THE ASSOCIATED INTVAR
			for (Entry<String, Plage> plageForLigne : plagesForLigne.entrySet()) {
				IntVar varForPlage = VariableFactory.enumerated(
						date + "_" + plageForLigne.getKey(),
						docsIndices,
						solver);
				plagesForDayBuffer.put(plageForLigne.getKey(), plageForLigne.getValue());
				varsForDayBuffer.put(plageForLigne.getKey(), varForPlage);
			}
			
			// NOW WE FILLED PLAGESBUFFER AND INTVARSBUFFER WITH NEEDED PLAGES AND INTVARS
		}
	}

	@Override
	public void generateConstraints(LocalDate date, Solver solver) {
		// VERIFY THAT THIS DAY HAS BEEN GENERATED
		HashMap<String, IntVar> toDay = intVarsBuffer.get(date);
		if (toDay == null) {
			throw new IllegalArgumentException("date " + date + " has not been generated by getPlages()");
		} else {

			// EQUALITY BETWEEN LUNEL_1 AND LUNEL_2 == MEDECIN DE LUNEL IDEM JOUR ET GARDE
			{
				IntVar lunel1 = toDay.get("Lunel_1");
				IntVar lunel2 = toDay.get("Lunel_2");
				if (lunel1 != null && lunel2 != null) {
					solver.post(IntConstraintFactory.arithm(lunel1, "=", lunel2));
				}
			}
			
			// IMPOSIBLE TO WORK CONCURRENTLY (MUTUALLY EXCLUSIVE)
			// - FMC_1
			// - FMC_2 DAY BEFORE
			// - LUNEL_1 (HAS A CONSTRAINT LUNEL_1 = LUNEL_2)
			// - LUNEL_2 DAY BEFORE
			{
				LinkedList<IntVar> concurrent = new LinkedList<>();
				HashMap<String, IntVar> yesterday = intVarsBuffer.get(date.minusDays(1));
				IntVar fmc1 = toDay.get("Fmc_1");
				if (fmc1 != null) concurrent.add(fmc1);
				IntVar fmc2yesterday = yesterday == null ? null : yesterday.get("Fmc_2");
				if (fmc2yesterday != null) concurrent.add(fmc2yesterday);
				IntVar lunel1 = toDay.get("Lunel_1");
				if (lunel1 != null) concurrent.add(lunel1);
				IntVar lunel2yesterday = yesterday == null ? null : yesterday.get("Lunel_2");
				if (lunel2yesterday != null) concurrent.add(lunel2yesterday);
				
				solver.post(IntConstraintFactory.alldifferent(concurrent.toArray(new IntVar[concurrent.size()])));
			}
			
			// IMPOSIBLE TO WORK CONCURRENTLY (MUTUALLY EXCLUSIVE)
			// - FMC_2
			// - FMC_2 DAY BEFORE
			// - LUNEL_2 (HAS A CONSTRAINT LUNEL_1 = LUNEL_2)
			// - LUNEL_2 DAY BEFORE
			{
				LinkedList<IntVar> concurrent = new LinkedList<>();
				HashMap<String, IntVar> yesterday = intVarsBuffer.get(date.minusDays(1));
				IntVar fmc2 = toDay.get("Fmc_2");
				if (fmc2 != null) concurrent.add(fmc2);
				IntVar fmc2yesterday = yesterday == null ? null : yesterday.get("Fmc_2");
				if (fmc2yesterday != null) concurrent.add(fmc2yesterday);
				IntVar lunel2 = toDay.get("Lunel_2");
				if (lunel2 != null) concurrent.add(lunel2);
				IntVar lunel2yesterday = yesterday == null ? null : yesterday.get("Lunel_2");
				if (lunel2yesterday != null) concurrent.add(lunel2yesterday);
				
				solver.post(IntConstraintFactory.alldifferent(concurrent.toArray(new IntVar[concurrent.size()])));
			}

		}
	}

	public HashMap<LocalDate, HashMap<String, IntVar>> getIntVarsBuffer() {
		return intVarsBuffer;
	}

	public int[] intArrayListToPrimitive(List<Integer> list) {
		int[] newArray = new int[list.size()];
		int position = 0;
		for (Integer element : list) {
			newArray[position++] = element;
		}
		return newArray;
	}
}
