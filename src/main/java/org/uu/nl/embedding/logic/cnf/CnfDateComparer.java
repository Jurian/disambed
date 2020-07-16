package org.uu.nl.embedding.logic.cnf;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

import org.apache.log4j.Logger;
import org.uu.nl.embedding.logic.LogicRule;
import org.uu.nl.embedding.logic.ddnnf.DdnnfDate;
import org.uu.nl.embedding.logic.util.SimpleDate;

public class CnfDateComparer extends LogicLiteral {

    private final static Logger logger = Logger.getLogger(DdnnfDate.class);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-mm-yyyy");
	
    private boolean assignment;
    private String comparison;
    
	private SimpleDate date1;
	private SimpleDate date2;
	private String dateString1 = null;
	private String dateString2 = null;
	
	/**
	 * Constructor method for this d-DNNF logic rule.
	 * @param date The date of this class.
	 */
	public CnfDateComparer(final SimpleDate date1, final SimpleDate date2, final String name, final boolean assignment, final boolean isNegated) {
		super(name, assignment, isNegated);
		this.date1 = date1;
		this.date2 = date2;
		this.dateString1 = date1.toString();
		this.dateString2 = date2.toString();
		
	}
	
	public void setComparisonExact() {
		this.assignment = exactSameAs();
		
	}
	
	public void setComparisonBefore() {

		this.assignment = isBefore();
		
	}
	
	public void setComparisonAfter() {
		this.assignment = isAfter();
		
	}
	
	public void setComparisonBefore(final String intervalType, final long intervalSize) {
		this.assignment = isMaxBefore(intervalType, intervalSize);
		
	}
	
	public void setComparisonAfter(final String intervalType, final long intervalSize) {
		this.assignment = isMaxAfter(intervalType, intervalSize);
		
	}
	
	public String getComparison() {
		return this.comparison;
	}
	
	
	/**
	 * Getter method for obtaining the SimpleDate of this logic class.
	 * @return Returns the SimpleDate of this logic class.
	 */
	public SimpleDate[] getDates() {
		return new SimpleDate[] {this.date1, this.date2};
	}
	
	/**
	 * Method to check whether the otherDate is the same.
	 * @param otherDate The date to compare this date to.
	 * @return {@code true} if the difference between the dates
	 * 			is 0, and 
     *         {@code false} otherwise.
	 */
	public boolean exactSameAs() {
		comparison = "exact";
		if(differenceWith("dd") == 0) { return true; }
		else { return false; }
	}
	
	/**
	 * Method to check whether this date is before the given date.
	 * @param otherDate The date to compare this date to.
	 * @return {@code true} if the difference between the dates
	 * 			is larger than 0, and 
     *         {@code false} otherwise.
	 */
	public boolean isBefore() {
		comparison = "before";
		if(differenceWith("dd") < 0) { return true; }
		else { return false; }
	}
	
	/**
	 * Method to check whether this date is after the given date.
	 * @param otherDate The date to compare this date to.
	 * @return {@code true} if the difference between the dates
	 * 			is smaller than 0, and 
     *         {@code false} otherwise.
	 */
	public boolean isAfter() {
		comparison = "after";
		if(differenceWith("dd") > 0) { return true; }
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
	public boolean isMaxBefore(final String intervalType, final long intervalSize) {
		comparison = "maxBefore";
		long difference = differenceWith(intervalType);
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
	public boolean isMaxAfter(final String intervalType, final long intervalSize) {
		comparison = "maxAfter";
		long difference = differenceWith(intervalType);
		if(difference > 0 && difference <= intervalSize) { return true; }
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
	public long differenceWith(final String intervalType) {
		// Check if otherDate is in date format.
		if(!SimpleDate.isDateFormat(this.dateString1)) {
            logger.warn("Could not covert otherDate string to date");
            return -999;
		}// Check if otherDate is in date format.
		if(!SimpleDate.isDateFormat(this.dateString2)) {
            logger.warn("Could not covert otherDate string to date");
            return -999;
		}
		
		// Return 0 if dates are exactly the same.
        if(this.dateString1.equals(this.dateString2)) return 0;
        
        // Calculate the difference between the two dates.
        try {
        	long res;
    		
            // Calculate the difference in terms of provided time interval.
    		if(intervalType == "mm") {
               res = (long) SimpleDate.monthsToDate(this.date1, this.date2);
    			
    		} else if(intervalType == "yyyy") {
                res = (long) SimpleDate.yearsToDate(this.date1, this.date2);
    			
    		} else {
    			// if interval == "dd"
    			// if interval == any other input: assume "dd"
    			if(intervalType != "dd") { logger.warn("Not valid input for interval, therefore 'dd' was assumed"); }
                res = (long) SimpleDate.daysToDate(this.date1, this.date2);
    		}
    		return res;

        } catch (DateTimeParseException e) {
            logger.warn("Could not compare dates: " + e.getMessage());
            return -999;
        }
	}

	
	
	
	/*
	 * Below are all interface methods implemented
	 */

	@Override
	public boolean getAssignment() {
		return this.assignment;
	}

	@Override
	public void setAssignment(boolean assignment) {
		//this.assignment = assignment;
		
	}

	@Override
	public void setFalse() {
		//this.assignment = false;
		
	}

	@Override
	public void setTrue() {
		//this.assignment = true;
		
	}

	@Override
	public boolean isFalse() {
		return !this.assignment;
	}

	@Override
	public boolean isTrue() {
		return this.assignment;
	}

	@Override
	public String toString() {
		return "COMPARE(DATE(" + this.dateString1 + "), DATE(" + this.dateString2 + "))";
	}

	@Override
	public String toValueString() {
		return "DATE(" + this.assignment + ")";
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

        return this.toString() == ((CnfDateComparer) obj).toString();
    }

    @Override 
    public int hashCode() {
		int hash = 7;
		for (int i = 0; i < this.toString().length(); i++) {
			hash = hash*31 + this.toString().charAt(i);
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