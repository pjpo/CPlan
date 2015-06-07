package com.github.pjpo.planning.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.github.pjpo.planning.utils.IntervalDateTime;

public class PositionCode {

	private String name;
	
	private String code;

	private final ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Position getPosition(final LocalDate dateWorking) throws ScriptException {

		Boolean tempIsWorking = false;
		Integer tempWorkLoad = 0;
		LocalDateTime tempStartWork = null;
		LocalDateTime tempEndWork = null;
		IntervalDateTime tempPlage = null;
		
		try {
			engine.eval(code);
			final Invocable inv = (Invocable) engine;
			final Bindings result = (Bindings) inv.invokeFunction("fun", dateWorking);
			
			tempIsWorking = (Boolean) result.get("working");
			tempWorkLoad = (Integer) result.get("load");
			final String[] tempStartWorkString = ((String) result.get("start")).split(":");
			tempStartWork = dateWorking.atTime(Integer.parseInt(tempStartWorkString[0]), Integer.parseInt(tempStartWorkString[1]));
			tempEndWork = tempStartWork.plusMinutes((Integer) result.get("duration"));
			tempPlage = new IntervalDateTime(tempStartWork, tempEndWork);
		} catch (ScriptException e) {
			throw e;
		} catch (NoSuchMethodException e) {
			throw new ScriptException("Function 'fun' not found");
		}

		return new Position(dateWorking, tempStartWork, tempEndWork, tempPlage, tempIsWorking, tempWorkLoad, getName());
	}
		
}
