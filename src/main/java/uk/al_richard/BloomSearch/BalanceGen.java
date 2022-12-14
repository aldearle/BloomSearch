package uk.al_richard.BloomSearch;

import java.util.Iterator;

public class BalanceGen {

    public static final int ONETHOUSAND = 1000;
    public static final int ONEMILLION = ONETHOUSAND * ONETHOUSAND;

    public BalanceGen() {}

    public static boolean bitsAreBalanced(int number)
    {
        int count_zeros = 0;
        int count_ones = 0;

        for (int bit_index = 31; bit_index >= 0; bit_index--) {                 // assumes 32 bit ints
            if( ((number >> bit_index) & 1) == 1 ) {
                count_ones++;
            } else if(count_ones > 0) {
                count_zeros++;
            }
        }
        return count_zeros == count_ones;
    }

    public static int findBits(int number_unique_bitbalanced_numbers_required) {
        for( int i = 1; i < 32; i++ ) {
            if( (1 << i) > number_unique_bitbalanced_numbers_required) {
                System.out.println( i + 4 );
                return i + 4; // +4 heuristic!
            }
        }
        throw new RuntimeException( "Cannot satisfy requirement - number of results required too high");
    }

    public Iterator<Integer> getIterator( int number_unique_bitbalanced_numbers_required ) {

        final int bits_required = findBits( number_unique_bitbalanced_numbers_required );
        final int bits = 1 << bits_required;

        return new BalancedBitIterator(bits, bits_required);
    }


    private static class BalancedBitIterator implements Iterator<Integer> {
        private final int bits;
        private final int bits_required;
        int seed;
        int result;

        public BalancedBitIterator(int bits, int bits_required) {
            this.bits = bits;
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
            System.out.println( "Initialised seed = " + seed + " len " + Integer.toBinaryString(seed).length());
        }

        @Override
        public boolean hasNext() {

            while( true ) {
                if ( bitsAreBalanced( seed ) ) {
                    if ( Integer.toBinaryString(seed).length() == bits_required ) { // run out of bits - all strings same length
                        return true;
                    } else {
                        return false;
                    }
                }
                seed = seed + 1;
            }
        }

        @Override
        public Integer next() {
            int result = seed;
            seed = seed + 1;  // ensure we don't use this seed again.
            return result;
        }
    }

    public static void main(String[] args) {
        BalanceGen bg = new BalanceGen();
        Iterator<Integer> iter = bg.getIterator(ONEMILLION);

        int count = 1;

        while (iter.hasNext()) {
            int number = iter.next();
            System.out.println(count++ + " > " + number + " | " + Integer.toBinaryString(number) + " | " + Integer.toBinaryString(number).length());
        }
    }
}
