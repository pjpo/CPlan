package com.github.pjpo.planning.physician;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

import com.github.pjpo.planning.utils.Interval;

public class PhysicianBase {

	protected String name = null;
	
	protected Integer timePart = null;

	protected LocalDate workStart = null;
	
	protected LocalDate workEnd = null;
		
	protected ArrayList<Interval> paidVacation = new ArrayList<>();
	
	protected ArrayList<Interval> unpaidVacation = new ArrayList<>();

	protected HashMap<LocalDate, ArrayList<String>> workedVacs = new HashMap<>();
	
	protected ArrayList<String> refusedPostes = new ArrayList<>();

}
