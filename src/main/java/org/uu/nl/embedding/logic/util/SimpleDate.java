/**
 * 
 */
package org.uu.nl.embedding.logic.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

import org.apache.log4j.Logger;


/**
 * Class for a simple date format
 * 
 * @author Euan Westenbroek
 * @version 1.2
 * @since 13-05-2020
 */
public class SimpleDate {

    private final static Logger logger = Logger.getLogger(SimpleDate.class);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-mm-yyyy");
    
    private String date;
    private boolean isDate;
	private int[] intDate = new int[3];

    public SimpleDate(final String pattern) {
    	this.date = pattern;
    	isDate = checkDateFormat(this.date);
    	checkForException();
    	setDateAsIntArray();
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
    

    private void checkForException() {
    	if(!this.isDate) {
        	throw new IllegalArgumentException("Invalid date format: " + this.date);
    	}
    }

	/**
	 * 
	 * Sets the date as an integer array as [dd, mm, yyyy]
	 */
    private void setDateAsIntArray() {
    	String[] tokens = this.date.split("-");
    	for(int i = 0; i < tokens.length; i++) {
    		try { this.intDate[i] = Integer.parseInt(tokens[i]); }
    		catch (NumberFormatException e) { break; }
    	}
    }
    
    public boolean checkDateFormat(String pattern) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            dateFormat.setLenient(false);
            try {
                dateFormat.parse(pattern.trim());
            } catch (ParseException pe) {
                return false;
            }
            return true;
        }
    
    public static boolean isDateFormat(String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(pattern.trim());
        } catch (ParseException pe) {
            return false;
        }
        return true;
    }


    public long daysToDate(SimpleDate secondDate) {

        if (this.date == null) {
            throw new NullPointerException("This date must not be null");
        } else if (secondDate == null) {
            throw new NullPointerException("secondDate must not be null");
        }

        if (this.date.equals(secondDate.toString())) return 0;

        try {
        	
            final LocalDate d1 = LocalDate.parse(this.date, formatter);
            final LocalDate d2 = LocalDate.parse(secondDate.toString(), formatter);

            return (long) ChronoUnit.DAYS.between(d1, d2);

        } catch (DateTimeParseException e) {
            logger.warn("Could not compare dates: " + e.getMessage());
            return 0;
        }
    }
}
