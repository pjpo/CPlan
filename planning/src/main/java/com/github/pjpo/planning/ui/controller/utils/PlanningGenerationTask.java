package com.github.pjpo.planning.ui.controller.utils;

import java.util.LinkedList;

import com.github.pjpo.planning.SolutionException;
import com.github.pjpo.planning.model.Solution;
import com.github.pjpo.planning.problem.PlanningForInterval;
import com.github.pjpo.planning.ui.controller.GenerationOverviewController;

import javafx.application.Platform;
import javafx.concurrent.Task;

public class PlanningGenerationTask extends Task<LinkedList<Solution>> {

	private boolean isAlive = true;

	private final PlanningForInterval planningImplementation;
	
	private final Object sync = new Object();

	private final GenerationOverviewController controller;
		
	public PlanningGenerationTask(final PlanningForInterval planningImplementation, final GenerationOverviewController controller) {
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
			
			if (planningImplementation.findNewSolution() == false && planningImplementation.getSolutions().size() == 0) {
				throw new SolutionException("No solution found");
			} else {
				final Integer finalRetrys = Integer.valueOf(retrys);
				// UPDATES VALUES IN LABELS
				Platform.runLater(() ->
				controller.showFeedBack(finalRetrys, planningImplementation.getSolutions().getFirst().getWorkLoadSD()));
			}
		}
		// HERE, RETURN SOLUTIONS
		return planningImplementation.getSolutions();
	}

	public void stopProcessing(String reason) {
		synchronized (sync) {
			isAlive = false;
		}
	}
	
}

