package com.github.pjpo.planning.constraintsrules;

/**
 * Used in grammar for constraints : stores the name of a position and the number of days before
 * or after the one considered. Used in PositionConstraintBase to list the elements of a rule
 * @author jp@dm.lan
 *
 */
public class PositionConstraintRuleElement {
	
	private String positionName;
	
	private Integer deltaDays;
	
	public String getPositionName() {
		return positionName;
	}
	
	public void setPositionName(String positionName) {
		this.positionName = positionName;
	}
	
	public Integer getDeltaDays() {
		return deltaDays;
	}
	
	public void setDeltaDays(Integer deltaDays) {
		this.deltaDays = deltaDays;
	}
}
