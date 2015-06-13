package com.github.pjpo.planning.problem;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.stream.Collectors;

import org.chocosolver.solver.search.strategy.selectors.IntValueSelector;
import org.chocosolver.solver.variables.IntVar;

import com.github.pjpo.planning.model.Worker;

/**
 * Choco Strategy to select a value (= worker) for an IntVar (= position)
 * Randomly selects a person with the chance to select him
 * depending on its work part
 * @author jpc
 *
 */
@SuppressWarnings("serial")
public class CPlanRandomStrategy implements IntValueSelector {

	/** Random used to select a physician */ 
	private final Random rand = new Random(new Date().getTime());
	
	/** Physicians */
	private final HashMap<Integer, Worker> physicians;

	/** Number of retrys for each position */
	private final HashMap<IntVar, Integer> retrys = new HashMap<>();
	
	/** Number of retrys before we suggest that no solution exists */
	private final static int MAX_RETRYS = 10000;
	
	/** Random */
	private final Random random = new Random(new Date().getTime());
	
	/**
	 * Initiates the strategy with couples of physician and corresponding internal int value.
	 * @param physicians
	 */
	public CPlanRandomStrategy(final HashMap<Integer, Worker> physicians) {
		this.physicians = physicians;
	}

	@Override
	public int selectValue(final IntVar var) {
		// 'var' represents the position we must find a worker for (day / position)
		// We select randomly a people who can work at this position (but with more chances to pick up someone depending on its work part)

		// First increase retrys
		Integer retry = retrys.get(var);
		if (retry == null) {
			retry = 0;
		} else {
			retry += 1;
		}
		retrys.put(var, retry);
		
		// If number of retrys is above MAX_RETRYS, return a random value above max worker indice (suggesting nobody can work at this position)
		if (retry >= MAX_RETRYS) {
			return random.nextInt(Integer.MAX_VALUE - physicians.size()) + physicians.size();
		}
		
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
