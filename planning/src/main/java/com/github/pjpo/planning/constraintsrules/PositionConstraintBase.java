package com.github.pjpo.planning.constraintsrules;

import java.util.List;

public abstract class PositionConstraintBase {

	private List<PositionConstraintRuleElement> ruleElements;

	public List<PositionConstraintRuleElement> getRuleElements() {
		return ruleElements;
	}

	public void setRuleElements(List<PositionConstraintRuleElement> ruleElements) {
		this.ruleElements = ruleElements;
	}
	
}
