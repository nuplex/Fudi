package com.fudi.fudi.back;

import java.util.TreeMap;
import java.util.TreeSet;

/**
 * This is a temporary database for testing the UI.
 * Do not use this when the real database is set up and working.
 * This does not in anyway reflect the real database. This is way too simple.
 * Created by chijioke on 4/22/16.
 */
public class TestDatabase {

    private User mainUser;

    private TreeMap<String, FudDetail> fudDetails;

    private static TestDatabase instance = new TestDatabase();


    private TestDatabase(){
        mainUser = new User("555555555","testUser");
        fudDetails = new TreeMap<String, FudDetail>();
    }

    public static TestDatabase getInstance() {
        return instance;
    }

    public void put(String id, FudDetail fudDetail){
        fudDetails.put(id, fudDetail);
    }

    /**
     * Puts some random fake data in the database
     */
    public void load(){
        //TODO: implement sometime later
    }

    public TreeSet<Fud> getFuds(){
        TreeSet<Fud> fuds = new TreeSet<Fud>();
        for(String s : fudDetails.keySet()){
            fuds.add(getFud(s));
        }
        return fuds;
    }

    public Fud getFud(String id){
        return fudDetails.get(id).simplify();
    }

    public FudDetail getFudDetail(String id){
        return fudDetails.get(id);
    }

    public CommentSection getCommentSection(String fudId){
        return getFudDetail(fudId).getCommentSection();
    }

    public User getTestUser() {
        return mainUser;
    }
}
