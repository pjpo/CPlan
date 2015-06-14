package com.github.pjpo.planning.problem;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Random;
import java.util.stream.Collectors;

import org.chocosolver.solver.search.strategy.selectors.IntValueSelector;
import org.chocosolver.solver.variables.IntVar;

import com.github.pjpo.planning.model.Position;
import com.github.pjpo.planning.model.Worker;
import com.github.pjpo.planning.utils.IntervalDateTime;

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

	/** Private Hashtable for storing positions depending on intvar (index) */
	private final HashMap<IntVar, Position> intVarPositions;
	
	/** Number of retrys for each position */
	private final HashMap<IntVar, Integer> retrys = new HashMap<>();
	
	/** Number of retrys before we suggest that no solution exists */
	private final static int MAX_RETRYS = 800;
	
	/** Random */
	private final Random random = new Random(new Date().getTime());
	
	/**
	 * Initiates the strategy with couples of physician and corresponding internal int value.
	 * @param physicians
	 */
	public CPlanRandomStrategy(
			final HashMap<Integer, Worker> physicians,
			final HashMap<IntVar, Position> intVarPositions) {
		this.physicians = physicians;
		this.intVarPositions = intVarPositions;
	}

	@Override
	public int selectValue(final IntVar var) {
		// 'var' represents the position we must find a worker for (day / position)

		// Let's make the list of people who can work at this position
		if (var.getLB() == var.getUB()) {
			// Only one person can work at this position, return him
			return var.getLB();
		}
		// No employe is prepositioned by a previous solution.
		// if we retried too much times, put a random pseudo-employee (nobody
		// has been founs able to work at this position), else try with an existing employee
		// depending on its working time
		else {
			// Increment the number of retries for this var
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
			// else find a possible worker
			else {
				// Position for this intvar
				final Position position = intVarPositions.get(var);
				
				// Does somebody has to work this day ?
				for (final Entry<Integer, Worker> physician : physicians.entrySet()) {
					if (physician.getValue().getWorkedPositions().containsEntry(position.getBounds().getStart().toLocalDate(), position.getName())) {
						return physician.getKey();
					}
				}
				
				// Nobody has to specifically work this day, list the people able to work at this position
				final LinkedList<Integer> employeesAbleToWorkIndices = new LinkedList<>();
				eachPhysician : for (final Entry<Integer, Worker> physician : physicians.entrySet()) {
					
					// CHECK IF THIS PHYSICIAN HAS THE RIGHT TO WORK AT THIS POSITION
					if (physician.getValue().getRefusedPositions().contains(position.getName()))
						continue eachPhysician;					
					
					// CHECK IF PHYSICIAN IS IN PAID VACATIONS
					for (final IntervalDateTime vacation : physician.getValue().getPaidVacations()) {
						if (vacation.isOverlapping(position.getBounds()))
							continue eachPhysician;
					}
					
					// CHECK IF PHYSICIAN IS IN UNPAID VACATIONS
					for (final IntervalDateTime vacation : physician.getValue().getUnpaidVacations()) {
						if (vacation.isOverlapping(position.getBounds()))
							continue eachPhysician;
					}
					
					// THIS PHYSICIAN CAN WORK AT THIS POSITION AT THIS DAY
					employeesAbleToWorkIndices.add(physician.getKey());
				}

				// Nobody able to work, return a pseudo worker
				if (employeesAbleToWorkIndices.size() == 0) {
					return random.nextInt(Integer.MAX_VALUE - physicians.size()) + physicians.size();
				} else {
					// In order to randomly select the working person but depending on work part, we sum up
					// the whole work part and then choose randomly an number between 1 and this whole work part.
					// Afterwards, find which physician was pointed by this random number 
					
					// Sum of work part
					final int totalWorkPart = employeesAbleToWorkIndices.stream().collect(Collectors.summingInt((personNb) -> physicians.get(personNb).getTimePart()));

					// Randomizes a value between 1 and total work part
					final int workingElapsed = rand.nextInt(totalWorkPart - 1) + 1;

					// Finds to which person this random value corresponds
					int workPartElapsed = 1;
					
					for (final Integer personNb : employeesAbleToWorkIndices) {
						workPartElapsed += physicians.get(personNb).getTimePart();
						if (workPartElapsed > workingElapsed)
							return personNb;
					}

					// If something went wrong, return a pseudo worker
					return random.nextInt(Integer.MAX_VALUE - physicians.size()) + physicians.size();
				}
					
			}
		}
		
	}		
}
