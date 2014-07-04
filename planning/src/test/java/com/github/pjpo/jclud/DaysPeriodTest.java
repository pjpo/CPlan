package com.github.pjpo.jclud;

import java.time.LocalDate;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.github.pjpo.planning.utils.DaysPeriod;

/**
 * Unit test for simple App.
 */
public class DaysPeriodTest extends TestCase {

	public DaysPeriodTest(String testName) {
		super(testName);
	}

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(DaysPeriodTest.class);
    }
    
    public void test1() {
    	DaysPeriod period1 = new DaysPeriod(null, null);
    	DaysPeriod period2 = new DaysPeriod(null, null);
    	assertEquals(true, period1.isOverlapping(period2));
    }

    public void test2() {
    	DaysPeriod period1 = new DaysPeriod(LocalDate.of(2001, 01, 11), null);
    	DaysPeriod period2 = new DaysPeriod(null, LocalDate.of(2001, 01, 10));
    	assertEquals(false, period1.isOverlapping(period2));
    }

    public void test3() {
    	DaysPeriod period1 = new DaysPeriod(LocalDate.of(2001, 01, 10), null);
    	DaysPeriod period2 = new DaysPeriod(null, LocalDate.of(2001, 01, 10));
    	assertEquals(true, period1.isOverlapping(period2));
    }
    
    public void test4() {
    	DaysPeriod period1 = new DaysPeriod(LocalDate.of(2001, 01, 10), LocalDate.of(2001, 01, 11));
    	DaysPeriod period2 = new DaysPeriod(LocalDate.of(2001, 01, 9), LocalDate.of(2001, 01, 12));
    	assertEquals(true, period1.isOverlapping(period2));
    }
    
}
