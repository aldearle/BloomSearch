package uk.al_richard.BloomSearch;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import util.OrderedList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *  A Class to map from reference points to the number_nns which have that reference point in their nearest neighbour set.
 */
public class NNMap {

    private final Map<Integer,CartesianPoint> refs = new HashMap<>(); // Maps could be indexed and map to bit balanced  ints
    private final Map<Integer,CartesianPoint> dat = new HashMap<>(); // Maps could be indexed and map to bit balanced  ints
    private final int number_nns;
    private final Metric<CartesianPoint> metric;

    private final Map<Integer, Bloom> map = new HashMap<>();

    public NNMap(List<CartesianPoint> refs, List<CartesianPoint> dat, Metric<CartesianPoint> metric, int number_nns, double bloom_width, int hash_size_in_bits, int hash_overlap) {

        int total_size = refs.size() + dat.size();
        Iterator<Integer> identifiers = BalanceGen.getRandomIterator(total_size);

        initialiseRefs(refs,identifiers);
        initialiseDat(dat,identifiers);

        this.number_nns = number_nns;
        this.metric = metric;

        initialiseNNMap(bloom_width,hash_size_in_bits,hash_overlap);
    }

    private void initialiseNNMap(double bloom_width, int hash_size_in_bits, int hash_overlap) {

        for( int ro_index : refs.keySet() ) {
            CartesianPoint ref = refs.get(ro_index);

            OrderedList<Integer, Double> ol = new OrderedList<>(number_nns);
            for( int data_index : dat.keySet() ) {
                CartesianPoint point = dat.get(data_index);
                double distance = metric.distance(ref, point);
                ol.add(data_index,distance);
            }

            // We now have an ordered list of NNs
            // Next hash each of these and add them to a Bloom filter

            Bloom bloom = new Bloom( bloom_width );

            for( int data_id : ol.getList() ) {
                List<Integer> hashes = Hash.hash( data_id,hash_size_in_bits,hash_overlap );
                for( int hash : hashes ) {
                    bloom.addhash(hash);
                }
            }

            showDists(ro_index, ol);
            showBloom( bloom );
            map.put(ro_index,bloom);

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
