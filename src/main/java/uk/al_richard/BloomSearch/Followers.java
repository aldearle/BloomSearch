package uk.al_richard.BloomSearch;

import java.util.*;

/**
 * Stores the ground truth used to test the BloomMap code
 * This code is not necessary in a real system.
 * Used in initialisation of the RecommenderMap since the ground truth must be mirrored in it.
 */
public class Followers {
    private final List<Integer> data;
    private final int objects_per_influencer;
    private final List<Integer> refs;
    private Map<Integer,List<Integer>> map = new HashMap<>();

    Random rand = new Random(98162435 ); // key mash for repeatability

    public Followers(List<Integer> refs, List<Integer> data, int objects_per_influencer) {
        
        this.data = data;
        this.refs = refs;
        this.objects_per_influencer = objects_per_influencer;

        for( int ro : refs ) {
            List<Integer> followers = constructFollowers(ro);
            map.put( ro,followers);
        }
    }

    /**
     *
     * @return required reference objects at random
     */
    public List<Integer> createQueryInfluencers(int required ) {
        List<Integer> result = new ArrayList<>();
        for( int i = 0; i < required; i++ ) {
            result.add( getRandomInfluencer() );
        }
        return result;
    }

    public List<Integer> getInfluencersOf( int obj ) {
        System.out.println( "In data : " + data.contains(obj) );
        ArrayList<Integer> influencers = new ArrayList<>();
        for( int ro : map.keySet() ) {
            List<Integer> influenced_by = map.get(ro);
            if( influenced_by.contains(obj) ) {
                influencers.add( ro );
            }
        }
        return influencers;
    }

    /**
     *
     * @param ro a reference object
     * @return the objects that reference (are influenced by) the reference object ro
     */
    public List<Integer> getFollowers(int ro) {
        return map.get(ro);
    }

    //------ private methods ------//

    private List<Integer> constructFollowers(int ro) {
        List<Integer> followers = new ArrayList<>();
        for( int i = 0; i < objects_per_influencer; i++ ) {
            followers.add( getRandomFollower());
        }
        return followers;
    }

    private Integer getRandomFollower() {
        int index = rand.nextInt( data.size() ); // random within list of values
        return data.get( index );
    }

    private Integer getRandomInfluencer() {
        int index = rand.nextInt(refs.size()); // random within list of values
        return refs.get(index);
    }

    /**
     * All the data items should be influenced by the influencers in the query - this checks this.
     *  @param results - the results of the query - a set of data items
     * @param query - a list of influencers we are matching against
     */
    public void checkResults(Set<Integer> results, List<Integer> query) {
        for (int data_item : results) {
            int wrong = 0;
            int correct = 0;
            if (data.contains(data_item)) {
                for (int ref_object : query) {
                    List<Integer> object_ids = map.get(ref_object);
                    if (!object_ids.contains(data_item)) {
                        System.out.println("\tnot influenced by " + ref_object);
                        wrong++;
                    } else {
                        System.out.println("\t    influenced by " + ref_object + "++++");
                        correct++;
                    }
                }
                System.out.println("Data result: " + data_item + " influencers correct = " + correct + " Wrong = " + wrong);
            }
        }
    }

}
