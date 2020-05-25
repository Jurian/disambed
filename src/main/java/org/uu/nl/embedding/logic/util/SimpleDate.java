/**
 * 
 */
package org.uu.nl.embedding.logic.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

import org.apache.log4j.Logger;


/**
 * Class for a simple date format
 * 
 * @author Euan Westenbroek
 * @version 1.1
 * @since 13-05-2020
 */
public class SimpleDate {

    private final static Logger logger = Logger.getLogger(SimpleDate.class);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-mm-yyyy");
    
    private String date;
    private boolean isDate;

    public SimpleDate(final String pattern) {
    	this.date = null;
    	isDate = checkDateFormat(this.date);
    	throwException();
    }
    
    public String toString() {
    	return this.date;
    }
    
    private void throwException() {
    	if(!this.isDate) {
        	throw new IllegalArgumentException("Invalid date format: " + this.date);
    	}
    }
    
    public boolean checkDateFormat(String pattern) {
    	try {
	    	switch(pattern) {
	    		// Check for universal date format
				case "\\0[1-9]-\\0[1-9]-\\d{4}":
	            	this.date = pattern;
	    			return true;
				case "\\[1-2][0-9]-\\0[1-9]-\\d{4}":
	            	this.date = pattern;
	    			return true;
	    		case "\\3[0-2]-\\0[1-9]-\\d{4}":
	            	this.date = pattern;
	    			return true;
				case "\\0[1-9]-\\1[0-9]-\\d{4}":
	            	this.date = pattern;
	    			return true;
				case "\\[1-2][0-9]-\\1[0-9]-\\d{4}":
	            	this.date = pattern;
	    			return true;
	    		case "\\3[0-2]-\\1[0-9]-\\d{4}":
	            	this.date = pattern;
	    			return true;
	    	}
	    	    	
	    } catch(DateTimeParseException e) {
                logger.warn("Could not covert to date: " + e.getMessage());
	    	}
		return false;
    }
    
    public static boolean isDateFormat(String pattern) {
    	switch(pattern) {
    		// Check for universal date format
			case "\\0[1-9]-\\0[1-9]-\\d{4}":
    			return true;
			case "\\[1-2][0-9]-\\0[1-9]-\\d{4}":
    			return true;
    		case "\\3[0-2]-\\0[1-9]-\\d{4}":
    			return true;
			case "\\0[1-9]-\\1[0-9]-\\d{4}":
    			return true;
			case "\\[1-2][0-9]-\\1[0-9]-\\d{4}":
    			return true;
    		case "\\3[0-2]-\\1[0-9]-\\d{4}":
    			return true;
    		default:
    			return false;
    	}
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
