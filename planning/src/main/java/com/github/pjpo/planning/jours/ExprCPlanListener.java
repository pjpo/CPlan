package com.github.pjpo.planning.jours;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.variables.IntVar;

public class ExprCPlanListener extends ExprBaseListener {

	final private List<Constraint> constraints = new LinkedList<Constraint>();
	
	final private HashMap<LocalDate, HashMap<String, IntVar>> positionsIndices;
	
	final private LocalDate currentDate;
	
	public ExprCPlanListener(
			HashMap<LocalDate, HashMap<String, IntVar>> positionsIndices,
			LocalDate currentDate) {
		this.positionsIndices = positionsIndices;
		this.currentDate = currentDate;
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * When exiting equality, we have to add these constraints to solver
	 */
	@Override public void exitEquality(ExprParser.EqualityContext ctx) {
		newConstraints("=", ctx.rules.rules);
	}

	/**
	 * {@inheritDoc}
	 *
	 * When exiting differences, we have to add these constraints to solver
	 */
	@Override public void exitDifference(ExprParser.DifferenceContext ctx) {
		newConstraints("!=", ctx.rules.rules);
	}

	/**
	 * Stores the constraints for a list of rules with an identifier (equality or diference)
	 * @param equality
	 * @param rules
	 */
	private void newConstraints(String equality, List<ChocoRule> rules) {
		// If only one rule, do nothing (always equality with itself)
		if (rules.size() > 1) {
			// First equality member (null if not determined)
			IntVar previousIndice = null;
			rule : for (ChocoRule rule : rules) {
				// Get date reference or this rule
				HashMap<String, IntVar> subPositionsIndices =
						positionsIndices.get(currentDate.plusDays(rule.getValue()));
				// If nothing defined, go to next element
				if (subPositionsIndices == null)
					continue rule;
				// Get reference of this position
				IntVar indice = subPositionsIndices.get(rule.getName());
				// If no reference is defined, go to next element
				if (indice == null)
					continue rule;
				// If a previous element was defined, add equality
				if (previousIndice != null) {
					constraints.add(IntConstraintFactory.arithm(previousIndice, equality, indice));
				}
				// This indice is the next reference indice
				previousIndice = indice;
			}
		}			
	}

	public List<Constraint> getConstraints() {
		return constraints;
	}
}
