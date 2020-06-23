package org.uu.nl.embedding.logic.ddnnf;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.apache.log4j.Logger;
import org.uu.nl.embedding.logic.LogicRule;
import org.uu.nl.embedding.logic.util.SimpleDate;

public class DdnnfDate extends DdnnfLiteral {

    private final static Logger logger = Logger.getLogger(DdnnfDate.class);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-mm-yyyy");
	
    private boolean assignment;
    private String name;
    
	private final SimpleDate date;
	private final String dateString;
	
	/**
	 * Constructor method for this d-DNNF logic rule.
	 * @param date The date of this class.
	 */
	public DdnnfDate(final SimpleDate date, final String name, final boolean assignment) {
		super(name, assignment);
		this.name = name;
		this.date = date;
		
		this.dateString = date.toString();
		//this.ddnnfGraph = generateDdnnfGraph();
	}
	
	
	public String getName() {
		return "DATE(" + this.dateString + ")";
	}
	
	/**
	 * Getter method for obtaining the SimpleDate of this logic class.
	 * @return Returns the SimpleDate of this logic class.
	 */
	public SimpleDate getDate() {
		return this.date;
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
		this.assignment = assignment;
		
	}

	@Override
	public void setFalse() {
		this.assignment = false;
		
	}

	@Override
	public void setTrue() {
		this.assignment = true;
		
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
		return this.name + "(DATE(" + this.dateString + "))";
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

        return this.toString() == ((DdnnfDate) obj).toString();
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
