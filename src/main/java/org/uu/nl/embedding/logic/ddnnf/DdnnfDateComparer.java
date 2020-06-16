package org.uu.nl.embedding.logic.ddnnf;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.uu.nl.embedding.lensr.DdnnfGraph;
import org.uu.nl.embedding.logic.LogicRule;
import org.uu.nl.embedding.logic.cnf.CnfLogicRule;
import org.uu.nl.embedding.logic.util.SimpleDate;

public class DdnnfDateComparer extends DdnnfLiteral {

    private final static Logger logger = Logger.getLogger(DdnnfDate.class);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-mm-yyyy");
	
	private SimpleDate date1;
	private SimpleDate date2;
	private final String dateString1;
	private final String dateString2;
	
	private String comparison = "exact";
	private String intervalType = "dd";
	private long intervalSize = 0;
	
	/**
	 * Constructor method for this d-DNNF logic rule.
	 * @param date The date of this class.
	 */
	public DdnnfDateComparer(SimpleDate date1, SimpleDate date2) {
		super(("COMPARE(" + date1.toString() + ", " + date2.toString()+ ")"), false);
		this.date1 = date1;
		this.date2 = date2;
		this.dateString1 = date1.toString();
		this.dateString2 = date2.toString();
		//this.ddnnfGraph = generateDdnnfGraph();
	}
	
	/*
	private DdnnfGraph generateDdnnfGraph() {
		DdnnfGraph resGraph;
		if(comparison == "before") {
			
		} else if(comparison == "after") {
			
		} else if(comparison == "maxBefore") {
			
		} else if(comparison == "maxAfter") {
			
		} else /* if(comparison == "exact") or anything else {
			
		}
		return resGraph;
	}
	 */
	
	
	/**
	 * Getter method for obtaining the SimpleDate of this logic class.
	 * @return Returns the SimpleDate of this logic class.
	 */
	public SimpleDate[] getDates() {
		return new SimpleDate[] {this.date1, this.date2};
	}
	
	public String getComparison() {
		return this.comparison;
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
		if(differenceWith("dd") > 0) { return true; }
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
		if(differenceWith("dd") < 0) { return true; }
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
	public boolean isMaxAfter(final String intervalType, final long intervalSize) {
		comparison = "maxAfter";
		long difference = differenceWith(intervalType);
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
            final LocalDate d1 = LocalDate.parse(this.dateString1, formatter);
            final LocalDate d2 = LocalDate.parse(this.dateString2, formatter);
            boolean isBefore = d1.isBefore(d2);
    		
            // Calculate the difference in terms of provided time interval.
    		if(intervalType == "mm") {
               res = (long) Math.abs(ChronoUnit.MONTHS.between(d1, d2));
               if(isBefore) { return res; }
               return -1*res;
    			
    		} else if(intervalType == "yyyy") {
                res = (long) Math.abs(ChronoUnit.YEARS.between(d1, d2));
                if(isBefore) { return res; }
                return -1*res;
    			
    		} else {
    			// if interval == "dd"
    			// if interval == any other input: assume "dd"
    			if(intervalType != "dd") { logger.warn("Not valid input for interval, therefore 'dd' was assumed"); }
                res = (long) Math.abs(ChronoUnit.DAYS.between(d1, d2));
                if(isBefore) { return res; }
                return -1*res;
    		}

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
		return exactSameAs();
	}

	@Override
	public void setAssignment(boolean value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFalse() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTrue() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isFalse() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isTrue() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName() {
		return "COMPARE(DATE(" + this.dateString1 + "," + this.dateString1 + "))";
	}

	@Override
	public String getCnfName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDdnnfName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LogicRule getPrecedent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LogicRule getAntecedent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CnfLogicRule getSourceRule() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<DdnnfLiteral> getLiterals() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<DdnnfLiteral> getPositiveLiterals() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<DdnnfLiteral> getNegativeLiterals() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DdnnfLogicRule> getRules() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/*
	@Override
	public String toString() {
		return "DATE(" + this.date1String + ")";
	}*/

	@Override
	public String toValueString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void negate() {
		// TODO Auto-generated method stub
		
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

        return this.hashCode() == ((DdnnfDateComparer) obj).hashCode();
    }

    @Override 
    public int hashCode() {
		int hash = 7;
		for (int i = 0; i < this.date1.toString().length(); i++) {
			hash = hash*31 + this.date2.toString().charAt(i);
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