package com.github.pjpo.planning.jours;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.variables.IntVar;

import com.github.pjpo.planning.lignes.Position;

public class JourChuMtp {

	
	public static HashMap<String, Position> getPositions(LocalDate date) {
		
		HashMap<String, Position> positions = new HashMap<>();
		
		// GET POSITIONS DEFINITIONS AND GENERATE A LIST OF ACCEPTABLE POSITIONS FOR THIS LOCALDATE
		try (
				InputStream resource = JourChuMtp.class.getResourceAsStream("/com/github/pjpo/planning/positions.cfg");
				InputStreamReader isr = new InputStreamReader(resource);
				BufferedReader br = new BufferedReader(isr);) {
			String positionName = null;
			String separator = null;
			StringBuilder script = new StringBuilder();
			String readed = null;
			while ((readed = br.readLine()) != null) {
				// positionName not defined => define it
				if (positionName == null) {
					positionName = readed;
				}
				// positionName has been defined ; if separator has not been defined, define it
				else if (separator == null) {
					separator = readed;
				}
				// positionName and separator have been defined, see if we reached end of script
				else if (readed.equals(separator)) {
					// We have a new position defined, store it if position is active
					Position position = new Position(positionName, date, script.toString());
					if (position.isWorking()) positions.put(positionName, position);
					// Clean state
					positionName = separator = null;
					script = new StringBuilder();
				}
				// WE ARE IN SCRIPT, STORE IT
				else {
					script.append(readed);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return positions;
	}

	/**
	 * Get the constraints for the Day date, with workers defined in workers. workers contains the conditions for each worker.
	 * @param date
	 * @param workers
	 * @return
	 */
	public static List<Constraint> getConstraints(
			LocalDate date,
			HashMap<LocalDate, HashMap<String, IntVar>> workers) {

		List<Constraint> constraints = new ArrayList<>();

		HashMap<String, IntVar> toDay = workers.get(date);
		if (toDay == null) {
			throw new IllegalArgumentException("date " + date + " does not exist in agenda");
		} else {

			// EQUALITY BETWEEN LUNEL_1 AND LUNEL_2 == MEDECIN DE LUNEL IDEM JOUR ET GARDE
			{
				IntVar lunel1 = toDay.get("Lunel_Jour");
				IntVar lunel2 = toDay.get("Lunel_Nuit");
				if (lunel1 != null && lunel2 != null) {
					constraints.add(IntConstraintFactory.arithm(lunel1, "=", lunel2));
				}
			}

			// EQUALITY BETWEEN GANGES_1 AND GANGES_2 == MEDECIN DE GANGES IDEM JOUR ET GARDE
			{
				IntVar ganges1 = toDay.get("Ganges_Jour");
				IntVar ganges2 = toDay.get("Ganges_Nuit");
				if (ganges1 != null && ganges2 != null) {
					constraints.add(IntConstraintFactory.arithm(ganges1, "=", ganges2));
				}
			}

			// EQUALITY BETWEEN AGDE_1 AND AGDE_2 == MEDECIN DE AGDE IDEM JOUR ET GARDE
			{
				IntVar agde1 = toDay.get("Agde_Jour");
				IntVar agde2 = toDay.get("Agde_Nuit");
				if (agde1 != null && agde2 != null) {
					constraints.add(IntConstraintFactory.arithm(agde1, "=", agde2));
				}
			}
			
			// EQUALITY BETWEEN DRIVE_DIMANCHE AND DRIVE_SAMEDI DAY BEFORE
			{
				HashMap<String, IntVar> yesterday = workers.get(date.minusDays(1));
				IntVar driveDimanche = toDay.get("Drive_Dimanche");
				IntVar driveSamediYesterday = yesterday == null ? null : yesterday.get("Drive_Samedi");
				if (driveDimanche != null && driveSamediYesterday != null) {
					constraints.add(IntConstraintFactory.arithm(driveDimanche, "=", driveSamediYesterday));
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
			// - Drive_Semaine
			// - Drive_Semaine day before
			// - Drive_Dimanche day before
			// - Drive_Samedi
			// - Drive_Dimanche
			{
				LinkedList<IntVar> concurrent = new LinkedList<>();
				HashMap<String, IntVar> yesterday = workers.get(date.minusDays(1));
				// LUNEL
				IntVar lunel1 = toDay.get("Lunel_Jour");
				if (lunel1 != null) concurrent.add(lunel1);
				IntVar lunel2yesterday = yesterday == null ? null : yesterday.get("Lunel_Nuit");
				if (lunel2yesterday != null) concurrent.add(lunel2yesterday);
				// GANGES
				IntVar ganges1 = toDay.get("Ganges_Jour");
				if (ganges1 != null) concurrent.add(ganges1);
				IntVar ganges2yesterday = yesterday == null ? null : yesterday.get("Ganges_Nuit");
				if (ganges2yesterday != null) concurrent.add(ganges2yesterday);
				// AGDE
				IntVar agde1 = toDay.get("Agde_Jour");
				if (agde1 != null) concurrent.add(agde1);
				IntVar agde2yesterday = yesterday == null ? null : yesterday.get("Agde_Nuit");
				if (agde2yesterday != null) concurrent.add(agde2yesterday);
				// REGUL
				IntVar regul1 = toDay.get("Regul_Jour");
				if (regul1 != null) concurrent.add(regul1);
				IntVar regul2 = toDay.get("Regul_Nuit");
				if (regul2 != null) concurrent.add(regul2);
				IntVar regul3yesterday = yesterday == null ? null : yesterday.get("Regul_MidiMinuit");
				if (regul3yesterday != null) concurrent.add(regul3yesterday);
				// SMUR1
				IntVar smur11 = toDay.get("Smur1_Jour");
				if (smur11 != null) concurrent.add(smur11);
				IntVar smur12yesterday = yesterday == null ? null : yesterday.get("Smur1_Nuit");
				if (smur12yesterday != null) concurrent.add(smur12yesterday);
				// SMUR2
				IntVar smur21 = toDay.get("Smur2_Jour");
				if (smur21 != null) concurrent.add(smur21);
				IntVar smur22yesterday = yesterday == null ? null : yesterday.get("Smur2_Nuit");
				if (smur22yesterday != null) concurrent.add(smur22yesterday);
				// UHCD
				IntVar uhcd1 = toDay.get("Uhcd_Jour");
				if (uhcd1 != null) concurrent.add(uhcd1);
				// SI
				IntVar si1 = toDay.get("Si_Jour");
				if (si1 != null) concurrent.add(si1);
				IntVar si2yesterday = yesterday == null ? null : yesterday.get("Si_Nuit");
				if (si2yesterday != null) concurrent.add(si2yesterday);
				// FMC
				IntVar fmc1 = toDay.get("Fmc_Jour");
				if (fmc1 != null) concurrent.add(fmc1);
				IntVar fmc2yesterday = yesterday == null ? null : yesterday.get("Fmc_2");
				if (fmc2yesterday != null) concurrent.add(fmc2yesterday);
				// FC
				IntVar fc1 = toDay.get("Fc_Jour");
				if (fc1 != null) concurrent.add(fc1);
				// UTEC
				IntVar utec1 = toDay.get("Utec_Jour");
				if (utec1 != null) concurrent.add(utec1);
				IntVar utec2yesterday = yesterday == null ? null : yesterday.get("Utec_Nuit");
				if (utec2yesterday != null) concurrent.add(utec2yesterday);
				// UHCD
				IntVar uhcd = toDay.get("Uhcd");
				if (uhcd != null) concurrent.add(uhcd);
				// DRIVE
				IntVar driveSemaine = toDay.get("Drive_Semaine");
				if (driveSemaine != null) concurrent.add(driveSemaine);
				IntVar driveSemaineYesterday = yesterday == null ? null : yesterday.get("Drive_Semaine");
				if (driveSemaineYesterday != null) concurrent.add(driveSemaineYesterday);
				IntVar driveDimancheYesterday = yesterday == null ? null : yesterday.get("Drive_Dimanche");
				if (driveDimancheYesterday != null) concurrent.add(driveDimancheYesterday);
				IntVar driveSamedi = toDay.get("Drive_Samedi");
				if (driveSamedi != null) concurrent.add(driveSamedi);
				IntVar driveDimanche = toDay.get("Drive_Dimanche");
				if (driveDimanche != null) concurrent.add(driveDimanche);
				
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
			// - REGUL_2 DAY BEFORE
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
				IntVar lunel2 = toDay.get("Lunel_Nuit");
				if (lunel2 != null) concurrent.add(lunel2);
				IntVar lunel2yesterday = yesterday == null ? null : yesterday.get("Lunel_Nuit");
				if (lunel2yesterday != null) concurrent.add(lunel2yesterday);
				// GANGES
				IntVar ganges2 = toDay.get("Ganges_Nuit");
				if (ganges2 != null) concurrent.add(ganges2);
				IntVar ganges2yesterday = yesterday == null ? null : yesterday.get("Ganges_Nuit");
				if (ganges2yesterday != null) concurrent.add(ganges2yesterday);
				// AGDE
				IntVar agde2 = toDay.get("Agde_Nuit");
				if (agde2 != null) concurrent.add(agde2);
				IntVar agde2yesterday = yesterday == null ? null : yesterday.get("Agde_Nuit");
				if (agde2yesterday != null) concurrent.add(agde2yesterday);
				// REGUL
				IntVar regul2 = toDay.get("Regul_Nuit");
				if (regul2 != null) concurrent.add(regul2);
				IntVar regul2yesterday = yesterday == null ? null : yesterday.get("Regul_Nuit");
				if (regul2yesterday != null) concurrent.add(regul2yesterday);
				IntVar regul3 = toDay.get("Regul_MidiMinuit");
				if (regul3 != null) concurrent.add(regul3);
				IntVar regul3yesterday = yesterday == null ? null : yesterday.get("Regul_MidiMinuit");
				if (regul3yesterday != null) concurrent.add(regul3yesterday);
				// SMUR1
				IntVar smur12 = toDay.get("Smur1_Nuit");
				if (smur12 != null) concurrent.add(smur12);
				IntVar smur12yesterday = yesterday == null ? null : yesterday.get("Smur1_Nuit");
				if (smur12yesterday != null) concurrent.add(smur12yesterday);
				// SMUR2
				IntVar smur22 = toDay.get("Smur2_Nuit");
				if (smur22 != null) concurrent.add(smur22);
				IntVar smur22yesterday = yesterday == null ? null : yesterday.get("Smur2_Nuit");
				if (smur22yesterday != null) concurrent.add(smur22yesterday);
				// SI
				IntVar si2 = toDay.get("Si_Nuit");
				if (si2 != null) concurrent.add(si2);
				IntVar si2yesterday = yesterday == null ? null : yesterday.get("Si_Nuit");
				if (si2yesterday != null) concurrent.add(si2yesterday);
				// FMC
				IntVar fmc2 = toDay.get("Fmc_Nuit");
				if (fmc2 != null) concurrent.add(fmc2);
				IntVar fmc2yesterday = yesterday == null ? null : yesterday.get("Fmc_Nuit");
				if (fmc2yesterday != null) concurrent.add(fmc2yesterday);
				// UTEC
				IntVar utec2 = toDay.get("Utec_Nuit");
				if (utec2 != null) concurrent.add(utec2);
				IntVar utec2yesterday = yesterday == null ? null : yesterday.get("Utec_Nuit");
				if (utec2yesterday != null) concurrent.add(utec2yesterday);
				// Drive
				IntVar driveSemaineYesterday = yesterday == null ? null : yesterday.get("Drive_Semaine");
				if (driveSemaineYesterday != null) concurrent.add(driveSemaineYesterday);
				IntVar driveDimancheYesterday = yesterday == null ? null : yesterday.get("Drive_Dimanche");
				if (driveDimancheYesterday != null) concurrent.add(driveDimancheYesterday);
				IntVar driveSamediYesterday = yesterday == null ? null : yesterday.get("Drive_Samedi");
				if (driveSamediYesterday != null) concurrent.add(driveSamediYesterday);
				
				if (concurrent.size() >= 2)
					constraints.add(IntConstraintFactory.alldifferent(concurrent.toArray(new IntVar[concurrent.size()])));
			}
			
			// MUTUALLY EXCLUSIVE :
			// - Fmc_Jour
			// - Fmc_Nuit
			// - Si_Jour
			// - Si_Nuit
			// - Utec Jour
			// - Utec Nuit
			// - Uhcd
			{
				LinkedList<IntVar> concurrent = new LinkedList<>();
				IntVar fmc1 = toDay.get("Fmc_Jour");
				if (fmc1 != null) concurrent.add(fmc1);
				IntVar fmc2 = toDay.get("Fmc_Nuit");
				if (fmc2 != null) concurrent.add(fmc2);
				IntVar si1 = toDay.get("Si_Jour");
				if (si1 != null) concurrent.add(si1);
				IntVar si2 = toDay.get("Si_Nuit");
				if (si2 != null) concurrent.add(si2);
				IntVar utecJour = toDay.get("Utec_Jour");
				if (utecJour != null) concurrent.add(utecJour);
				IntVar utecNuit = toDay.get("Utec_Nuit");
				if (utecNuit != null) concurrent.add(utecNuit);
				IntVar uhcd = toDay.get("Uhcd");
				if (uhcd != null) concurrent.add(uhcd);
				
				if (concurrent.size() >= 2)
					constraints.add(IntConstraintFactory.alldifferent(concurrent.toArray(new IntVar[concurrent.size()])));
			}

			// MUTUALLY EXCLUSIVE :
			// - Smur1_Jour
			// - Smur2_Jour
			// - Fmc_Nuit
			// - Si_Nuit
			// - Utec_Nuit
			// - Regul_Nuit
			// - Regul_MidiMinuit
			{
				LinkedList<IntVar> concurrent = new LinkedList<>();
				IntVar smur1Jour = toDay.get("Smur1_Jour");
				if (smur1Jour != null) concurrent.add(smur1Jour);
				IntVar smur2Jour = toDay.get("Smur2_Jour");
				if (smur2Jour != null) concurrent.add(smur2Jour);
				IntVar fmcNuit = toDay.get("Fmc_Nuit");
				if (fmcNuit != null) concurrent.add(fmcNuit);
				IntVar siNuit = toDay.get("Si_Nuit");
				if (siNuit != null) concurrent.add(siNuit);
				IntVar utecNuit = toDay.get("Utec_Nuit");
				if (utecNuit != null) concurrent.add(utecNuit);
				IntVar regulNuit = toDay.get("Regul_Nuit");
				if (regulNuit != null) concurrent.add(regulNuit);
				IntVar regulMidiMinuit = toDay.get("Regul_MidiMinuit");
				if (regulMidiMinuit != null) concurrent.add(regulMidiMinuit);
				
				if (concurrent.size() >= 2)
					constraints.add(IntConstraintFactory.alldifferent(concurrent.toArray(new IntVar[concurrent.size()])));
			}
			
			// MUTUALLY EXCLUSIVE :
			// - Smur1_Nuit
			// - Smur2_Nuit
			// - Fmc_Jour
			// - Si_Jour
			// - Utec_Jour
			// - Uhcd
			// - Fc
			// - Regul_Jour
			// - Regul_MidiMinuit
			
			{
				LinkedList<IntVar> concurrent = new LinkedList<>();
				IntVar smur1Nuit = toDay.get("Smur1_Nuit");
				if (smur1Nuit != null) concurrent.add(smur1Nuit);
				IntVar smur2Nuit = toDay.get("Smur2_Nuit");
				if (smur2Nuit != null) concurrent.add(smur2Nuit);
				IntVar fmcJour = toDay.get("Fmc_Jour");
				if (fmcJour != null) concurrent.add(fmcJour);
				IntVar siJour = toDay.get("Si_Jour");
				if (siJour != null) concurrent.add(siJour);
				IntVar utecJour = toDay.get("Utec_Jour");
				if (utecJour != null) concurrent.add(utecJour);
				IntVar uhcd = toDay.get("Uhcd");
				if (uhcd != null) concurrent.add(uhcd);
				IntVar fc = toDay.get("Fc");
				if (fc != null) concurrent.add(fc);
				IntVar regulJour = toDay.get("Regul_Jour");
				if (regulJour != null) concurrent.add(regulJour);
				IntVar regulMidiMinuit = toDay.get("Regul_MidiMinuit");
				if (regulMidiMinuit != null) concurrent.add(regulMidiMinuit);
				
				if (concurrent.size() >= 2)
					constraints.add(IntConstraintFactory.alldifferent(concurrent.toArray(new IntVar[concurrent.size()])));
			}
			
			return constraints;
		}
	}

}
