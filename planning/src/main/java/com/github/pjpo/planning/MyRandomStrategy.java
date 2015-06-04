package com.github.pjpo.planning;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;
import java.util.stream.Collectors;

import org.chocosolver.solver.search.strategy.selectors.IntValueSelector;
import org.chocosolver.solver.variables.IntVar;

import com.github.pjpo.planning.model.Physician;

/**
 * Choco Strategy to select a value (= worker) for an IntVar (= position)
 * Randomly selects a person with the chance to select him
 * depending on its work part
 * @author jpc
 *
 */
@SuppressWarnings("serial")
public class MyRandomStrategy implements IntValueSelector {

	/** Random used to select a physician */ 
	private final Random rand = new Random(new Date().getTime());
	
	/** Physicians */
	private final ArrayList<Physician> physicians;

	/**
	 * Simple Constructor : list of all people
	 * @param physicians
	 */
	public MyRandomStrategy(final ArrayList<Physician> physicians) {
		this.physicians = physicians;
	}

	@Override
	public int selectValue(final IntVar var) {
		// 'var' represents the position we must find a worker for (day / position)
		// We select randomly a people who can work at this position (but with more chances to pick up someone depending on its work part)

		// Let's make the list of people who can work at this position
		final LinkedList<Integer> possiblePersons = new LinkedList<>();
		for (int i = var.getLB(); i <= var.getUB(); i = var.nextValue(i)) {
			possiblePersons.add(i);
		}
		
		// In order to randomly select the working person but depending on work part, we sum up
		// the whole work part and then choose randomly an number between 1 and this whole work part.
		// Afterwards, find which physician was pointed by this random number 
		
		// Sum of work part
		final int totalWorkPart = possiblePersons.stream().collect(Collectors.summingInt((personNb) -> physicians.get(personNb).getTimePart()));
				
		// Randomizes a value between 1 and total work part
		final int workingElapsed = rand.nextInt(totalWorkPart - 1) + 1;

		// Finds to which person this random value corresponds
		int workPartElapsed = 1;
		
		for (final Integer personNb : possiblePersons) {
			workPartElapsed += physicians.get(personNb).getTimePart();
			if (workPartElapsed > workingElapsed)
				return personNb;
		}

		// If something went wrong, throw error
		throw new IllegalArgumentException("No person found to fill this position");
	}
}
