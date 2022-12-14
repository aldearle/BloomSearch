package uk.al_richard.BloomSearch;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import util.OrderedList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NNMap {

    private final Map<Integer,CartesianPoint> refs = new HashMap<>();
    private final Map<Integer,CartesianPoint> dat = new HashMap<>();
    private final int number_nns;
    private final Metric<CartesianPoint> metric;

    private Map<Integer, OrderedList<Integer, Double>> map = new HashMap<Integer, OrderedList<Integer, Double>>();

    public NNMap(List<CartesianPoint> refs, List<CartesianPoint> dat, Metric<CartesianPoint> metric, int number_nns) {

        initialiseRefs(refs);
        initialiseDat(dat);
        this.number_nns = number_nns;
        this.metric = metric;

        initialiseNNMap();
    }

    private void initialiseRefs(List<CartesianPoint> supplied_refs) {
        initialiseRawData( refs,supplied_refs );
    }

    private void initialiseDat(List<CartesianPoint> supplied_dat) {
        initialiseRawData(dat, supplied_dat);
    }

    private void initialiseRawData(Map<Integer, CartesianPoint> map, List<CartesianPoint> points) {
        int i = 1;
        for( CartesianPoint p : points ) {
            map.put( i++, p );
        }
    }

    private void initialiseNNMap() {

        for( int ro_index : refs.keySet() ) {
            CartesianPoint ref = refs.get(ro_index);

            OrderedList<Integer, Double> ol = new OrderedList<>(number_nns);
            for( int data_index : dat.keySet() ) {
                CartesianPoint point = dat.get(data_index);
                double distance = metric.distance(ref, point);
                ol.add(data_index,distance);
            }

            map.put(ro_index,ol);
            showDists(ro_index, ol);
        }
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
