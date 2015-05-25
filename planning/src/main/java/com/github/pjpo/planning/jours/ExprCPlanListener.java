package com.github.pjpo.planning.jours;

public class ExprCPlanListener extends ExprBaseListener {

	/**
	 * {@inheritDoc}
	 *
	 * When exiting equality, we have to add these constraints to solver
	 */
	@Override public void exitEquality(ExprParser.EqualityContext ctx) {
		System.out.println(" = ");
		for (ChocoRule rule : ctx.rules.rules) {
			System.out.println(rule.getName() + " " + rule.getValue());
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * When exiting differences, we have to add these constraints to solver
	 */
	@Override public void exitDifference(ExprParser.DifferenceContext ctx) { }
}
