package com.github.pjpo.planning.physician;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

import com.github.pjpo.planning.utils.IntervalDate;

public class PhysicianBase {

	protected String name = null;
	
	protected Integer timePart = null;
		
	protected ArrayList<IntervalDate> paidVacation = new ArrayList<>();
	
	protected ArrayList<IntervalDate> unpaidVacation = new ArrayList<>();

	protected HashMap<LocalDate, ArrayList<String>> workedVacs = new HashMap<>();
	
	protected ArrayList<String> refusedPostes = new ArrayList<>();

}
