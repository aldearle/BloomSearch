package uk.al_richard.BloomSearch;

import uk.al_richard.BloomSearch.Util.Util;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static uk.al_richard.BloomSearch.BloomProductSearch.ONE_MILLION;
import static uk.al_richard.BloomSearch.BloomProductSearch.bitsRequiredToEncode;
import static uk.al_richard.BloomSearch.Hash.pad;

public class HashEncodeDecodeTest {

    public static void main(String[] args) {

        int object_population_size = ONE_MILLION;
        int hash_length_in_bits = 16;
        int hash_overlap = 4;
        int num_bits_in_data_source = bitsRequiredToEncode(object_population_size);

        System.out.println( "Number of bits to encode one million " + pad(Integer.toBinaryString(object_population_size), num_bits_in_data_source) + " is " + num_bits_in_data_source );

        Iterator<Integer> generator = BalanceGen.getRandomIterator(object_population_size);

        int balanced_rep_size = BalanceGen.numberBitsInBalancedRep(object_population_size);

        System.out.println( "Size balanced rep " + balanced_rep_size );

        int some_number = 0;
        if (generator.hasNext()) {
            some_number = generator.next();
        }

        System.out.println("some_number = " + some_number + "\t" +  pad(Integer.toBinaryString(some_number), balanced_rep_size) + " " + Util.check( pad( Integer.toBinaryString(some_number), balanced_rep_size ), balanced_rep_size) );

        Hash h = new Hash(hash_length_in_bits, hash_overlap, balanced_rep_size);

        List<Integer> hashed = h.hash(some_number);

        System.out.println("hashes:");
        for (int hash : hashed) {
            System.out.println(pad(Integer.toBinaryString(hash), hash_length_in_bits));
        }

        Util.checkSubString( pad( Integer.toBinaryString(some_number), balanced_rep_size ),hashed, hash_length_in_bits);

        Set<Integer> reversed = h.reverseHashes(hashed); // May contain false positives

        Set<Integer> filtered = BalanceGen.filter(reversed, balanced_rep_size);

        System.out.println("filtered:");
        for( int i : filtered ) {
            System.out.println("filtered = " + i + "\t" +  pad(Integer.toBinaryString(i), balanced_rep_size) + " " + Util.check( pad( Integer.toBinaryString(i), balanced_rep_size ),balanced_rep_size ) );
            Util.checkSubString( pad( Integer.toBinaryString(i), balanced_rep_size ),hashed, hash_length_in_bits);
        }
    }
}
