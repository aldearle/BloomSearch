package uk.al_richard.BloomSearch;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Euclidean;
import testloads.TestContext;
import testloads.TestContext.Context;
import util.OrderedList;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * A driver program to test the BloomSearch algorithm
 */
public class BloomSearch {

	public static void main(String[] args) throws Exception {

		Context context = Context.euc20;

		boolean fourPoint = true;
		boolean balanced = false;
		boolean rotationEnabled = true;

		System.out.println("Date/time\t" + new Date());

		int noOfRefPoints = 100; 							// small for now
		TestContext tc = new TestContext(context);
		int query_size =  1000;
		tc.setSizes(query_size, noOfRefPoints);
		List<CartesianPoint> dat = tc.getData();
		dat = dat.subList(0,1000); 							// small for now
		List<CartesianPoint> refs = tc.getRefPoints();
		double threshold = tc.getThresholds()[0];
		List<CartesianPoint> queries = tc.getQueries();
		int nn_size = 50;

		int hash_size_in_bits = 12;
		int hash_overlap = 2;
		int source_length = 24;

		double bloom_width = Math.pow(2,hash_size_in_bits);

		Metric<CartesianPoint> metric = new Euclidean<>();

		System.out.println( "data size =\t" + dat.size() );
		System.out.println( "refs size =\t" + refs.size() );
		System.out.println( "query size =\t" + query_size );
		System.out.println( "NN size =\t" + nn_size );
		System.out.println( "Metric =\t" + metric.getMetricName() );
		System.out.println( "Hash size=\t" + hash_size_in_bits );
		System.out.println( "Hash overlap=\t" + hash_overlap );
		System.out.println( "Bloom width =\t" + bloom_width );
		System.out.println( "Source length =\t" + source_length );

		NNMap map = new NNMap( refs, dat, metric, nn_size, bloom_width, hash_size_in_bits, hash_overlap, source_length );

		CartesianPoint query = queries.get(0);

		Set<CartesianPoint> results = map.search( query );
		int count = 1;
		for( CartesianPoint point : results ) {
			System.out.println( "result " + count++ + " distance = " + metric.distance(query,point) );
		}
		bruteForce(query,dat,metric);
	}

	private static void bruteForce(CartesianPoint query, List<CartesianPoint> dat, Metric<CartesianPoint> metric) {
		OrderedList<CartesianPoint, Double> ol = new OrderedList<>(5);
		for( CartesianPoint point : dat ) {
			ol.add( point, metric.distance(query,point) );
		}
		System.out.println( "5 closest distances to query are:");
		int count = 1;
		for( Double dist : ol.getComparators() ) {
			System.out.println( "True " + count++ + " distance = " + dist );
		}
	}


}
