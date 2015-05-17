package com.github.pjpo.planning.lignes;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.github.pjpo.planning.utils.IntervalDateTime;

public class Position {
	
	private final LocalDate date;
	
	private final LocalDateTime startWork;
	
	private final LocalDateTime endWork;

	private final IntervalDateTime plage;

	private final Boolean isWorking;
	
	private final Integer workLoad;
	
	private final String name;
	
	public Position(final String name, final LocalDate date, String script) {
		this.date = date;
		this.name = name;

		ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

		Boolean tempIsWorking = false;
		Integer tempWorkLoad = 0;
		LocalDateTime tempStartWork = null;
		LocalDateTime tempEndWork = null;
		IntervalDateTime tempPlage = null;
		try {
			engine.eval(script);
			Invocable inv = (Invocable) engine;
			Bindings result = (Bindings) inv.invokeFunction("fun", date);
			tempIsWorking = (Boolean) result.get("working");
			tempWorkLoad = (Integer) result.get("load");
			String[] tempStartWorkString = ((String) result.get("start")).split(":");
			tempStartWork = date.atTime(Integer.parseInt(tempStartWorkString[0]), Integer.parseInt(tempStartWorkString[1]));
			tempEndWork = tempStartWork.plusMinutes((Integer) result.get("duration"));
			tempPlage = new IntervalDateTime(tempStartWork, tempEndWork);
		} catch (ScriptException | NoSuchMethodException e) {
			e.printStackTrace();
		}
		isWorking = tempIsWorking;
		workLoad = tempWorkLoad;
		startWork = tempStartWork;
		endWork = tempEndWork;
		plage = tempPlage;
	};
	
	
	public final Boolean isWorking() {
		return isWorking;
	}

	public String getName() {
		return name;
	}

	public Integer getWorkLoad() {
		return workLoad;
	}


	public LocalDate getDate() {
		return date;
	}


	public LocalDateTime getStartWork() {
		return startWork;
	}


	public LocalDateTime getEndWork() {
		return endWork;
	}


	public IntervalDateTime getPlage() {
		return plage;
	}

	
}
