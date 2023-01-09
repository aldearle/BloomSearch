package uk.al_richard.BloomSearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class splits a number into n hashes of m bits with a p bit overlap.
 */
public class Hash {

    private final int hash_length;
    private final int overlap;
    private final int num_bits_in_balanced_rep;

    /**
     * @param hash_length - how long the hash is required to be
     * @param overlap - each hash is overlapped by this amount
     * @param num_bits_in_balanced_rep - the length of the integers being hashed.
     */
    public Hash(int hash_length, int overlap, int num_bits_in_balanced_rep) {
        this.hash_length = hash_length;
        this.overlap = overlap;
        this.num_bits_in_balanced_rep = num_bits_in_balanced_rep;
    }

    /**
     *
     * @param to_split - a number that is to be hashed into binary numbers each hash_length bits in length
     * @return a list of integers containing the hashes
     */
    public List<Integer> hash( int to_split ) {
        List<Integer> result = new ArrayList<>();

        if (num_bits_in_balanced_rep < hash_length) {
            throw new RuntimeException("Not enough bits with which to hash");
        }

        int num_hashes_created = ( num_bits_in_balanced_rep - hash_length ) / overlap;

        for( int index = 0; index <= num_hashes_created; index++ ) {
            int val = (to_split & bits(hash_length));
            to_split = to_split >> overlap;
            result.add(val);
        }
        return result;
    }

    /* utility methods */

    /**
     *
     * @param length - the number of bits that we require to be set i.e. 4 will produce 0b1111
     * @return and integer with exactly length bits set
     */
    private static int bits(int length) {
        int result = 1;
        for( int i = 1; i < length; i++ ) {
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
        // The amount of debug in this code reflects the difficulty in writing
        // I doubt this code is optimal.

        Set<Integer> result = new TreeSet<>();

        int non_overlap_size = hash_length - overlap;
        int NON_OVERLAPPING_BITS = bits(non_overlap_size);
        int OVERLAPPING_BITS = bits(overlap);

        for (int initial_index = 0; initial_index < hashes.size(); initial_index++) {
            Set<Integer> new_list = new TreeSet<>();
            int initial_hash = hashes.get(initial_index);
            new_list.add(initial_hash);  // each initial will be of length hash_length bits.

            for (int index = non_overlap_size; index < num_bits_in_balanced_rep - overlap; index += overlap) { // move up the entries in result, overlap bits at a time.

                for (int hash_index = 0; hash_index < hashes.size(); hash_index++) {
                    if (initial_index != hash_index) {
                        int hash = hashes.get(hash_index);
                        // for each hash try and match the next bits from everything in result
                        for (int extendand : result) {
                            // match the hash_length - overlap bits from extendand and hash
                            // if they match add onto the end of extendand and add to new set
                            int start_of_hash = (hash >> overlap) & NON_OVERLAPPING_BITS; // first n bits of the hash - knock out the overlap and mask=
                            int end_of_extendand = extendand & NON_OVERLAPPING_BITS;
//                    System.out.println("hash: " + pad(Integer.toBinaryString(hash), hash_length));
//                    System.out.println("first " + non_overlap_size + " bits hash: " + pad(Integer.toBinaryString(start_of_hash), non_overlap_size));
                            if (startOfHashMatchesEndOfExtendand(start_of_hash, end_of_extendand)) {
                                // add the non-matched bits from last_bits_hash to the end of extendand
                                int last_bits_hash = hash & OVERLAPPING_BITS;

//                                System.out.println("MATCH: " + pad(Integer.toBinaryString(start_of_hash), non_overlap_size) + " and " +
//                                        pad(Integer.toBinaryString(end_of_extendand), non_overlap_size));
//                                System.out.println("hash: " + pad(Integer.toBinaryString(hash), hash_length));
//                                System.out.println("index: " + index);
//                                System.out.println("extend: " + Integer.toBinaryString(extendand));
//                                System.out.println("last " + overlap + " bits hash: " + pad(Integer.toBinaryString(last_bits_hash), overlap));
//                                System.out.println("shift extend: " + Integer.toBinaryString((extendand << overlap)));

                                int candidate = (extendand << overlap) | last_bits_hash;
                                new_list.add(candidate);
                            }

                        }
                        // now add all the new list to the result.
                        result.addAll(new_list);
                    }
                }
            }
        }
        return result;
    }

    private static boolean startOfHashMatchesEndOfExtendand( int start_of_hash, int end_of_extendand ) {
        return ( start_of_hash ^ end_of_extendand ) == 0;
    }

    /**
     * Main class just for testing
     * @param args - unused in this code
     */
    public static void main(String[] args) {

        int number = 0b101001011010111100011000; // 24 bits

        int overlap = 2;
        int hash_length = 10;
        int source_length = 24; // number of bits

        System.out.println("Number: " + pad( Integer.toBinaryString(number),24) );
        System.out.println("Overlap: " + overlap);
        System.out.println("hash_length: " + hash_length);
        System.out.println("source_length: " + source_length);

        Hash hash = new Hash(hash_length, overlap,source_length);
        List<Integer> splits = hash.hash(number);
        for (int i : splits) {
            System.out.println("hash:\t" + i + " " +  pad(Integer.toBinaryString(i), hash_length));
        }

        Set<Integer> recovered = BalanceGen.filter( hash.reverseHashes( splits ), source_length);
        int count = 1;
        for (int i : recovered) {
            System.out.println("recovered: " + count++ + "\t" + pad(Integer.toBinaryString(i), source_length));
        }

    }

}
