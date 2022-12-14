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
 * This version works with conventional Metrics and Euclidean data
 * Another version of this code BloomProductSearch works in a more abstract space of products and users
 */
public class BloomMetricSearch {

	public static void main(String[] args) throws Exception {

		Context context = Context.euc20;

		System.out.println("Date/time\t" + new Date());

		int no_of_ref_points = 500; 							// small for now
		TestContext tc = new TestContext(context);
		int query_size =  1000;
		tc.setSizes(query_size, no_of_ref_points);
		List<CartesianPoint> dat = tc.getData();
		dat = dat.subList(0,100000); 							// small for now
		List<CartesianPoint> refs = tc.getRefPoints();
		// double threshold = tc.getThresholds()[0];
		List<CartesianPoint> queries = tc.getQueries();
		int num_nn_in_bloom = 100;

		int hash_size_in_bits = 10;
		int hash_overlap = 2;
		int bits_in_data_encoding = 24;

		int number_of_nn_pivots_used_in_search = 4;

		double bloom_width = Math.pow(2,hash_size_in_bits);

		Metric<CartesianPoint> metric = new Euclidean<>();

		System.out.println( "data size =\t" + dat.size() );
		System.out.println( "refs size =\t" + refs.size() );
		System.out.println( "query size =\t" + query_size );
		System.out.println( "NN in bloom filter =\t" + num_nn_in_bloom );
		System.out.println( "Metric =\t" + metric.getMetricName() );
		System.out.println( "Hash size=\t" + hash_size_in_bits );
		System.out.println( "Hash overlap=\t" + hash_overlap );
		System.out.println( "Bloom width =\t" + Math.round(bloom_width ) );
		System.out.println( "Source length =\t" + bits_in_data_encoding );
		System.out.println( "Number of hashes per hash: " + new Hash(hash_size_in_bits, hash_overlap, bits_in_data_encoding).hash( 1432523 ).size() );
		System.out.println( "Number of NN pivots matched in search: " + number_of_nn_pivots_used_in_search );


		MetricNNMap map = new MetricNNMap( refs, dat, metric, num_nn_in_bloom, bloom_width, hash_size_in_bits, hash_overlap, bits_in_data_encoding, number_of_nn_pivots_used_in_search );

		CartesianPoint query = queries.get(0);

		Set<CartesianPoint> results = map.search( query );
		int count = 1;
		if( results.size() == 0 ) {
			System.out.println( "No results found");
		} else {
			System.out.println( "Results:" );
			for (CartesianPoint point : results) {
				System.out.println("result " + count++ + " distance = " + metric.distance(query, point));
			}
			bruteForce(query,dat,metric);
		}

	}

	private static void bruteForce(CartesianPoint query, List<CartesianPoint> dat, Metric<CartesianPoint> metric) {
		OrderedList<CartesianPoint, Double> ol = new OrderedList<>(5);
		double sum = 0.0d;
		double max = Double.MIN_VALUE;
		for( CartesianPoint point : dat ) {
			double dist = metric.distance(query,point);
			sum = sum + dist;
			if( dist > max ) {
				max = dist;
			}
			ol.add( point, dist );
		}
		System.out.println("Max distance to query = " + max );
		System.out.println("Mean distance to query = " + ( sum / dat.size() ) );
		System.out.println( "5 closest distances to query are:");
		int count = 1;
		for( Double dist : ol.getComparators() ) {
			System.out.println( "True " + count++ + " distance = " + dist );
		}
	}


}
