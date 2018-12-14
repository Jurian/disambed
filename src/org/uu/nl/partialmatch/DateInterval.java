package org.uu.nl.partialmatch;

import com.brein.time.timeintervals.intervals.LongInterval;

/**
 * Approximate interval representation for fuzzy dates
 * @author Jurian Baas
 *
 */
public class DateInterval {

	public static final int INTERVAL_YEAR = 365;
	public static final int INTERVAL_MONTH = 30;
	public static final int INTERVAL_DAY = 1;
	
	public static LongInterval fromDateString(String dateString) {
		dateString = dateString.toLowerCase();
		
		int year = 0, month = 0, day = 0;
		long interval = 0;
		long start;
		
		// Input is of format "yyyy"
		if(!dateString.contains("-")) {
			year = Integer.parseInt(dateString);
			start = year * INTERVAL_YEAR;
			interval = INTERVAL_YEAR;
		} else {
			
			String[] segments = dateString.split("-");
			
			year = Integer.parseInt(segments[0]);
			start = year * INTERVAL_YEAR;
			interval = INTERVAL_YEAR;
			
			// Input is at least of format "yyyy-mm" with only numbers
			if(segments.length == 2 && containsOnlyNumbers(segments[1])) {
				month = Integer.parseInt(segments[1]);
				interval = INTERVAL_MONTH;
				start += month * INTERVAL_MONTH;
			}
					
			// Input is of format "yyyy-mm-dd" with only numbers
			if(segments.length == 3 && containsOnlyNumbers(segments[2])) {
				month = Integer.parseInt(segments[1]);
				interval = INTERVAL_MONTH;
				start += month * INTERVAL_MONTH;
				
				day = Integer.parseInt(segments[2]);
				interval = INTERVAL_DAY;
				start += day * INTERVAL_DAY;
			}

		}
		
		return new LongInterval(start, start + interval, true, false);
	}
	
	public static String toString(LongInterval i) {
		long year = i.getStart() / INTERVAL_YEAR;
		long month = (i.getStart() - year * INTERVAL_YEAR) / INTERVAL_MONTH;
		long day = (i.getStart() - year * INTERVAL_YEAR - month * INTERVAL_MONTH) / INTERVAL_DAY;
		return year + (month == 0 ? "" : "-" + month) + (day == 0 ? "" : "-" + day);
	}
	
	public static double jaccard(LongInterval i1, LongInterval i2) {
		
		if(!i1.overlaps(i2)) return 0;
		
		long intersection = Math.min(i1.getEnd(), i2.getEnd()) - Math.max(i1.getStart(), i2.getStart());
		long union = Math.max(i1.getEnd(), i2.getEnd()) - Math.min(i1.getStart(), i2.getStart());
		
		return intersection / (double) union;
	}
	
	private static boolean containsOnlyNumbers(String s) {
		return s.matches("^[0-9]*$");
	}
}
