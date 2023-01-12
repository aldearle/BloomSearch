package uk.al_richard.BloomSearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class splits a number into n hashes of m bits with a p bit overlap.
 */
public class CircularHash {

    private final int SOLN_BITS;
    private final int OVERLAPPING_BITS;
    private final int NON_OVERLAPPING_BITS;

    private final int hash_length;
    private final int overlap;
    private final int num_bits_in_input;
    private final int num_hashes;

    /**
     * @param hash_length - how long the hash is required to be
     * @param overlap - each hash is overlapped by this amount
     * @param num_hashes - the number of hashes needed
     * @param num_bits_in_input - the length of the integers being hashed.
     */
    public CircularHash(int hash_length, int overlap, int num_hashes, int num_bits_in_input) {
        this.hash_length = hash_length;
        this.overlap = overlap;
        this.num_hashes = num_hashes;
        this.num_bits_in_input = num_bits_in_input;

        int non_overlap_size = hash_length - overlap;
        this.OVERLAPPING_BITS = bits(overlap);
        this.NON_OVERLAPPING_BITS = bits(non_overlap_size);
        this.SOLN_BITS = bits(num_bits_in_input);

        if( num_hashes * overlap > num_bits_in_input ) {
            throw new RuntimeException( "Too many hashes requested" );
        }
    }

    /**
     *
     * @param to_split - a number that is to be hashed into binary numbers each hash_length bits in length
     * @return a list of integers containing the hashes
     */
    public List<Integer> hash( int to_split ) {
        List<Integer> result = new ArrayList<>();

        if (num_bits_in_input < hash_length) {
            throw new RuntimeException("Not enough bits with which to hash");
        }

        for( int count = 0; count < num_hashes; count++ ) {
            int index = count * overlap;
            if( index + hash_length <= num_bits_in_input ) { // we can take the hash in a single grab
                int val = to_split & bits(hash_length);
                to_split = rotateRight(to_split,overlap);
                result.add(val);
            } else { // need to take two bit slices and combine them together
                int rhs_remainder_size = num_bits_in_input - index;         // number of bits to select from right end of int
                int lhs_remainder_size = hash_length-rhs_remainder_size;    // number of bits to select from left end of int

                int rhs_remainder = to_split & bits(rhs_remainder_size);    // the low bits from the rhs of the integer (right of index)
                int lhs_remainder = ( to_split >> (num_bits_in_input-lhs_remainder_size) ) & bits(lhs_remainder_size); // will become high bits from left of index

                to_split = rotateRight(to_split,overlap);

                result.add( rhs_remainder | lhs_remainder <<  rhs_remainder_size );
            }
        }
        return result;
    }

    /* utility methods */

    private int rotateRight( int number, int amount ) {
        int bottom_bits = number & bits(amount);
        number = number >> amount;
        return number | (bottom_bits << (num_bits_in_input-amount));
    }

    /**
     *
     * @param length - the number of bits that we require to be set i.e. 4 will produce 0b1111
     * @return and integer with exactly length bits set
     */
    private static int bits(int length) {
        int result = 0;
        for( int i = 0; i < length; i++ ) {
            result = result | (1<<i);
        }
        return result;
    }

    /* below here testing */

    /**
     * @param binary_string - a string (of 0s and 1s) for left length padding
     * @param required_length - the length of the result string
     * @return a string that is left padded to be of required_length digits
     */
    public static String pad(String binary_string, int required_length) {
        if( binary_string.length() > required_length ) {
            throw new RuntimeException( "String: " + binary_string + " has length " + binary_string.length() + " greater than " + required_length);
        }
        String result = binary_string;
        int num_zeros = required_length - binary_string.length();
        for( int i = 0; i < num_zeros; i++ ) {
            result = "0" + result;
        }
        return result;
    }

    /**
     * @param hashes - a set of hashes
     * @return the set of possible records that created the hashes (including false positives).
     */
    public Set<Integer> reverseHashes(List<Integer> hashes) {

        Set<Integer> potential_solns = new TreeSet<>();

        // 1. Put the hashes into potential_solns

        potential_solns.addAll(hashes);

        // repeat num_hashes -1 times: try and add the hashes to the end of the strings in potential_solns

        for (int unused_count = 0; unused_count < num_hashes - 1; unused_count++) {
            Set<Integer> new_potential_solutions = new TreeSet<>(); // potential_solns extended with the hashes if they match

            for (int potential_soln : potential_solns) { // try and see if we can extend the solutions with each of the hashes

                for (int hash : hashes) { // try and extend potential_soln with each hash
                    if (match(tail(potential_soln), head(hash))) {
                        new_potential_solutions.add( extend(potential_soln,hash) );
                    }
                    // if no matches don't do anything - the solution set will be overwritten with the new set and this potential solution will not be included
                }
            }
            potential_solns = new_potential_solutions;
        }
        return trimToLength( potential_solns );
    }

    private Set<Integer> trimToLength(Set<Integer> potential_solns) {
        Set<Integer> result = new TreeSet<>();
        for( int i : potential_solns ) {
            result.add( i & SOLN_BITS );
        }
        return result;
    }

    private int extend(int potential_soln, int hash) {
        int last_bits_hash = hash & OVERLAPPING_BITS;
        return (potential_soln << overlap) | last_bits_hash;
    }

    private int head(int hash) {
        return (hash >> overlap) & NON_OVERLAPPING_BITS; // first n bits of the hash - knock out the overlap and mask
    }

    private int tail(int potential_soln) {
        return potential_soln & NON_OVERLAPPING_BITS; // the non overlapping bits from the solution
    }

    private static boolean match(int start_of_hash, int end_of_extendand ) {
            return ( start_of_hash ^ end_of_extendand ) == 0;
    }

    /**
     * Main class just for testing
     * @param args - unused in this code
     */
    public static void main(String[] args) {

        int number = 0b101001011010111100011000; // 24 bits

        int overlap = 1;
        int hash_length = 20;
        int source_length = 24; // number of bits
        int number_hashes = 8;

        System.out.println("Number: " + pad( Integer.toBinaryString(number),24) );
        System.out.println("Overlap: " + overlap);
        System.out.println("hash_length: " + hash_length);
        System.out.println("source_length: " + source_length);
        System.out.println("Number hashes: " + number_hashes);

        CircularHash hash = new CircularHash(hash_length, overlap, number_hashes, source_length);
        List<Integer> splits = hash.hash(number);
//        for (int i : splits) {
//            System.out.println("hash:\t" + i + " " +  pad(Integer.toBinaryString(i), hash_length));
//        }

        Set<Integer> reversed = hash.reverseHashes(splits);
        for (int i : reversed) {
            System.out.println("reversed:\t" + i + " " +  pad(Integer.toBinaryString(i), source_length));
        }

        Set<Integer> recovered = BalanceGen.filter( reversed, source_length );
        int count = 1;
        for (int i : recovered) {
            System.out.println("recovered: " + count++ + "\t" + pad(Integer.toBinaryString(i), source_length));
        }

    }

}
