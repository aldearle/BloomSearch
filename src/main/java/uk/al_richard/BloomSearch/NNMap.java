package uk.al_richard.BloomSearch;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import uk.al_richard.BloomSearch.Util.OpenBitSet;
import util.OrderedList;

import java.util.*;

/**
 *  A Class to map from reference points to the number_nns which have that reference point in their nearest neighbour set.
 */
public class NNMap {

    private final Map<Integer,CartesianPoint> refs = new HashMap<>();   // Maps could be indexed and map to bit balanced  ints
    private final Map<Integer,CartesianPoint> dat = new HashMap<>();    // Maps could be indexed and map to bit balanced  ints
    private final Map<Integer, Bloom> bloom_map = new HashMap<>();      // Maps from pivot to a bloom filter of points for which that pivot is in the NN set
    private final double bloom_width;                                   // The width of the bloom filter
    private final Metric<CartesianPoint> metric;                        // the metric to use
    private final int number_instantiation_nns;                         // The number of nns to use in the bloom_map
    private final int number_search_nns = 4;                           // The number of nns to use to select query solutions
    private final Hash hash;

    public NNMap(List<CartesianPoint> refs, List<CartesianPoint> dat, Metric<CartesianPoint> metric, int number_nns, double bloom_width, int hash_size_in_bits, int hash_overlap, int source_length) {

        int total_size = refs.size() + dat.size();
        Iterator<Integer> identifiers = BalanceGen.getRandomIterator(total_size);

        initialiseRefs(refs,identifiers);
        initialiseDat(dat,identifiers);

        this.number_instantiation_nns = number_nns;
        this.bloom_width = bloom_width;
        this.metric = metric;

        this.hash = new Hash( hash_size_in_bits,hash_overlap, source_length);

        initialiseNNMap(bloom_width,hash_size_in_bits,hash_overlap);
    }

    public Set<CartesianPoint> search(CartesianPoint query) {
        Set<CartesianPoint> results = new HashSet<>();
        Set<Integer> indices = getDataIndices(query);
        for( int data_index : indices ) {
            CartesianPoint point = dat.get(data_index);
            if( point != null ) {
                results.add( point );
            }
        }
        return results;
    }

    /*------------ private methods ------------*/

    /**
     *
     * @param query - a metric query to be performed
     * @return - the set of data indices corresponding to the hashes returned
     */
    private Set<Integer> getDataIndices(CartesianPoint query) {
        List<Integer> matching_hashes = getHashes( query ); // these are the AND of the hashes of datums that for which the pivots are NNs.

        return BalanceGen.filter( hash.reverseHashes( matching_hashes ) );
    }

    /**
     * @param query - a metric query to be performed
     * @return a set of hashes which are extracted from the bloom filter search result
     */
    private List<Integer> getHashes(CartesianPoint query) {
        List<Integer> result = new ArrayList<>();

        OpenBitSet bits = bitSearch(query);

        for (int i = bits.nextSetBit(0); i >= 0; i = bits.nextSetBit(i+1)) {
            result.add( i );
            if (i == bloom_width) {
                break;
            }
        }
        return result;
    }

    /**
     * @param query - a metric query to be performed
     * @return a bloom filter which is the AND of all the bloom filters corresponding to the pivots that are the nearest neighbours of query
     */
    private OpenBitSet bitSearch(CartesianPoint query) {

        List<Integer> closest_pivot_indices = findClosestPivotIndices(query);
        Iterator<Integer> iter = closest_pivot_indices.iterator();
        if( iter.hasNext() ) {
            OpenBitSet bits = bloom_map.get(iter.next()).getBitsCopy(); // pull out a copy of the first bitmap from the map entries of the closest pivots
            while (iter.hasNext()) {
                OpenBitSet next_bits = bloom_map.get(iter.next()).getBits();
                bits.and(next_bits);
            }
            return bits;
        } else {
            throw new RuntimeException( "Cannot get iterator over pivots in bitSearch" );
        }
    }
    
    private List<Integer> findClosestPivotIndices(CartesianPoint query) {
        OrderedList<Integer, Double> ol = new OrderedList<>(number_search_nns); // a list of the closest pivots to the query.
        for( int i : refs.keySet() ) {
            CartesianPoint ref = refs.get(i);
            ol.add( i,metric.distance(query,ref ) ); // remember the closest pivots to the query,
        }
        return ol.getList();
    }
    

    private void initialiseNNMap(double bloom_width, int hash_size_in_bits, int hash_overlap) {

        for( int ro_index : refs.keySet() ) {
            CartesianPoint ref = refs.get(ro_index);

            OrderedList<Integer, Double> ol = new OrderedList<>(number_instantiation_nns);
            for( int data_index : dat.keySet() ) {
                CartesianPoint point = dat.get(data_index);
                double distance = metric.distance(ref, point);
                ol.add(data_index,distance);
            }

            // We now have an ordered list of NNs
            // Next hash each of these and add them to a Bloom filter

            Bloom bloom = new Bloom( bloom_width );

            for( int data_id : ol.getList() ) {
                List<Integer> hashes = hash.hash( data_id );
                for( int hash : hashes ) {
                    bloom.addhash(hash);
                }
            }

            showDists(ro_index, ol);
            showBloom( bloom );
            bloom_map.put(ro_index,bloom);

        }
    }

    private void initialiseRefs(List<CartesianPoint> supplied_refs, Iterator<Integer> identifiers) {
        initialiseRawData( refs,supplied_refs,identifiers );
    }

    private void initialiseDat(List<CartesianPoint> supplied_dat, Iterator<Integer> identifiers) {
        initialiseRawData(dat, supplied_dat, identifiers);
    }

    private void initialiseRawData(Map<Integer, CartesianPoint> map, List<CartesianPoint> points, Iterator<Integer> identifiers) {

        for( CartesianPoint p : points ) {
            if( identifiers.hasNext() ) {
                map.put(identifiers.next(), p);
            } else {
                throw new RuntimeException( "Cannot get balanced identifier for data/pivot" );
            }
        }
    }

    private void showBloom(Bloom bloom) {
        System.out.println( bloom.toString() );
    }

    private void showDists(int ro_index, OrderedList<Integer, Double> ol) {
        System.out.print( "pivot dists to " + ro_index + " : " ) ;
        for( double d : ol.getComparators() ) {
            System.out.print( d + " " );
        }
        System.out.println();
        System.out.print( "points closest to " + ro_index + " : " ) ;
        for( int data_index : ol.getList() ) {
            System.out.print( data_index + " " );
        }
        System.out.println();
    }
}
