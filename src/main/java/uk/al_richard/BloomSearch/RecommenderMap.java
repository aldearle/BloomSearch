package uk.al_richard.BloomSearch;

import uk.al_richard.BloomSearch.Util.OpenBitSet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *  A Class to map from reference points to the number_nns which have that reference point in their nearest neighbour set.
 */
public class RecommenderMap {

    private final Map<Integer, Bloom> bloom_map = new HashMap<>();       // Maps from pivot to a bloom filter of points for which that pivot is in the NN set
    private final double bloom_width_bits;                              // The width of the bloom filter
    private final int reference_objects_per_query;                      // The number of reference objects to use to select query solutions
    private final CircularHash hash;
    private final int size_of_balanced_representation;
    private final int no_referrers_per_object;                          // the number of references from the bloom filter to each pivot
    private final int hash_length_in_bits;

    private final List<Integer> dat;
    private final List<Integer> refs;


    private Followers followers;

    public RecommenderMap(List<Integer> refs, List<Integer> dat, Followers followers, double bloom_width_bits, int hash_length_in_bits, int hash_overlap, int num_hashes, int size_of_balanced_representation, int reference_objects_per_query, int no_referrers_per_object) {

        this.dat = dat;
        this.refs = refs;

        this.followers = followers;

        int total_size = refs.size() + dat.size();

        this.bloom_width_bits = bloom_width_bits;
        this.reference_objects_per_query = reference_objects_per_query;
        this.size_of_balanced_representation = size_of_balanced_representation;
        this.no_referrers_per_object = no_referrers_per_object;
        this.hash_length_in_bits = hash_length_in_bits;

        this.hash = new CircularHash( hash_length_in_bits,hash_overlap, num_hashes, size_of_balanced_representation);

        initialiseNNMap(bloom_width_bits,hash_length_in_bits,hash_overlap);
    }

    /**
     *
     * @param influenced_by - a set of influencers who influence query object
     * @return the matching users who share similar influences
     */
    public Set<Integer> search(List<Integer> influenced_by) {
        OpenBitSet bits = andMatches(influenced_by);    // these are the AND of the hashes of datums that for which the ref objects are NNs.
        List<Integer> matching_indices = Bloom.getSetBits(bits,bloom_width_bits);  // the indices into the linear bloom filter corresponding to set bits

        System.out.println( "Bits set in soln = " + matching_indices.size() + "(" +  ( matching_indices.size() * 100 / bits.size() ) +  "%)" );  // Debug/analysis

        // findIndicesDebug( bloom_map, matching_indices);


        Set<Integer> results = hash.reverseHashes(matching_indices);  // a set of numbers (inc false +ves) that could give rise to the indices
//        if( results.size() == 0 ) {
//            System.out.println("Did not manage to reverse any hashes");
//        } else {
//            for (int i : results) {
//                System.out.println("reversed = " + i + "\t" + pad(Integer.toBinaryString(i), size_of_balanced_representation) + " " + Util.check(pad(Integer.toBinaryString(i), size_of_balanced_representation), size_of_balanced_representation));
//            }
//        }

        Set<Integer> filtered = BalanceGen.filter(results, size_of_balanced_representation); // filter out the non bit balanced hashes
//        if( filtered.size() == 0 ) {
//            System.out.println("Did find any legal solutions after filtering");
//        } else {
//            for (int i : filtered) {
//                System.out.println("filtered = " + i + "\t" + pad(Integer.toBinaryString(i), size_of_balanced_representation) + " " + Util.check(pad(Integer.toBinaryString(i), size_of_balanced_representation), size_of_balanced_representation));
//            }
//        }
        return filtered;
    }

    /**
     * Check to see if matches are correct!
     * @param bloom_map
     * @param matching_indices
     */
    private void findIndicesDebug(Map<Integer, Bloom> bloom_map, List<Integer> matching_indices) {
        for( int match : matching_indices ) {
            for( Map.Entry<Integer,Bloom> entry : bloom_map.entrySet() ) {
                OpenBitSet bits = entry.getValue().getBits();
                if( bits.get(match) ) {
                    System.out.println( "Match for index " + match + " key = " + entry.getKey() );
                }

            }
        }
    }

    /**
     * @param influenced_by - a set of influencers who influence query object
     * @return a bloom filter which is the AND of all the bloom filters corresponding to the ref objects that match influenced_by
     */
    private OpenBitSet andMatches(List<Integer> influenced_by) {

        if( influenced_by.size() == 0 ) {
            throw new RuntimeException( "Cannot get iterator over pivots in andMatches" );
        } else if( influenced_by.size() == 1 ) {
            return bloom_map.get(influenced_by.get(0)).getBitsCopy();
        } else {
            OpenBitSet bits = bloom_map.get(influenced_by.get(0)).getBitsCopy();
            for( int i = 1; i < influenced_by.size(); i++ ) {
                OpenBitSet next_bits = bloom_map.get(influenced_by.get(i)).getBits();
                bits.and(next_bits);
            }
            return bits;
        }
    }

    private void initialiseNNMap(double bloom_width_bits, int hash_size_in_bits, int hash_overlap) {

        boolean first = true;

        for( int ro : refs ) {

            List<Integer> influenced_by = followers.getFollowers(ro);

            Bloom bloom = new Bloom( bloom_width_bits );

            for( int follower : influenced_by ) {
                List<Integer> hashes = hash.hash( follower );
                for( int hash : hashes ) {
                    bloom.addhash(hash);
                }
            }
             OpenBitSet bits = bloom.getBits();                                  // Debug/analysis
             List<Integer> set_bits = Bloom.getSetBits(bits,bloom_width_bits);  // Debug/analysis
             if( first ) {
                 System.out.println( "Bits set = " + set_bits.size() + "(" +  ( set_bits.size() * 100 / bits.size() ) +  "%)" );  // Debug/analysis
                 first = false;
             }

            //showDists(ro, ol);
            //showBloom( bloom );
            bloom_map.put(ro,bloom);

        }
    }
}
