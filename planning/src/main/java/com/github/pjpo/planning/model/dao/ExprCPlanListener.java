package com.github.pjpo.planning.model.dao;

import java.util.LinkedList;
import java.util.List;

import com.github.pjpo.planning.model.dao.ExprBaseListener;
import com.github.pjpo.planning.model.dao.ExprParser;
import com.github.pjpo.planning.model.PositionConstraintBase;
import com.github.pjpo.planning.model.PositionDifferentConstraint;
import com.github.pjpo.planning.model.PositionEqualConstraint;

public class ExprCPlanListener extends ExprBaseListener {

	final private List<PositionConstraintBase> constraints = new LinkedList<>();
		
	/**
	 * {@inheritDoc}
	 *
	 * When exiting equality, we have to add these constraints to solver
	 */
	@Override public void exitEquality(ExprParser.EqualityContext ctx) {
		final PositionEqualConstraint constraint = new PositionEqualConstraint();
		constraint.setRuleElements(ctx.rules.rules);
		constraints.add(constraint);
	}

	/**
	 * {@inheritDoc}
	 *
	 * When exiting differences, we have to add these constraints to solver
	 */
	@Override public void exitDifference(ExprParser.DifferenceContext ctx) {
		final PositionDifferentConstraint constraint = new PositionDifferentConstraint();
		constraint.setRuleElements(ctx.rules.rules);
		constraints.add(constraint);
	}

	public List<PositionConstraintBase> getConstraints() {
		return constraints;
	}
	
}
