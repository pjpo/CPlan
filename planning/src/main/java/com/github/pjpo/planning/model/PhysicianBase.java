package com.github.pjpo.planning.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

import com.github.pjpo.planning.utils.IntervalDateTime;

public class PhysicianBase {

	protected String name = null;
	
	protected Integer timePart = null;
		
	protected ArrayList<IntervalDateTime> paidVacation = new ArrayList<>();
	
	protected ArrayList<IntervalDateTime> unpaidVacation = new ArrayList<>();

	protected HashMap<LocalDate, ArrayList<String>> workedVacs = new HashMap<>();
	
	protected ArrayList<String> refusedPostes = new ArrayList<>();

}
