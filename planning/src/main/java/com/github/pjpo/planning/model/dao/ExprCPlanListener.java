package com.github.pjpo.planning.model.dao;

import java.util.LinkedList;
import java.util.List;

import com.github.pjpo.planning.model.dao.ExprBaseListener;
import com.github.pjpo.planning.model.dao.ExprParser;
import com.github.pjpo.planning.model.PositionConstraintBase;
import com.github.pjpo.planning.model.PositionDifferentConstraint;
import com.github.pjpo.planning.model.PositionEqualConstraint;

/**
 * Class between Parser and Data access object to read a constraints code
 * and transform it to constraints objects
 * @author jp@dm.lan
 *
 */
public class ExprCPlanListener extends ExprBaseListener {

	final private List<PositionConstraintBase> constraints = new LinkedList<>();
		
	/**
	 * Listens to parser exiting an equality code. At this moment,
	 * add this equal constraint to the aggregator
	 */
	@Override public void exitEquality(ExprParser.EqualityContext ctx) {
		final PositionEqualConstraint constraint = new PositionEqualConstraint();
		constraint.setRuleElements(ctx.rules.rules);
		constraints.add(constraint);
	}

	/**
	 * Listens to parser exiting a different code. At this moment,
	 * add this different constraint to the aggregator
	 */
	@Override public void exitDifference(ExprParser.DifferenceContext ctx) {
		final PositionDifferentConstraint constraint = new PositionDifferentConstraint();
		constraint.setRuleElements(ctx.rules.rules);
		constraints.add(constraint);
	}

	/**
	 * Retrieves the list of constraints after parsing
	 * @return
	 */
	public List<PositionConstraintBase> getConstraints() {
		return constraints;
	}
	
}
