package uk.al_richard.BloomSearch;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Euclidean;
import testloads.TestContext;
import testloads.TestContext.Context;

import java.util.Date;
import java.util.List;

public class BloomSearch {

	public static void main(String[] args) throws Exception {

		Context context = Context.euc20;

		boolean fourPoint = true;
		boolean balanced = false;
		boolean rotationEnabled = true;
		int noOfRefPoints = 20; 							// small for now

		System.out.println("Date/time\t" + new Date().toString());

		TestContext tc = new TestContext(context);
		int query_size =  1000;
		tc.setSizes(query_size, noOfRefPoints);
		List<CartesianPoint> dat = tc.getData();
		dat = dat.subList(0,1000); 							// small for now
		List<CartesianPoint> refs = tc.getRefPoints();
		double threshold = tc.getThresholds()[0];
		List<CartesianPoint> queries = tc.getQueries();
		int nn_size = 5;

		Metric<CartesianPoint> metric = new Euclidean<>();

		System.out.println( "data size =\t" + dat.size() );
		System.out.println( "refs size =\t" + refs.size() );
		System.out.println( "query size =\t" + query_size );
		System.out.println( "NN size =\t" + nn_size );
		System.out.println( "Metric =\t" + metric.getMetricName() );

		NNMap map = new NNMap( refs, dat, metric, nn_size );
	}


}
