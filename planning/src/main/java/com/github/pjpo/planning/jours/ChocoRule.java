package com.github.pjpo.planning.jours;

/**
 * Used in grammar for constraints : stores the name of a position and the number of days before
 * or after the one considered
 * @author jp@dm.lan
 *
 */
public class ChocoRule {
	
	private String name;
	
	private Integer value;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Integer getValue() {
		return value;
	}
	
	public void setValue(Integer value) {
		this.value = value;
	}
}
