package uk.al_richard.BloomSearch.Util;

import uk.al_richard.BloomSearch.Bloom;
import util.OrderedList;

import java.util.Collection;

import static uk.al_richard.BloomSearch.Hash.pad;

public class Util {
    public static void  checkSubString(String to_check, Collection<Integer> hashed, int hash_length_in_bits) {
        boolean error = false;
        for (int hash : hashed) {
            String hash_as_string = pad(Integer.toBinaryString(hash), hash_length_in_bits);
            if( ! to_check.contains(hash_as_string) ) {
                error = true;
                System.out.println( hash_as_string + " not a substring of " + to_check );
            }
        }
        if( ! error ) {
            System.out.println( "Substrings of " + to_check + " ok" );
        }
    }

    public static String check(String digits, int expected_length) {
        if( digits.length() != expected_length ) {
            return "String " + digits + " is not of expected length: " + expected_length;
        }
        int zeros = 0;
        int ones = 0;
        for( int i = 0; i < digits.length(); i++ ) {
            String next = digits.substring( i,i+1 );
            if( next.equals("0") ) zeros++; else
            if( next.equals("1") ) ones++; else {
                return "Found illegal character:" + next;
            }
        }
        return "Zeros = " + zeros + " ones = " + ones + " balanced = " + ( zeros == ones );
    }

    public static void showBloom(Bloom bloom) {
        System.out.println( bloom.toString() );
    }

    public static void showDists(int ro_index, OrderedList<Integer, Double> ol) {
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
