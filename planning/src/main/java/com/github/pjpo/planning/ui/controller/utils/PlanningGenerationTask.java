package com.github.pjpo.planning.ui.controller.utils;

import java.util.LinkedList;

import com.github.pjpo.planning.PlanningImplementation;
import com.github.pjpo.planning.PlanningSolver;
import com.github.pjpo.planning.Solution;
import com.github.pjpo.planning.SolutionException;
import com.github.pjpo.planning.ui.controller.GenerationOverviewController;

import javafx.application.Platform;
import javafx.concurrent.Task;

public class PlanningGenerationTask extends Task<LinkedList<Solution>> {

	private PlanningSolver solver = null;

	private boolean isAlive = true;

	private final PlanningImplementation planningImplementation;
	
	private final Object sync = new Object();

	private final GenerationOverviewController controller;
		
	public PlanningGenerationTask(final PlanningImplementation planningImplementation, final GenerationOverviewController controller) {
		this.planningImplementation = planningImplementation;
		this.controller = controller;
	}
	
	@Override
	protected LinkedList<Solution> call() throws Exception {
		// INIT VARS
		Integer retrys = 0;

		for (;;) {
			retrys++;

			// FIRST CHECK GENERATION STATUS
			synchronized (sync) {
				if (isCancelled() || !isAlive)
					break;
			}
			
			final Solution solution = planningImplementation.findNewSolution();
			final Integer finalRetrys = new Integer(retrys);

			if (planningImplementation.getPreviousAcceptedSolutions().size() == 0 && solution == null && !solver.hasSolution() && !solver.isUndefined()) {
				throw new SolutionException("No solution");
			} else if (solution != null) {
				// UPDATES VALUES IN LABELS
				Platform.runLater( () ->
				controller.showFeedBack(new Integer(finalRetrys), solution.getWorkLoadSD()));
			} else {
				Platform.runLater( () ->
				controller.showFeedBack(new Integer(finalRetrys),
						planningImplementation.getPreviousAcceptedSolutions().size() == 0 ? null : planningImplementation.getPreviousAcceptedSolutions().getLast().getWorkLoadSD()));
			}
		}
		// HERE, RETURN SOLUTIONS
		return planningImplementation.getPreviousAcceptedSolutions();
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

