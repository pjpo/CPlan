package com.github.pjpo.planning.ui.controller.utils;

import javafx.application.Platform;
import javafx.concurrent.Task;

import com.github.pjpo.planning.problem.PlanningForInterval;
import com.github.pjpo.planning.problem.Solution;
import com.github.pjpo.planning.problem.SolutionException;
import com.github.pjpo.planning.ui.controller.GenerationOverviewController;

public class PlanningGenerationTask extends Task<Solution> {

	private boolean isAlive = true;

	private final PlanningForInterval planningImplementation;
	
	private final Object sync = new Object();

	private final GenerationOverviewController controller;
		
	public PlanningGenerationTask(final PlanningForInterval planningImplementation, final GenerationOverviewController controller) {
		this.planningImplementation = planningImplementation;
		this.controller = controller;
	}
	
	@Override
	protected Solution call() throws Exception {
		// INIT VARS
		Integer retrys = 0;

		for (;;) {
			retrys++;

			// FIRST CHECK GENERATION STATUS
			synchronized (sync) {
				if (isCancelled() || !isAlive)
					break;
			}
			
			try {
				if (planningImplementation.findNewSolution() == false && planningImplementation.getSolution() == null) {
					throw new SolutionException("No solution found");
				} else {
					final Integer finalRetrys = Integer.valueOf(retrys);
					// UPDATES VALUES IN LABELS
					Platform.runLater(() ->
					controller.showFeedBack(finalRetrys, planningImplementation.getWorkLoadSDs().getFirst()));
				}
			} catch (Throwable th) {
				th.printStackTrace();
			}
		}
		// HERE, RETURN SOLUTIONS
		return planningImplementation.getSolution();
	}

	public void stopProcessing(String reason) {
		synchronized (sync) {
			isAlive = false;
		}
	}
	
}

