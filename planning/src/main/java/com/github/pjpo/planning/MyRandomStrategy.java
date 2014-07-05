package com.github.pjpo.planning;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;

import solver.search.strategy.selectors.IntValueSelector;
import solver.variables.IntVar;

import com.github.pjpo.planning.physician.Physician;

/**
 * Randomly selects a physician with the chance to select him
 * depending on its working time
 * @author jpc
 *
 */
@SuppressWarnings("serial")
public class MyRandomStrategy implements IntValueSelector {

	/** Random used to select a physician */ 
	private final Random rand = new Random(new Date().getTime());
	
	/** Physicians */
	private final ArrayList<Physician> physicians;

	public MyRandomStrategy(ArrayList<Physician> physicians) {
		this.physicians = physicians;
	}

	@Override
	public int selectValue(IntVar var) {
		// CREATE THE LIST OF WORKING PHYSICIANS IN THIS VAR
		final LinkedList<Integer> workingPhysicians = new LinkedList<>();
		for (int i = var.getLB(); i <= var.getUB(); i = var.nextValue(i)) {
			workingPhysicians.add(i);
		}

		// SUMS THE TOTAL WORKING TIME FOR EACH PHYSICIAN
		int totalWorkingTime = 0;
		for (Integer physicianNb : workingPhysicians) {
			totalWorkingTime += physicians.get(physicianNb).getTimePart();
		}
		
		// RANDOMIZES A VALUE BETWEEN 0 AND TOTALWORKINGTIME - 1
		// USES IT TO RANDOMIZE DEPENDING ON THE WORKING TIME
		int workingElapsed = rand.nextInt(totalWorkingTime - 1);
		int workingElapsedTime = 0;
		
		for (Integer physicianNb : workingPhysicians) {
			workingElapsedTime += physicians.get(physicianNb).getTimePart();
			if (workingElapsedTime > workingElapsed)
				return physicianNb;
		}

		// IF SOMETHING WENT WRONG, RETURN A BOUND 
		return var.getLB();
	}
}
