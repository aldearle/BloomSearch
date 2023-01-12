package uk.al_richard.BloomSearch;

import java.util.*;

import static uk.al_richard.BloomSearch.Hash.pad;

/**
 * A driver program to test the BloomSearch algorithm
 * This version works with an abstract space of products and users
 * Another version of this code BloomMetricSearch works withconventional Metrics and Euclidean data
 */
public class BloomProductSearch {

	public static final int TEN = 10;
	public static final int ONE_HUNDRED = 100;
	public static final int FIVE_HUNDRED = 5 * ONE_HUNDRED;
	public static final int ONE_THOUSAND = 1000;
	public static final int TEN_THOUSAND = TEN * ONE_THOUSAND;
	public static final int FIFTY_THOUSAND = 5 * TEN_THOUSAND;
	public static final int HUNDRED_THOUSAND = ONE_HUNDRED * ONE_THOUSAND;
	public static final int ONE_MILLION = ONE_THOUSAND * ONE_THOUSAND;
	public static final int ONE_POINT_25_MILLION = 125 * TEN_THOUSAND;
	public static final int TEN_MILLION = 10 * ONE_MILLION;

	public static void main(String[] args) throws Exception {

		System.out.println("Date/time\t" + new Date());

		int no_of_ref_points = 4092; 							// from paper - https://www.overleaf.com/project/637fcaab8a3088b3ff45c1f0
		int object_population_size = ONE_MILLION;	// NOT FROM			// from paper - https://www.overleaf.com/project/637fcaab8a3088b3ff45c1f0

		Iterator<Integer> generator = BalanceGen.getRandomIterator(object_population_size);

		List<Integer> refs = carveSlice( generator,no_of_ref_points ); // carve off a small number of refs from the data. These are not treated as data following this slice.
		List<Integer> dat = carveSlice( generator,object_population_size ); // this is used as data from this point onwards

		int reference_objects_per_query = 5;

		int objects_per_bloom_filter = FIFTY_THOUSAND;

		int hash_length = 20; 										// from paper - https://www.overleaf.com/project/637fcaab8a3088b3ff45c1f0
		int hash_overlap = 1; // longer the overlap the less likelihood to get false +ves?
		int size_of_balanced_representation = BalanceGen.numberBitsInBalancedRep(object_population_size);

		int num_hashes = 6; // also worked with 8

		int no_referrers_per_object = 512 ;				// from paper - https://www.overleaf.com/project/637fcaab8a3088b3ff45c1f0 (top_k_references_per_object)
														// number of references from data to each reference object

		double bloom_width_bits = Math.pow(2,hash_length); // size of each bloom filter in bits

		System.out.println( "Object population size =\t" + dat.size() );
		System.out.println( "Bits needed to encode population =\t" + size_of_balanced_representation );
		System.out.println( "No of ref objects =\t" + refs.size() );
		System.out.println( "No references per object =\t" + no_referrers_per_object );
		System.out.println( "Objects per bloom filter =\t" + objects_per_bloom_filter );
		System.out.println( "Bits per bloom filter =\t" + bloom_width_bits );
		System.out.println( "Hash length=\t" + hash_length );
		System.out.println( "Num Hashes=\t" + num_hashes );
		System.out.println( "No of ref objects per query =\t" + reference_objects_per_query );

		System.out.println( "Hash overlap=\t" + hash_overlap );
		System.out.println( "Number of hashes per object: " + new Hash(hash_length, hash_overlap, size_of_balanced_representation).hash( 1432523 ).size() );

		Followers followers = new Followers(refs, dat, objects_per_bloom_filter);

		System.out.println( "Creating Recommender Map\t" + new Date() );

		RecommenderMap map = new RecommenderMap( refs, dat, followers, bloom_width_bits, hash_length, hash_overlap, num_hashes, size_of_balanced_representation, reference_objects_per_query, no_referrers_per_object );

		System.out.println( "Performing Query\t" + new Date() );

		List<Integer> query = followers.createQueryInfluencers(reference_objects_per_query);

		System.out.println( "Query " + query.size() + " influencers of query : " );
		for( Integer influencer : query ) {
			System.out.println( influencer );
		}

		Set<Integer> results = map.search( query );

		System.out.println( "Got " + results.size() + " results in final query solution" );
		for( Integer result : results ) {
			System.out.println( result + " : " + pad( Integer.toBinaryString( result ),size_of_balanced_representation)  + " : " );
		}
		followers.checkResults( results, query );
	}


	private static List<Integer> carveSlice(Iterator<Integer> dat, int no_of_ref_points) {
		List<Integer> result = new ArrayList<>();
		while( dat.hasNext() && result.size() != no_of_ref_points ) {
			result.add( dat.next() );
		}
		return result;
	}

	public static int bitsRequiredToEncode( int num ) {
		int count = 0;
		while (num > 0) {
			count++;
			num = num >> 1;
		}
		return count;
	}

}
