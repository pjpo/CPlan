package com.github.pjpo.planning;

import java.time.LocalDate;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.github.pjpo.planning.utils.IntervalDate;

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
    	IntervalDate period1 = new IntervalDate();
    	IntervalDate period2 = new IntervalDate();
    	assertEquals(true, period1.isOverlapping(period2));
    }

    public void test2() {
    	IntervalDate period1 = new IntervalDate(LocalDate.of(2001, 01, 11), null);
    	IntervalDate period2 = new IntervalDate(null, LocalDate.of(2001, 01, 10));
    	assertEquals(false, period1.isOverlapping(period2));
    }

    public void test3() {
    	IntervalDate period1 = new IntervalDate(LocalDate.of(2001, 01, 10), null);
    	IntervalDate period2 = new IntervalDate(null, LocalDate.of(2001, 01, 10));
    	assertEquals(true, period1.isOverlapping(period2));
    }
    
    public void test4() {
    	IntervalDate period1 = new IntervalDate(LocalDate.of(2001, 01, 10), LocalDate.of(2001, 01, 11));
    	IntervalDate period2 = new IntervalDate(LocalDate.of(2001, 01, 9), LocalDate.of(2001, 01, 12));
    	assertEquals(true, period1.isOverlapping(period2));
    }
    
    public void test5() {
    	IntervalDate period1 = new IntervalDate(LocalDate.of(2001, 01, 10), null);
    	IntervalDate period2 = new IntervalDate(null, LocalDate.of(2001, 01, 10));
    	assertEquals(true, period1.compareTo(period2) > 0);
    }

    public void test6() {
    	IntervalDate period1 = new IntervalDate(LocalDate.of(2001, 01, 10), null);
    	IntervalDate period2 = new IntervalDate(null, LocalDate.of(2001, 01, 10));
    	assertEquals(true, period2.compareTo(period1) < 0);
    }

}
