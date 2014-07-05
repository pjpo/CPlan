package com.github.pjpo.jclud;

import java.time.LocalDate;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.github.pjpo.planning.utils.Interval;

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
    	Interval period1 = new Interval(null, null);
    	Interval period2 = new Interval(null, null);
    	assertEquals(true, period1.isOverlapping(period2));
    }

    public void test2() {
    	Interval period1 = new Interval(LocalDate.of(2001, 01, 11), null);
    	Interval period2 = new Interval(null, LocalDate.of(2001, 01, 10));
    	assertEquals(false, period1.isOverlapping(period2));
    }

    public void test3() {
    	Interval period1 = new Interval(LocalDate.of(2001, 01, 10), null);
    	Interval period2 = new Interval(null, LocalDate.of(2001, 01, 10));
    	assertEquals(true, period1.isOverlapping(period2));
    }
    
    public void test4() {
    	Interval period1 = new Interval(LocalDate.of(2001, 01, 10), LocalDate.of(2001, 01, 11));
    	Interval period2 = new Interval(LocalDate.of(2001, 01, 9), LocalDate.of(2001, 01, 12));
    	assertEquals(true, period1.isOverlapping(period2));
    }
    
    public void test5() {
    	Interval period1 = new Interval(LocalDate.of(2001, 01, 10), null);
    	Interval period2 = new Interval(null, LocalDate.of(2001, 01, 10));
    	assertEquals(true, period1.compareTo(period2) > 0);
    }

    public void test6() {
    	Interval period1 = new Interval(LocalDate.of(2001, 01, 10), null);
    	Interval period2 = new Interval(null, LocalDate.of(2001, 01, 10));
    	assertEquals(true, period2.compareTo(period1) < 0);
    }

}
