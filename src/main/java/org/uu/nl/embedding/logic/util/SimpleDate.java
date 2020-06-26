/**
 * 
 */
package org.uu.nl.embedding.logic.util;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;


/**
 * Class for a simple date format
 * 
 * @author Euan Westenbroek
 * @version 1.3
 * @since 17-06-2020
 */
public class SimpleDate {

    private final static Logger logger = Logger.getLogger(SimpleDate.class);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-mm-yyyy");
    
    private String date;
    private boolean isDate;
	private int[] intDate = new int[3];

    public SimpleDate(final String pattern) {
    	this.date = pattern;
    	this.intDate = getDateAsIntArray(pattern);
    	this.isDate = isDateFormat(pattern);
    }
    
    public String toString() {
    	return this.date;
    }

	/**
	 * 
	 * @return Returns the date as an integer array as [dd, mm, yyyy]
	 */
    public int[] getDateAsIntArray() {
    	return this.intDate;
    }

	/**
	 * 
	 * Sets the date as an integer array as [dd, mm, yyyy]
	 */
    public static int[] getDateAsIntArray(final String dateString) {
    	int[] resInts = new int[3];
    	List<String> tokens = Arrays.asList(dateString.split("-"));
    	
    	for(int i = 0; i < tokens.size(); i++) {
			resInts[i] = Integer.parseInt(tokens.get(i)); 
    		try {
    			resInts[i] = (int) Integer.parseInt(tokens.get(i));
    			if (i == 0 && (resInts[i] < 1 || resInts[i] > 31)) {
    				throw new IllegalArgumentException("Invalid date format: " + dateString);
    			} else if (i == 1 && (resInts[i] < 1 || resInts[i] > 12)) {
    				throw new IllegalArgumentException("Invalid date format: " + dateString);
    			} else if (i == 2 && resInts[i] < 0) {
    				throw new IllegalArgumentException("Invalid date format: " + dateString);
    			}
    		} catch (NumberFormatException e) { break; }
    	}
    	return resInts;
    }
    
    public static boolean isDateFormat(String pattern) {
    	int[] res = getDateAsIntArray(pattern);
        if(res[0] > 0 && res[0] < 31 && res[1] > 0 && res[1] < 13 && res[2] > 0) {
        	return true; }
        return false;
    }

    public long daysToDate(final SimpleDate secondDate) {
    	return daysToDate(this, secondDate);
    }
    
    public static long daysToDate(final SimpleDate firstDate, final SimpleDate secondDate) {

        if (firstDate == null) {
            throw new NullPointerException("This date must not be null");
        } else if (secondDate == null) {
            throw new NullPointerException("secondDate must not be null");
        }

        if (firstDate.toString().equals(secondDate.toString())) return 0;

        try {

        	int[] firstDateInts = getDateAsIntArray(firstDate.toString());
        	int[] secondDateInts = getDateAsIntArray(secondDate.toString());
    		long diff = calculateDaysBetween(firstDateInts, secondDateInts);

        	return calculateDaysBetween(firstDateInts, secondDateInts);
        	
        } catch (DateTimeParseException e) {
            logger.warn("Could not compare dates: " + e.getMessage());
            return 0;
        }
    }
    
    /*
     * DEZE GAAT NOG NIET EXACT GOED:
     * 		1.	SCHRIKKELJAREN GAAN BLIJKBAAR NOG STEEDS NIET GOED.
     */
	public static long calculateDaysBetween(final int[] date1, final int[] date2) {
		long res = 0;
		boolean date1IsEarlier;
		
		// date2 is before date1
		if (date1[2] > date2[2]) { date1IsEarlier = false; }
		// date2 is after date1
		else if (date1[2] < date2[2]) { date1IsEarlier = true; }
		// date2 is before date1
		else if (date1[1] > date2[1]) { date1IsEarlier = false; }
		// date2 is after date1
		else if (date1[1] < date2[1]) { date1IsEarlier = true; }
		// date2 is before date1
		else if (date1[0] > date2[0]) { date1IsEarlier = false; }
		// date2 is after date1
		else if (date1[0] < date2[0]) { date1IsEarlier = true; }
		// Dates are exactly the same, thus return 0.
		else { return res; }

		int day1, mon1, year1;
		int day2, mon2, year2;
		// Initialize variables correspondingly.
		if (date1IsEarlier) {
			day1=date1[0]; mon1=date1[1]; year1=date1[2];
			
			day2=date2[0]; mon2=date2[1]; year2=date2[2];
		} else {
			day1=date2[0]; mon1=date2[1]; year1=date2[2];
			day2=date1[0]; mon2=date1[1]; year2=date1[2];
		}
		
		
		//days 
		if (day1 > day2) { 
			res += daysInMonth(mon1, year1) - day1 + day2;
			mon1++;
		} else if (day1 < day2) {
			res += day2 - day1;
		}
		//months
		while (mon1 != mon2) {
			res += daysInMonth(mon1, year1);
			mon1++;
			if (mon1 == 13) { mon1 = 1; year1++; }
		}
		// years
		while (year1 < year2) {
			res += daysInYear(year1);
			year1++;
		}

		// Return the result.
		if (date1IsEarlier) { return res; }
		else { return -1*res; }

	}
    
    public static long monthsToDate(final SimpleDate firstDate, final SimpleDate secondDate) {

        if (firstDate == null) {
            throw new NullPointerException("This date must not be null");
        } else if (secondDate == null) {
            throw new NullPointerException("secondDate must not be null");
        }

        if (firstDate.toString().equals(secondDate.toString())) return 0;

        try {

        	int[] firstDateInts = getDateAsIntArray(firstDate.toString());
        	int[] secondDateInts = getDateAsIntArray(secondDate.toString());
        	return calculateMonthsBetween(firstDateInts, secondDateInts);
        	
        } catch (DateTimeParseException e) {
            logger.warn("Could not compare dates: " + e.getMessage());
            return 0;
        }
    }
    
	public static long calculateMonthsBetween(final int[] date1, final int[] date2) {
		long res = 0;
		boolean date1IsEarlier;

		if (date1[2] > date2[2]) { date1IsEarlier = false; }
		else if (date1[2] < date2[2]) { date1IsEarlier = true; }
		else if (date1[1] < date2[1]) { date1IsEarlier = true; }
		else if (date1[0] < date2[0]) { date1IsEarlier = true; }
		else { return res; }
		
		if (date1IsEarlier) {
			res += date2[2] - date1[2];
			res = (res*12);
			if ((date2[1] < date1[1])) {
				res -= 12;
				res = ( res + (12-(date1[1] - date2[1])) );
			}
			else if ((date2[1] > date1[1])) {
				res += date2[1] - date1[1];
			}
			if ((date2[0] - date1[0]) < 0) { res--; }
			return res;
		} else {
			res += date1[2] - date2[2];
			res = (res*12);
			if ((date1[1] < date2[1])) {
				res -= 12;
				res = ( res + (12-(date2[1] - date1[1])) );
			}
			else if ((date2[1] > date1[1])) {
				res += date2[1] - date1[1];
			}
			if ((date1[0] - date2[0]) < 0) { res--; }
			return -1*res;
		}
	}
    
    public static long yearsToDate(final SimpleDate firstDate, final SimpleDate secondDate) {

        if (firstDate == null) {
            throw new NullPointerException("This date must not be null");
        } else if (secondDate == null) {
            throw new NullPointerException("secondDate must not be null");
        }

        if (firstDate.toString().equals(secondDate.toString())) return 0;

        try {

        	int[] firstDateInts = getDateAsIntArray(firstDate.toString());
        	int[] secondDateInts = getDateAsIntArray(secondDate.toString());
        	return calculateYearsBetween(firstDateInts, secondDateInts);
        	
        } catch (DateTimeParseException e) {
            logger.warn("Could not compare dates: " + e.getMessage());
            return 0;
        }
    }
    
	public static long calculateYearsBetween(final int[] date1, final int[] date2) {
		long res = 0;
		boolean date1IsEarlier;

		if (date1[2] > date2[2]) { date1IsEarlier = false; }
		else if (date1[2] < date2[2]) { date1IsEarlier = true; }
		else if (date1[1] < date2[1]) { date1IsEarlier = true; }
		else if (date1[0] < date2[0]) { date1IsEarlier = true; }
		else { return res; }
		
		if (date1IsEarlier) {
			res += date2[2] - date1[2];
			if ((date2[1] - date1[1]) < 0) { res--; }
			else if ((date2[1] - date1[1]) == 0 && (date2[0] - date1[0]) < 0) { res--; }
			return res;
		} else {
			res += date1[2] - date2[2];
			if ((date1[1] - date1[1]) < 0) { res--; }
			else if ((date1[1] - date2[1]) == 0 && (date1[0] - date2[0]) < 0) { res--; }
			return -1*res;
		}
	}
	
    public static int daysInMonth(final int month, final int year) {
    	switch(month) {
    		case 1: return 31;
    		case 2: if ( (year % 4 == 0) & (!(year % 100 == 0) && (year % 400 == 0)) ) { return 29; } else { return 28; }
    		/*case 2: if (year % 400 == 0) { return 29; }
    				else if ( (year % 4 == 0) && !(year % 100 == 0) ) { return 29; }
    				else { return 28; }*/
    		case 3: return 31;
    		case 4: return 30;
    		case 5: return 31;
    		case 6: return 30;
    		case 7: return 31;
    		case 8: return 31;
    		case 9: return 30;
    		case 10: return 31;
    		case 11: return 30;
    		case 12: return 31;
    		default: throw new IllegalArgumentException("Month isn't one of the 12.");
    	}
    }
    
    public static int daysInYear(final int year) {
    	if ( (year % 4 == 0) & (!(year % 100 == 0) && (year % 400 == 0)) ) { 
    		return 366;
    	} else { return 365; }
    }
}
