package uk.al_richard.BloomSearch;

import uk.al_richard.BloomSearch.Util.OpenBitSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static uk.al_richard.BloomSearch.BloomProductSearch.ONE_MILLION;
import static uk.al_richard.BloomSearch.BloomProductSearch.bitsRequiredToEncode;
import static uk.al_richard.BloomSearch.Hash.pad;

public class MultipleEncodeDecodeTest {

    public static void main(String[] args) {

        int object_population_size = ONE_MILLION;
        int hash_length_in_bits = 16;
        int two_to_the_16 =  65536;
        int half_of_two_to_the_16 = two_to_the_16 / 2;
        int hash_overlap = 4;
        int num_bits_in_data_source = bitsRequiredToEncode(object_population_size);

        System.out.println( "Number of bits to encode one million " + pad(Integer.toBinaryString(object_population_size), num_bits_in_data_source) + " is " + num_bits_in_data_source );

        Iterator<Integer> generator = BalanceGen.getRandomIterator(object_population_size);

        int balanced_rep_size = BalanceGen.numberBitsInBalancedRep(object_population_size);

        System.out.println( "Size balanced rep " + balanced_rep_size );

        // Generate some numbers to add to Bloom.
        List<Integer> numbers = new ArrayList<>();
        int number_inputs = half_of_two_to_the_16;
        System.out.println( "Number numbers put into Bloom: " + number_inputs );

        for( int i = 0; i < number_inputs; i++ ) {
            if (generator.hasNext()) {
                int some_number = generator.next();
                numbers.add( some_number );
                // System.out.println("some_number = " + some_number + "\t" +  pad(Integer.toBinaryString(some_number), balanced_rep_size) + " " + Util.check( pad( Integer.toBinaryString(some_number), balanced_rep_size ), balanced_rep_size) );
            }
        }

        Bloom bloom = new Bloom( Math.pow(2,hash_length_in_bits) );

        Hash h = new Hash(hash_length_in_bits, hash_overlap, balanced_rep_size);

        List<Integer> all_hashes = new ArrayList<>();
        for( int some_number : numbers ) {
            List<Integer> hashed = h.hash(some_number);
            all_hashes.addAll( hashed );
            for (int hash : hashed) {
                bloom.addhash(hash);
            }
        }

        OpenBitSet bitset = bloom.getBits();

        ArrayList<Integer> hashes_from_bloom = new ArrayList<>();

        for (int next_set = bitset.nextSetBit(0); next_set >= 0; next_set = bitset.nextSetBit(next_set+1)) {
            hashes_from_bloom.add(next_set);
            if (next_set == Integer.MAX_VALUE) {
                break;
            }
        }

        boolean error = false;

        // compare set in bloom with original
        for( int i : all_hashes ) {
            if( ! hashes_from_bloom.contains(i) ) {
                System.out.println( "Error int in hashed not found in bloom: " + i );
                error = true;
            }
        }
        for( int i : hashes_from_bloom ) {
            if( ! all_hashes.contains(i) ) {
                System.out.println( "Error int in hashes_from_bloom not found in hashes: " + i );
                error = true;
            }
        }
        if( ! error ) {
            System.out.println("Hashes all recovered correctly from Bloom");
        }

    }
}
