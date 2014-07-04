package com.github.aiderpmsi.planning;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;

import solver.search.strategy.selectors.IntValueSelector;
import solver.variables.IntVar;

import com.github.aiderpmsi.planning.physician.Physician;

@SuppressWarnings("serial")
public class MyRandomStrategy implements IntValueSelector {

	final Random rand;
	
	final ArrayList<Physician> physicians;

	public MyRandomStrategy(ArrayList<Physician> physicians) {
		this.rand = new Random(new Date().getTime());
		this.physicians = physicians;
	}

	@Override
	public int selectValue(IntVar var) {
		LinkedList<Integer> possiblePhysicians = new LinkedList<>();  
		for (int i = var.getLB(); i <= var.getUB(); i = var.nextValue(i)) {
			// CHECK IF THIS PHYSICIAN WORKS THIS DAY
			// TODO
			// LISTS ALL THE POSSIBLE PHYSICIANS
			possiblePhysicians.add(i);
		}
		// SUMS THE WORKING TIME FOR EACH PHYSICIAN
		int workingTime = 0;
		for (Integer physicianNb : possiblePhysicians) {
			workingTime += physicians.get(physicianNb).getTimePart();
		}
		// FINDS AN INT BETWEEN 0 AND WORKINGTIME - 1 (DEFINES THE WORKER WHO WILL HAVE TO WORK)
		int workingWorker = rand.nextInt(workingTime - 1);
		int workingElapsedTime = 0;
		
		for (Integer physicianNb : possiblePhysicians) {
			workingElapsedTime += physicians.get(physicianNb).getTimePart();
			if (workingElapsedTime > workingWorker)
				return physicianNb;
		}

		// IF SOMETHING WENT WRONG, RETURN A BOUND 
		return var.getLB();
	}
}
