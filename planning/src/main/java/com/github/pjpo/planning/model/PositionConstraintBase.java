package com.github.pjpo.planning.model;

import java.util.List;

/**
 * Base class for constraint : list of positions who are in a rule
 * @author jp@dm.lan
 *
 */
public abstract class PositionConstraintBase {

	private List<PositionConstraintRuleElement> ruleElements;

	public List<PositionConstraintRuleElement> getRuleElements() {
		return ruleElements;
	}

	public void setRuleElements(final List<PositionConstraintRuleElement> ruleElements) {
		this.ruleElements = ruleElements;
	}
	
	@Override
	public String toString() {
		return ruleElements.toString();
	}
}
