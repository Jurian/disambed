package org.uu.nl.embedding.logic.cnf;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.apache.log4j.Logger;
import org.uu.nl.embedding.logic.LogicRule;
import org.uu.nl.embedding.logic.util.SimpleDate;

public class CnfDateLogic extends LogicLiteral {

    private final static Logger logger = Logger.getLogger(CnfDateLogic.class);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-mm-yyyy");

	private boolean assignment;
	private SimpleDate date;
	private String dateString;
	private String logicString;
	private String name;
	
	
	/**
	 * Constructor method for CnfDateLogic class
	 * @param date The string to be converted
	 */
	public CnfDateLogic(final String date, String name, final boolean assignment, final boolean isNegated) {
		super(name, assignment, isNegated);
		if(!SimpleDate.isDateFormat(date)) {
			{ throw new IllegalArgumentException("Given string is not in the appropriate date format."); }
		}
		this.assignment = assignment;
		this.date = new SimpleDate(date);
		this.dateString = date;
		this.name = ("DATE(" + date + ")");
	}

	/**
	 * Constructor method for CnfDateLogic class
	 * @param date The SimpleDate to be converted
	 */
	public CnfDateLogic(SimpleDate date, String name, final boolean assignment, final boolean isNegated) {
		super(name, assignment, isNegated);

		this.assignment = assignment;
		this.date = date;
		this.dateString = date.toString();
		this.name = name;
		this.logicString = ("DATE(" + date.toString() + ")");
	}
	
	/**
	 * Method to check whether the otherDate is the same.
	 * @param otherDate The date to compare this date to.
	 * @return {@code true} if the difference between the dates
	 * 			is 0, and 
     *         {@code false} otherwise.
	 */
	public boolean exactSameAs(final String otherDate) {
		if(differenceWith(otherDate, "dd") == 0) { return true; }
		else { return false; }
	}

	/**
	 * Method to check whether the otherDate is the same.
	 * @param otherDate The date to compare this date to.
	 * @return {@code true} if the difference between the dates
	 * 			is 0, and 
     *         {@code false} otherwise.
	 */
	public boolean exactSameAs(final SimpleDate otherDate) {
		if(differenceWith(otherDate, "dd") == 0) { return true; }
		else { return false; }
	}
	
	/**
	 * Method to check whether this date is before the given date.
	 * @param otherDate The date to compare this date to.
	 * @return {@code true} if the difference between the dates
	 * 			is larger than 0, and 
     *         {@code false} otherwise.
	 */
	public boolean isBefore(final String otherDate) {
		if(differenceWith(otherDate, "dd") > 0) { return true; }
		else { return false; }
	}	

	/**
	 * Method to check whether this date is before the given date.
	 * @param otherDate The date to compare this date to.
	 * @return {@code true} if the difference between the dates
	 * 			is larger than 0, and 
     *         {@code false} otherwise.
	 */
	public boolean isBefore(final SimpleDate otherDate) {
		if(differenceWith(otherDate, "dd") > 0) { return true; }
		else { return false; }
	}
	
	/**
	 * Method to check whether this date is after the given date.
	 * @param otherDate The date to compare this date to.
	 * @return {@code true} if the difference between the dates
	 * 			is smaller than 0, and 
     *         {@code false} otherwise.
	 */
	public boolean isAfter(final String otherDate) {
		if(differenceWith(otherDate, "dd") < 0) { return true; }
		else { return false; }
	}
	
	/**
	 * Method to check whether this date is after the given date.
	 * @param otherDate The date to compare this date to.
	 * @return {@code true} if the difference between the dates
	 * 			is smaller than 0, and 
     *         {@code false} otherwise.
	 */
	public boolean isAfter(final SimpleDate otherDate) {
		if(differenceWith(otherDate, "dd") < 0) { return true; }
		else { return false; }
	}

	/**
	 * Method to check whether this date is no more than the given
	 * interval size before the given date.
	 * @param otherDate The date to compare this date to.
	 * @param intervalType The type of interval indicating if
	 * the comparison should be based on days ("dd"), months ("mm"),
	 * or years ("yyyy").
	 * @param intervalSize The maximally allowed difference number of
	 * interval types
	 * @return {@code true} if the difference between the dates
	 * 			is bigger than 0 but smaller than intervalSize, and 
     *         {@code false} otherwise.
	 */
	public boolean isMaxBefore(final String otherDate, final String intervalType, final long intervalSize) {
		long difference = differenceWith(otherDate, intervalType);
		if(difference > 0 && difference <= intervalSize) { return true; }
		else { return false; }
	}

	/**
	 * Method to check whether this date is no more than the given
	 * interval size before the given date.
	 * @param otherDate The date to compare this date to.
	 * @param intervalType The type of interval indicating if
	 * the comparison should be based on days ("dd"), months ("mm"),
	 * or years ("yyyy").
	 * @param intervalSize The maximally allowed difference number of
	 * interval types
	 * @return {@code true} if the difference between the dates
	 * 			is bigger than 0 but smaller than intervalSize, and 
     *         {@code false} otherwise.
	 */
	public boolean isMaxBefore(final SimpleDate otherDate, final String intervalType, final long intervalSize) {
		long difference = differenceWith(otherDate, intervalType);
		if(difference > 0 && difference <= intervalSize) { return true; }
		else { return false; }
	}

	
	/**
	 * Method to check whether this date is no more than the given
	 * interval size after the given date.
	 * @param otherDate The date to compare this date to.
	 * @param intervalType The type of interval indicating if
	 * the comparison should be based on days ("dd"), months ("mm"),
	 * or years ("yyyy").
	 * @param intervalSize The maximally allowed difference number of
	 * interval types
	 * @return {@code true} if the difference between the dates
	 * 			is smaller than 0 and bigger than intervalSize, and 
     *         {@code false} otherwise.
	 */
	public boolean isMaxAfter(final String otherDate, final String intervalType, final long intervalSize) {
		long difference = differenceWith(otherDate, intervalType);
		if(difference < 0 && difference >= intervalSize) { return true; }
		else { return false; }
	}
	
	/**
	 * Method to check whether this date is no more than the given
	 * interval size after the given date.
	 * @param otherDate The date to compare this date to.
	 * @param intervalType The type of interval indicating if
	 * the comparison should be based on days ("dd"), months ("mm"),
	 * or years ("yyyy").
	 * @param intervalSize The maximally allowed difference number of
	 * interval types
	 * @return {@code true} if the difference between the dates
	 * 			is smaller than 0 and bigger than intervalSize, and 
     *         {@code false} otherwise.
	 */
	public boolean isMaxAfter(final SimpleDate otherDate, final String intervalType, final long intervalSize) {
		long difference = differenceWith(otherDate, intervalType);
		if(difference < 0 && difference >= intervalSize) { return true; }
		else { return false; }
	}
	
	
	/**
	 * Method to calculate the difference between this date and
	 * the given otherDate.
	 * @param otherDate The date to compare this date with.
	 * @param intervalType The type of interval indicating if
	 * the comparison should be based on days ("dd"), months ("mm"),
	 * or years ("yyyy").
	 * @return Returns a long indicating the difference in terms of
	 * the provided interval type.
	 */
	public long differenceWith(final String otherDate, final String intervalType) {
		// Check if otherDate is in date format.
		if(!SimpleDate.isDateFormat(this.dateString)) {
            logger.warn("Could not covert otherDate string to date");
            return -999;
		}// Check if otherDate is in date format.
		if(!SimpleDate.isDateFormat(otherDate)) {
            logger.warn("Could not covert otherDate string to date");
            return -999;
		}
		
		// Return 0 if dates are exactly the same.
        if(this.dateString.equals(otherDate)) return 0;
        
        // Calculate the difference between the two dates.
        try {
        	long res;
        	SimpleDate date2 = new SimpleDate(otherDate);
    		
            // Calculate the difference in terms of provided time interval.
    		if(intervalType == "mm") {
               res = (long) SimpleDate.monthsToDate(this.date, date2);
    			
    		} else if(intervalType == "yyyy") {
                res = (long) SimpleDate.yearsToDate(this.date, date2);
    			
    		} else {
    			// if interval == "dd"
    			// if interval == any other input: assume "dd"
    			if(intervalType != "dd") { logger.warn("Not valid input for interval, therefore 'dd' was assumed"); }
                res = (long) SimpleDate.daysToDate(this.date, date2);
    		}
    		return res;

        } catch (DateTimeParseException e) {
            logger.warn("Could not compare dates: " + e.getMessage());
            return -999;
        }
	}
	
	/**
	 * Method to calculate the difference between this date and
	 * the given otherDate.
	 * @param otherDate The date to compare this date with.
	 * @param intervalType The type of interval indicating if
	 * the comparison should be based on days ("dd"), months ("mm"),
	 * or years ("yyyy").
	 * @return Returns a long indicating the difference in terms of
	 * the provided interval type.
	 */
	public long differenceWith(final SimpleDate otherDate, final String intervalType) {
		return differenceWith(otherDate.toString(), intervalType);
	}
	
	/**
	 * Getter method for obtaining the SimpleDate of this logic class.
	 * @return Returns the SimpleDate of this logic class.
	 */
	public SimpleDate getDate() {
		return this.date;
	}

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (!getClass().equals(obj.getClass())) {
            return false;
        }

        return this.getName() == ((CnfDateLogic) obj).getName();
    }

    @Override 
    public int hashCode() {
		int hash = 7;
		for (int i = 0; i < this.name.length(); i++) {
			hash = hash*31 + this.name.charAt(i);
		}
		return hash;
    }

    @Override 
    public int compareTo(LogicRule other) {

      if (this.hashCode() < other.hashCode()) {
        return -1;
      }
      return this.hashCode() == other.hashCode() ? 0 : 1;
    }

}
