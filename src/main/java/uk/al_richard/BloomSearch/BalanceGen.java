package uk.al_richard.BloomSearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * This class creates identifiers (ints) which are comprised of even numbers of ones and zeros.
 * The size of the strings are determined by how many numbers are required - higher makes the strings longer (obviously).
 */
public class BalanceGen {

    // Sizes used for testing only
    public static final int ONE_THOUSAND = 1000;
    public static final int ONE_MILLION = ONE_THOUSAND * ONE_THOUSAND;

    public BalanceGen() {}

    /**
     *
     * @param number_unique_bit_balanced_numbers_required - the number of identifiers we need (expected to be governed by some dataset size).
     * @return an iterator which is guaranteed to be able to generate at least number_unique_bit_balanced_numbers_required of RANDOM unique bit balanced integers.
     */
    public static Iterator<Integer> getRandomIterator( int number_unique_bit_balanced_numbers_required ) {
        List<Integer> list = new ArrayList<>();

        Iterator<Integer> iter = getIterator(number_unique_bit_balanced_numbers_required);
        while( iter.hasNext() ) {
            list.add( iter.next() );
        }

        Collections.shuffle(list);
        return list.listIterator();
    }

    /**
     * @param number_unique_bit_balanced_numbers_required - the number of identifiers we need (expected to be governed by some dataset size).
     * @return an iterator which is guaranteed to be able to generate at least number_unique_bit_balanced_numbers_required of unique bit balanced integers.
     */
    public static Iterator<Integer> getIterator( int number_unique_bit_balanced_numbers_required ) {

        final int bits_required = findBits( number_unique_bit_balanced_numbers_required );
        final int bits = 1 << bits_required;

        return new BalancedBitIterator(bits, bits_required);
    }

    /**
     * @param number_unique_bit_balanced_numbers_required - the number_unique_bit_balanced_numbers_required of identifiers this code needs to be able to generate
     * @return true if the number of ones and zeros in the representation are equal.
     */
    private static boolean bitsAreBalanced(int number_unique_bit_balanced_numbers_required)
    {
        int count_zeros = 0;
        int count_ones = 0;

        for (int bit_index = 31; bit_index >= 0; bit_index--) {                 // assumes 32 bit ints
            if( ((number_unique_bit_balanced_numbers_required >> bit_index) & 1) == 1 ) {
                count_ones++;
            } else if(count_ones > 0) {
                count_zeros++;
            }
        }
        return count_zeros == count_ones;
    }

    /**
     * Used in initialisation
     * @param number_unique_bit_balanced_numbers_required - the number of identifiers this code needs to be able to generate
     * @return the number of bits we require to make all the generated strings of the same length
     */
    private static int findBits(int number_unique_bit_balanced_numbers_required) {
        for( int i = 1; i < 32; i++ ) {
            if( (1 << i) > number_unique_bit_balanced_numbers_required) {
                System.out.println( i + 4 );
                return i + 4; // +4 heuristic!
            }
        }
        throw new RuntimeException( "Cannot satisfy requirement - number of results required too high");
    }

    /**
     * An instance of this class is returned by getIterator()
     * The contract is that hasNext() must be called before next()
     */
    private static class BalancedBitIterator implements Iterator<Integer> {
        private final int bits_required;
        int seed;
        int result;
        boolean consumed = false;

        public BalancedBitIterator(int bits, int bits_required) {
            this.bits_required = bits_required;
            seed = 1;
            result = 0;
            // move the seed up to the required number of bits.
            for (int number = seed; number <= bits; number++) {
                int len = Integer.toBinaryString(number).length();
                if( len >= bits_required ) {
                    seed = number;
                    break;
                }
            }
        //    System.out.println( "Initialised seed = " + seed + " len " + Integer.toBinaryString(seed).length());
        }

        @Override
        public boolean hasNext() {

            while( true ) {
                if ( bitsAreBalanced( seed ) ) {
                    if ( Integer.toBinaryString(seed).length() == bits_required ) { // if we have not run out of bits (normal case).
                        consumed = false;
                        return true;
                    } else {                //  We have run out of bits - all strings must be of the same length
                        consumed = false;
                        return false;
                    }
                }
                seed = seed + 1;
            }
        }

        @Override
        public Integer next() {
            if( consumed ) {
                throw new RuntimeException( "hasNext() has not been called before requesting next()");
            }
            int result = seed;
            seed = seed + 1;  // ensure we don't use this seed again.
            consumed = true;
            return result;
        }
    }

    /**
     * This main is for demo purposes only
     * @param args - this is unused in this context
     */
    public static void main(String[] args) {
        BalanceGen bg = new BalanceGen();
        Iterator<Integer> iter = bg.getIterator(ONE_MILLION);

        int count = 1;

        while (iter.hasNext()) {
            int number = iter.next();
            // number = iter.next(); // should get an exception.
            System.out.println(count++ + " > " + number + " | " + Integer.toBinaryString(number) + " | " + Integer.toBinaryString(number).length());
        }
    }
}
