package uk.al_richard.BloomSearch;

import uk.al_richard.BloomSearch.Util.OpenBitSet;
import uk.al_richard.BloomSearch.Util.Util;

import java.util.ArrayList;
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

        // First check the hash algorithms and the ability to encode/decode.

        Hash h = new Hash(hash_length_in_bits, hash_overlap, balanced_rep_size);

        List<Integer> hashed = h.hash(some_number);

        System.out.println("hashes:");
        for (int hash : hashed) {
            System.out.println( hash + " " + pad(Integer.toBinaryString(hash), hash_length_in_bits));
        }

        Util.checkSubString( pad( Integer.toBinaryString(some_number), balanced_rep_size ),hashed, hash_length_in_bits);
        Set<Integer> reversed = h.reverseHashes(hashed); // May contain false positives
        Set<Integer> filtered = BalanceGen.filter(reversed, balanced_rep_size);

        System.out.println("filtered:");
        for( int i : filtered ) {
            System.out.println("filtered = " + i + "\t" +  pad(Integer.toBinaryString(i), balanced_rep_size) + " " + Util.check( pad( Integer.toBinaryString(i), balanced_rep_size ),balanced_rep_size ) );
            Util.checkSubString( pad( Integer.toBinaryString(i), balanced_rep_size ),hashed, hash_length_in_bits);
        }

        // Next check bloom filtering

        Bloom b = new Bloom( Math.pow(2,hash_length_in_bits) );
        for (int hash : hashed) {
            b.addhash(hash);
        }

        OpenBitSet bitset = b.getBits();

        ArrayList<Integer> hashes_from_bloom = new ArrayList<>();

        for (int next_set = bitset.nextSetBit(0); next_set >= 0; next_set = bitset.nextSetBit(next_set+1)) {
            hashes_from_bloom.add(next_set);
            if (next_set == Integer.MAX_VALUE) {
                break;
            }
        }

        boolean error = false;

        // compare set in bloom with original
        for( int i : hashed ) {
            if( ! hashes_from_bloom.contains(i) ) {
                System.out.println( "Error int in hashed not found in bloom: " + i );
                error = true;
            }
        }
        for( int i : hashes_from_bloom ) {
            if( ! hashed.contains(i) ) {
                System.out.println( "Error int in hashes_from_bloom not found in hashes: " + i );
                error = true;
            }
        }
        if( ! error ) {
            System.out.println( "Hashes all recovered correctly from Bloom");
        }

    }
}
