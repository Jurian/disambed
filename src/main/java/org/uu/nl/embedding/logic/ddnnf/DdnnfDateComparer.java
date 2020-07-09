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
	private String nam;
	
	private String comparison = "exact";
	
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
	

	/**
	 * Constructor method for this d-DNNF logic rule.
	 * @param date The date of this class.
	 */
	public DdnnfDateComparer(final SimpleDate date1, final SimpleDate date2, final String name) {
		super(name, false);
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
	
	
	
	/*
	 * Below are all interface methods implemented
	 */

	@Override
	public boolean getAssignment() {
		return false;
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