package com.github.pjpo.planning.jours;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.variables.IntVar;

import com.github.pjpo.planning.lignes.FmcLigne;
import com.github.pjpo.planning.lignes.Ligne;
import com.github.pjpo.planning.lignes.LunelLigne;
import com.github.pjpo.planning.lignes.Plage;
import com.github.pjpo.planning.lignes.SILigne;

public class JourChuMtp implements Jour {

	private Ligne[] lignes = new Ligne[] {
			new LunelLigne(),
			new FmcLigne(),
			new SILigne()
	};
	
	@Override
	public HashMap<String, Plage> getPlages(LocalDate date) {
		// THE MAP
		HashMap<String, Plage> plages = new HashMap<>();
		// GET PLAGES FROM LIGNES AND CONCATS THEM INTO HASHMAP
		for (Ligne ligne : lignes) {
			ligne.getPlages(date).forEach(
					(key, value) -> plages.put(key, value));
		}
		return plages;
	}

	@Override
	public List<Constraint> getConstraints(
			LocalDate date, HashMap<LocalDate, HashMap<String, IntVar>> workers) {

		List<Constraint> constraints = new ArrayList<>();

		HashMap<String, IntVar> toDay = workers.get(date);
		if (toDay == null) {
			throw new IllegalArgumentException("date " + date + " does not exist in agenda");
		} else {

			// EQUALITY BETWEEN LUNEL_1 AND LUNEL_2 == MEDECIN DE LUNEL IDEM JOUR ET GARDE
			{
				IntVar lunel1 = toDay.get("Lunel_1");
				IntVar lunel2 = toDay.get("Lunel_2");
				if (lunel1 != null && lunel2 != null) {
					constraints.add(IntConstraintFactory.arithm(lunel1, "=", lunel2));
				}
			}

			// IMPOSSIBLE TO WORK ON FMC1 AND FMC2
			{
				IntVar fmc1 = toDay.get("Fmc_1");
				IntVar fmc2 = toDay.get("Fmc_2");
				if (fmc1 != null && fmc2 != null) {
					constraints.add(IntConstraintFactory.arithm(fmc1, "!=", fmc2));
				}
					
			}
			
			// IMPOSIBLE TO WORK CONCURRENTLY (MUTUALLY EXCLUSIVE)
			// - FMC_1
			// - FMC_2 DAY BEFORE
			// - LUNEL_1 (HAS A CONSTRAINT LUNEL_1 = LUNEL_2)
			// - LUNEL_2 DAY BEFORE
			// - SI_1
			// - SI_2 DAY BEFORE
			{
				LinkedList<IntVar> concurrent = new LinkedList<>();
				HashMap<String, IntVar> yesterday = workers.get(date.minusDays(1));
				IntVar fmc1 = toDay.get("Fmc_1");
				if (fmc1 != null) concurrent.add(fmc1);
				IntVar fmc2yesterday = yesterday == null ? null : yesterday.get("Fmc_2");
				if (fmc2yesterday != null) concurrent.add(fmc2yesterday);
				IntVar lunel1 = toDay.get("Lunel_1");
				if (lunel1 != null) concurrent.add(lunel1);
				IntVar lunel2yesterday = yesterday == null ? null : yesterday.get("Lunel_2");
				if (lunel2yesterday != null) concurrent.add(lunel2yesterday);
				IntVar si1 = toDay.get("Si_1");
				if (si1 != null) concurrent.add(si1);
				IntVar si2yesterday = yesterday == null ? null : yesterday.get("Si_2");
				if (si2yesterday != null) concurrent.add(si2yesterday);
				
				if (concurrent.size() >= 2)
					constraints.add(IntConstraintFactory.alldifferent(concurrent.toArray(new IntVar[concurrent.size()])));
			}
			
			// IMPOSIBLE TO WORK CONCURRENTLY (MUTUALLY EXCLUSIVE)
			// - FMC_2
			// - FMC_2 DAY BEFORE
			// - LUNEL_2 (HAS A CONSTRAINT LUNEL_1 = LUNEL_2)
			// - LUNEL_2 DAY BEFORE
			// - SI_2
			// - SI_2 DAY BEFORE
			{
				LinkedList<IntVar> concurrent = new LinkedList<>();
				HashMap<String, IntVar> yesterday = workers.get(date.minusDays(1));
				IntVar fmc2 = toDay.get("Fmc_2");
				if (fmc2 != null) concurrent.add(fmc2);
				IntVar fmc2yesterday = yesterday == null ? null : yesterday.get("Fmc_2");
				if (fmc2yesterday != null) concurrent.add(fmc2yesterday);
				IntVar lunel2 = toDay.get("Lunel_2");
				if (lunel2 != null) concurrent.add(lunel2);
				IntVar lunel2yesterday = yesterday == null ? null : yesterday.get("Lunel_2");
				if (lunel2yesterday != null) concurrent.add(lunel2yesterday);
				IntVar si2 = toDay.get("Si_2");
				if (si2 != null) concurrent.add(si2);
				IntVar si2yesterday = yesterday == null ? null : yesterday.get("Si_2");
				if (si2yesterday != null) concurrent.add(si2yesterday);
				
				if (concurrent.size() >= 2)
					constraints.add(IntConstraintFactory.alldifferent(concurrent.toArray(new IntVar[concurrent.size()])));
			}
			
			// MUTUALLY EXCLUSIVE :
			// - FMC_1
			// - FMC_2
			// - SI_1
			// - SI_2
			{
				LinkedList<IntVar> concurrent = new LinkedList<>();
				IntVar fmc1 = toDay.get("Fmc_1");
				if (fmc1 != null) concurrent.add(fmc1);
				IntVar fmc2 = toDay.get("Fmc_2");
				if (fmc2 != null) concurrent.add(fmc2);
				IntVar si1 = toDay.get("Si_1");
				if (si1 != null) concurrent.add(si1);
				IntVar si2 = toDay.get("Si_2");
				if (si2 != null) concurrent.add(si2);
				
				if (concurrent.size() >= 2)
					constraints.add(IntConstraintFactory.alldifferent(concurrent.toArray(new IntVar[concurrent.size()])));
			}
		
			return constraints;
		}
	}

}
