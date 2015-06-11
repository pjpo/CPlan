package com.github.pjpo.planning.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.github.pjpo.planning.utils.IntervalDateTime;

/**
 * definitions of a position. From these definitions, creates a {@link Position} from a date.
 * @author jp@dm.lan
 *
 */
public class PositionDefinition {

	/** name of this position */
	private String name;
	
	/** script for this position */
	private String script;

	/** Js script engine used. Created once for each position definition */
	private final ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
	
	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getScript() {
		return script;
	}

	public void setScript(final String script) {
		this.script = script;
	}

	/**
	 * Returns the position associated with this definition for defined date
	 * @param dateWorking
	 * @return position, or null if no such position exists for this day
	 * @throws ScriptException
	 */
	public Position getPosition(final LocalDate dateWorking) throws ScriptException {

		try {
			final Position position = new Position();
			
			// Invokes the function with date as a parameter which defines the Position fields
			engine.eval(script);
			final Invocable inv = (Invocable) engine;
			final Bindings result = (Bindings) inv.invokeFunction("fun", dateWorking);
			
			position.setName(name);
			position.setIsActive((Boolean) result.get("working"));
			position.setWorkLoad((Integer) result.get("load"));
			final String[] startWorkString = ((String) result.get("start")).split(":");
			final LocalDateTime startWorkDate = dateWorking.atTime(Integer.parseInt(startWorkString[0]), Integer.parseInt(startWorkString[1]));
			position.setBounds(new IntervalDateTime(startWorkDate, startWorkDate.plusMinutes((Integer) result.get("duration"))));
			
			return position; 
		} catch (ScriptException e) {
			throw e;
		} catch (NoSuchMethodException e) {
			throw new ScriptException("Function 'fun' not found");
		}
	}
		
}
