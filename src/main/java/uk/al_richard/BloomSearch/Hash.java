package uk.al_richard.BloomSearch;

import java.util.ArrayList;
import java.util.List;

/**
 * This class splits a number into n hashes of m bits with a p bit overlap.
 */
public class Hash {

    /**
     *
     * @param to_split - a number that is to be hashed into binary numbers each hash_length bits in length
     * @param hash_length - how long the hash is required to be
     * @param overlap - each hash is overlapped by this amount
     *                  e.g. a number 01111010 hashed with a hash length of 4 and overlap 2 will create the list 1010, 1110, 0111
     * @return a list of integers containing the hashes
     */
    public static List<Integer> hash(int to_split, int hash_length, int overlap) {
        List<Integer> result = new ArrayList<>();

        int to_split_length = Integer.toBinaryString(to_split).length();
        if (to_split_length < hash_length) {
            throw new RuntimeException("Not enough bits with which to hash");
        }

        int num_hashes_created = ( to_split_length - hash_length ) / overlap;

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
    private static String pad(String binary_string, int required_length) {
        String result = binary_string;
        int num_zeros = required_length - binary_string.length();
        for( int i = 0; i < num_zeros; i++ ) {
            result = "0" + result;
        }
        return result;
    }

    /**
     * Main class just for testing
     * @param args - unused in this code
     */
    public static void main(String[] args) {

        int number = 0b010100001010111100011000;

        int overlap = 2;
        int hash_length = 8;

        System.out.println("Number: " + Integer.toBinaryString(number));
        System.out.println("Overlap: " + overlap);
        System.out.println("hash_length: " + hash_length);

        List<Integer> splits = hash(number, hash_length, overlap);
        for (int i : splits) {
            System.out.println("next:   " + pad(Integer.toBinaryString(i), hash_length));
        }
    }
}
