package org.uu.nl.embedding.analyze.partialmatch;

import java.util.Collection;

import com.brein.time.timeintervals.collections.ListIntervalCollection;
import com.brein.time.timeintervals.indexes.IntervalTree;
import com.brein.time.timeintervals.indexes.IntervalTreeBuilder;
import com.brein.time.timeintervals.indexes.IntervalTreeBuilder.IntervalType;
import com.brein.time.timeintervals.intervals.IInterval;
import com.brein.time.timeintervals.intervals.LongInterval;

public class PartialMatching {

	private final IntervalTree tree;
	
	public PartialMatching() {
		
		this.tree = IntervalTreeBuilder.newBuilder()
				.usePredefinedType(IntervalType.LONG)
				.collectIntervals(interval -> new ListIntervalCollection())
				.build();

		tree.add(DateInterval.fromDateString("1641"));
		tree.add(DateInterval.fromDateString("1641-03-16"));
		tree.add(DateInterval.fromDateString("1641-03"));
		tree.add(DateInterval.fromDateString("1641-01-01"));
		tree.add(DateInterval.fromDateString("1642-11"));
		tree.add(DateInterval.fromDateString("1642-09-16"));
		tree.add(DateInterval.fromDateString("1642-09"));
		tree.add(DateInterval.fromDateString("1642-09-01"));
		
		Collection<IInterval> result;
		LongInterval test1 = DateInterval.fromDateString("1641");
		result = tree.overlap(test1);
		for(IInterval i : result ) {
			LongInterval li = (LongInterval) i;
			//System.out.println(DateInterval.toString(li) + ", " + DateInterval.jaccard(test1, li));
		}
		System.out.println();
		LongInterval test2 = DateInterval.fromDateString("1641-03");
		result = tree.overlap(test2);
		for(IInterval i : result ) {
			LongInterval li = (LongInterval) i;
			//System.out.println(DateInterval.toString(li) + ", " + DateInterval.jaccard(test1, li));
		}
		
		System.out.println(DateInterval.jaccard(
				DateInterval.fromDateString("1641"), 
				DateInterval.fromDateString("1641")
			));
		
		System.out.println(DateInterval.jaccard(
				DateInterval.fromDateString("1641-03"), 
				DateInterval.fromDateString("1641-03")
			));
		
		System.out.println(DateInterval.jaccard(
				DateInterval.fromDateString("1641-03-11"), 
				DateInterval.fromDateString("1641-03-11")
			));
		
		System.out.println(DateInterval.jaccard(
				DateInterval.fromDateString("1641-03"), 
				DateInterval.fromDateString("1641-03-11")
			));
			
		System.out.println(DateInterval.jaccard(
				DateInterval.fromDateString("1641-03"), 
				DateInterval.fromDateString("1641")
			));
	}
}
