package uk.al_richard.BloomSearch;

import uk.al_richard.BloomSearch.Util.OpenBitSet;

public class Bloom {

    private final OpenBitSet bitrep;

    /**
     * Creates a Bloom filter with a width of width bits.
     * @param width - the width of the bloom filter in bits
     */
    public Bloom(double width ) {
        if( width > Integer.MAX_VALUE ) {
            throw new RuntimeException( "Cannot create a bloom filter with width " + width );
        }
        this.bitrep = new OpenBitSet((int) width);
    }

    /**
     * Adds the hash to this Bloom filter object
     * @param hash - the bit pattern to add to the filter.
     */
    public void addhash( int hash ) {
        bitrep.set( hash,true );
    }

    /**
     * Returns a hex representation of the bloom filter
     */
    public String toString() {
        byte[] bytes = bitrep.toByteArray();
        StringBuilder sb = new StringBuilder();
        sb.append( "Bytes: " );
        for( int i = 0; i < bytes.length; i++ ) {
            sb.append(Integer.toHexString(bytes[i]));
            if( ( ( i % 2 ) == 0 ) && i > 1 ) {
                sb.append(":");
            }
        }
        return sb.toString();
    }
}
