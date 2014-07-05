package com.github.pjpo.planning.jours;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.variables.IntVar;

import com.github.pjpo.planning.lignes.AgdeLigne;
import com.github.pjpo.planning.lignes.FcLigne;
import com.github.pjpo.planning.lignes.FmcLigne;
import com.github.pjpo.planning.lignes.GangesLigne;
import com.github.pjpo.planning.lignes.Ligne;
import com.github.pjpo.planning.lignes.LunelLigne;
import com.github.pjpo.planning.lignes.RegulLigne;
import com.github.pjpo.planning.lignes.SILigne;
import com.github.pjpo.planning.lignes.SmurLigne;
import com.github.pjpo.planning.lignes.UHCDLigne;
import com.github.pjpo.planning.lignes.UtecLigne;
import com.github.pjpo.planning.utils.IntervalDateTime;

public class JourChuMtp implements Jour {

	private Ligne[] lignes = new Ligne[] {
			new LunelLigne(),
			new GangesLigne(),
			new AgdeLigne(),
			new RegulLigne(),
			new SmurLigne(1),
			new SmurLigne(2),
			new UHCDLigne(),
			new SILigne(),
			new FmcLigne(),
			new FcLigne(),
			new UtecLigne()
			
	};
	
	@Override
	public HashMap<String, IntervalDateTime> getPlages(LocalDate date) {
		// THE MAP
		HashMap<String, IntervalDateTime> plages = new HashMap<>();
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

			// EQUALITY BETWEEN GANGES_1 AND GANGES_2 == MEDECIN DE GANGES IDEM JOUR ET GARDE
			{
				IntVar ganges1 = toDay.get("Ganges_1");
				IntVar ganges2 = toDay.get("Ganges_2");
				if (ganges1 != null && ganges2 != null) {
					constraints.add(IntConstraintFactory.arithm(ganges1, "=", ganges2));
				}
			}

			// EQUALITY BETWEEN AGDE_1 AND AGDE_2 == MEDECIN DE AGDE IDEM JOUR ET GARDE
			{
				IntVar agde1 = toDay.get("Agde_1");
				IntVar agde2 = toDay.get("Agde_2");
				if (agde1 != null && agde2 != null) {
					constraints.add(IntConstraintFactory.arithm(agde1, "=", agde2));
				}
			}
			
			// IMPOSIBLE TO WORK CONCURRENTLY (MUTUALLY EXCLUSIVE)
			// - LUNEL_1 (HAS A CONSTRAINT LUNEL_1 = LUNEL_2)
			// - LUNEL_2 DAY BEFORE
			// - GANGES_1 (HAS A CONSTRAINT GANGES_1 = GANGES_2)
			// - GANGES_2 DAY BEFORE
			// - AGDE_1 (HAS A CONSTRAINT AGDE_1 = AGDE_2)
			// - AGDE_2 DAY BEFORE
			// - REGUL_1
			// - REGUL_2
			// - REGUL_3 BEFORE
			// - SMUR1_1
			// - SMUR1_2 DAY BEFORE
			// - SMUR2_1
			// - SMUR2_2 DAY BEFORE
			// - UHCD_1
			// - SI_1
			// - SI_2 DAY BEFORE
			// - FMC_1
			// - FMC_2 DAY BEFORE
			// - FC_1
			// - UTEC_1
			// - UTEC_2 DAY BEFORE
			{
				LinkedList<IntVar> concurrent = new LinkedList<>();
				HashMap<String, IntVar> yesterday = workers.get(date.minusDays(1));
				// LUNEL
				IntVar lunel1 = toDay.get("Lunel_1");
				if (lunel1 != null) concurrent.add(lunel1);
				IntVar lunel2yesterday = yesterday == null ? null : yesterday.get("Lunel_2");
				if (lunel2yesterday != null) concurrent.add(lunel2yesterday);
				// GANGES
				IntVar ganges1 = toDay.get("Ganges_1");
				if (ganges1 != null) concurrent.add(ganges1);
				IntVar ganges2yesterday = yesterday == null ? null : yesterday.get("Ganges_2");
				if (ganges2yesterday != null) concurrent.add(ganges2yesterday);
				// AGDE
				IntVar agde1 = toDay.get("Agde_1");
				if (agde1 != null) concurrent.add(agde1);
				IntVar agde2yesterday = yesterday == null ? null : yesterday.get("Agde_2");
				if (agde2yesterday != null) concurrent.add(agde2yesterday);
				// REGUL
				IntVar regul1 = toDay.get("Regul_1");
				if (regul1 != null) concurrent.add(regul1);
				IntVar regul2 = toDay.get("Regul_2");
				if (regul2 != null) concurrent.add(regul2);
				IntVar regul3yesterday = yesterday == null ? null : yesterday.get("Regul_3");
				if (regul3yesterday != null) concurrent.add(regul3yesterday);
				// SMUR1
				IntVar smur11 = toDay.get("Smur1_1");
				if (smur11 != null) concurrent.add(smur11);
				IntVar smur12 = toDay.get("Smur1_2");
				if (smur12 != null) concurrent.add(smur12);
				// SMUR2
				IntVar smur21 = toDay.get("Smur2_1");
				if (smur21 != null) concurrent.add(smur21);
				IntVar smur22 = toDay.get("Smur2_2");
				if (smur22 != null) concurrent.add(smur22);
				// UHCD
				IntVar uhcd1 = toDay.get("Uhcd_1");
				if (uhcd1 != null) concurrent.add(uhcd1);
				// SI
				IntVar si1 = toDay.get("Si_1");
				if (si1 != null) concurrent.add(si1);
				IntVar si2yesterday = yesterday == null ? null : yesterday.get("Si_2");
				if (si2yesterday != null) concurrent.add(si2yesterday);
				// FMC
				IntVar fmc1 = toDay.get("Fmc_1");
				if (fmc1 != null) concurrent.add(fmc1);
				IntVar fmc2yesterday = yesterday == null ? null : yesterday.get("Fmc_2");
				if (fmc2yesterday != null) concurrent.add(fmc2yesterday);
				// FC
				IntVar fc1 = toDay.get("Fc_1");
				if (fc1 != null) concurrent.add(fc1);
				// UTEC
				IntVar utec1 = toDay.get("Utec_1");
				if (utec1 != null) concurrent.add(utec1);
				IntVar utec2yesterday = yesterday == null ? null : yesterday.get("Utec_2");
				if (utec2yesterday != null) concurrent.add(utec2yesterday);
								
				if (concurrent.size() >= 2)
					constraints.add(IntConstraintFactory.alldifferent(concurrent.toArray(new IntVar[concurrent.size()])));
			}
			
			// IMPOSIBLE TO WORK CONCURRENTLY (MUTUALLY EXCLUSIVE)
			// - LUNEL_2 (HAS A CONSTRAINT LUNEL_1 = LUNEL_2)
			// - LUNEL_2 DAY BEFORE
			// - GANGES_2 (HAS A CONSTRAINT GANGES_1 = GANGES_2)
			// - GANGES_2 DAY BEFORE
			// - AGDE_2 (HAS A CONSTRAINT AGDE_1 = AGDE_2)
			// - AGDE_2 DAY BEFORE
			// - REGUL_2
			// - REGUL_3
			// - REGUL_3 DAY BEFORE
			// - SMUR1_2
			// - SMUR1_2 DAY BEFORE
			// - SMUR2_2
			// - SMUR2_2 DAY BEFORE
			// - SI_2
			// - SI_2 DAY BEFORE
			// - FMC_2
			// - FMC_2 DAY BEFORE
			// - UTEC_2
			// - UTEC_2 DAY BEFORE
			{
				LinkedList<IntVar> concurrent = new LinkedList<>();
				HashMap<String, IntVar> yesterday = workers.get(date.minusDays(1));
				// LUNEL
				IntVar lunel2 = toDay.get("Lunel_2");
				if (lunel2 != null) concurrent.add(lunel2);
				IntVar lunel2yesterday = yesterday == null ? null : yesterday.get("Lunel_2");
				if (lunel2yesterday != null) concurrent.add(lunel2yesterday);
				// GANGES
				IntVar ganges2 = toDay.get("Ganges_2");
				if (ganges2 != null) concurrent.add(ganges2);
				IntVar ganges2yesterday = yesterday == null ? null : yesterday.get("Ganges_2");
				if (ganges2yesterday != null) concurrent.add(ganges2yesterday);
				// AGDE
				IntVar agde2 = toDay.get("Agde_2");
				if (agde2 != null) concurrent.add(agde2);
				IntVar agde2yesterday = yesterday == null ? null : yesterday.get("Agde_2");
				if (agde2yesterday != null) concurrent.add(agde2yesterday);
				// REGUL
				IntVar regul2 = toDay.get("Regul_2");
				if (regul2 != null) concurrent.add(regul2);
				IntVar regul3 = toDay.get("Regul_3");
				if (regul3 != null) concurrent.add(regul3);
				IntVar regul3yesterday = yesterday == null ? null : yesterday.get("Regul_3");
				if (regul3yesterday != null) concurrent.add(regul3yesterday);
				// SMUR1
				IntVar smur12 = toDay.get("Smur1_2");
				if (smur12 != null) concurrent.add(smur12);
				IntVar smur12yesterday = yesterday == null ? null : yesterday.get("Smur1_2");
				if (smur12yesterday != null) concurrent.add(smur12yesterday);
				// SMUR2
				IntVar smur22 = toDay.get("Smur2_2");
				if (smur22 != null) concurrent.add(smur22);
				IntVar smur22yesterday = yesterday == null ? null : yesterday.get("Smur2_2");
				if (smur22yesterday != null) concurrent.add(smur22yesterday);
				// SI
				IntVar si2 = toDay.get("Si_2");
				if (si2 != null) concurrent.add(si2);
				IntVar si2yesterday = yesterday == null ? null : yesterday.get("Si_2");
				if (si2yesterday != null) concurrent.add(si2yesterday);
				// FMC
				IntVar fmc2 = toDay.get("Fmc_2");
				if (fmc2 != null) concurrent.add(fmc2);
				IntVar fmc2yesterday = yesterday == null ? null : yesterday.get("Fmc_2");
				if (fmc2yesterday != null) concurrent.add(fmc2yesterday);
				// UTEC
				IntVar utec2 = toDay.get("Utec_2");
				if (utec2 != null) concurrent.add(utec2);
				IntVar utec2yesterday = yesterday == null ? null : yesterday.get("Utec_2");
				if (utec2yesterday != null) concurrent.add(utec2yesterday);
				
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
