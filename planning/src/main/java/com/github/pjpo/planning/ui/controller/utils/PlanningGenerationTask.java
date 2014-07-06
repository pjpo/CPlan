package com.github.pjpo.planning.ui.controller.utils;

import java.util.LinkedList;

import com.github.pjpo.planning.Planning;
import com.github.pjpo.planning.PlanningSolver;
import com.github.pjpo.planning.Solution;
import com.github.pjpo.planning.SolutionException;
import com.github.pjpo.planning.ui.controller.GenerationOverviewController;
import javafx.application.Platform;

import javafx.concurrent.Task;

public class PlanningGenerationTask extends Task<LinkedList<Solution>> {

	private PlanningSolver solver = null;

	private boolean isAlive = true;

	private final Planning planning;
	
	private final Object sync = new Object();

	private final GenerationOverviewController controller;
		
	public PlanningGenerationTask(final Planning planning, final GenerationOverviewController controller) {
		this.planning = planning;
		this.controller = controller;
	}
	
	@Override
	protected LinkedList<Solution> call() throws Exception {
		// INIT VARS
		final LinkedList<Solution> solutions = new LinkedList<>();
		Integer retrys = 0;

		for (;;) {
			// IN ORDER TO PREVENT A RACE CONDITION, GENERATE THE SOLVER BEFORE CHECKING IF PROCESS IS CANCELLED
			retrys++;
			synchronized (sync) {
				solver = planning.generateSolver(solutions);
			}

			// FIRST CHECK GENERATION STATUS
			synchronized (sync) {
				if (isCancelled() || !isAlive)
					break;
			}

			final Solution solution = solver.findSolution();
			final Integer finalRetrys = new Integer(retrys);

			if (solution == null && !solver.hasSolution() && !solver.isUndefined()) {
				throw new SolutionException("No solution");
			} else if (solution != null) {
				// UPDATES VALUES IN LABELS
				Platform.runLater( () ->
				controller.showFeedBack(new Integer(finalRetrys), solution.getMaxWorkLoad(), solution.getMinWorkLoad()));
				// ADDS THIS SOLUTION AS LAST SOLUTION
				solutions.add(solution);
			} else {
				Platform.runLater( () ->
				controller.showFeedBack(new Integer(finalRetrys),
						solutions.size() == 0 ? null : solutions.getLast().getMaxWorkLoad(),
								solutions.size() == 0 ? null : solutions.getLast().getMinWorkLoad()));
			}
		}
		// HERE, RETURN SOLUTIONS
		return solutions;
	}

	public void stopProcessing(String reason) {
		synchronized (sync) {
			if (solver != null) {
				solver.stopProcessing(reason);
			}
			isAlive = false;
		}
	}
	
}

