package com.fudi.fudi.back;

import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Represents the details and comment section presented on the FudDetail Screen.
 * A fud detail consists of the photo of the food, net votes, restaurnt, price, the user who posted it
 * and their review, and the replies to the post.
 *
 * Optional: Aggregated rating of review comments
 *
 * Created by chijioke on 4/14/16.
 */
public class FudDetail implements Comparable<FudDetail>, Voteable{

    private String fudID;

    private String imageURL;

    private String dishName;

    private String restaurant;
    private String cost;
    private String description;
    private String[] tags;

    private User whoPosted;
    private Vote vote;

    private CommentSection commentSection;

    /**
     * The location where the post was made. Unsure if needed.
     */
    private Location locationPosted;

    /**
     * The location of the restaurant, this is used in filtering posts by area.
     */
    private Location locationOfRestaurant;
    private Date timestamp;

    private static final String PREFIX = "FD";

    public FudDetail(){fudID = genarateID();}

    public FudDetail(String imageURL, String dishName, String restaurant, String cost,
                     String description, User whoPosted, String... tags){
        fudID = genarateID();
        this.imageURL = imageURL;
        this.dishName = dishName;
        this.restaurant = restaurant;
        this.cost = cost;
        this.whoPosted = whoPosted;
        this.tags = tags;
        this.description = description;
        timestamp = Calendar.getInstance().getTime();
        vote = new Vote(whoPosted.getUserID());
        locationPosted = new Location(LocationManager.NETWORK_PROVIDER);
        locationOfRestaurant = new Location(LocationManager.NETWORK_PROVIDER);
        commentSection = new CommentSection(this);
    }

    /**
     * Creates a Fud post for displaying on the main screen. This done by "simplfying" the FudDetail
     * for simple display on the main screen as
     * @return Fud
     */
    public Fud simplify(){
        return new Fud(fudID, imageURL, dishName, restaurant, cost, vote,
                commentSection.getCommentNumber(), timestamp);
    }


    /**
     * Add a comment to the CommentSection for the FudDetail
     * @param comment a GeneralComment or ReviewComment
     */
    public void addComment(Comment comment){
        commentSection.postComment(comment);
    }


    public Location getLocationOfRestaurant() {
        return locationOfRestaurant;
    }

    /**
     * Sets the location of the restaruant by accessing Yelp based on the name of the Resatuarant.
     * If you need more info to do this, feel free to add necessary parameters.
     * @param restaurant name
     */
    public void setLocationOfRestaurant(String restaurant) {
        //TODO: If possible, access Yelp for this restaurant and get it's location (lat/long)
        //Should be done asynchronously
    }

    /**
     * Sets the location of the restaurant by passing in a location. This would be used if location
     * data can be acessed in some other way (i.e. select from GoogleMaps).
     * @param locationOfRestaurant
     */
    public void setLocationOfRestaurant(Location locationOfRestaurant) {
        this.locationOfRestaurant = locationOfRestaurant;
    }

    /**
     * Generates and ID for this FudDetail, should be a unique ID starting with the FudDetail
     * prefix, and then 10 alphanumeric characters (uppercase and lowercase letters);
     * @return the generated ID
     */
    public String genarateID(){
        return PREFIX + FudiApp.generateID(25);
    }

    public String getFudID() {
        return fudID;
    }

    public String getDishName() {
        return dishName;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getRestaurant() {
        return restaurant;
    }

    public String getDescription() {
        return description;
    }

    public String[] getTags() {
        return tags;
    }

    public String getCost() {
        return cost;
    }

    public User getWhoPosted() {
        return whoPosted;
    }

    public Vote getVote() {
        return vote;
    }

    public CommentSection getCommentSection() {
        return commentSection;
    }

    public Location getLocationPosted() {
        return locationPosted;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public TreeMap<String, Object> toFirebase(){
        TreeMap firebaseable = new TreeMap<String, Object>();
        firebaseable.put("fudID", fudID);
        firebaseable.put("imageURL", imageURL);
        firebaseable.put("dishName",dishName);
        firebaseable.put("restaurant",restaurant);
        firebaseable.put("cost",cost);
        firebaseable.put("description",description);
        firebaseable.put("tags",tags);
        firebaseable.put("whoPostedID", whoPosted.getUserID());
        firebaseable.put("whoPostedName", whoPosted.getUsername());
        firebaseable.put("upvotes", vote.getUpvotes());
        firebaseable.put("downvotes", vote.getDownvotes());
        firebaseable.put("netvote", vote.getNet());
        firebaseable.put("comments", commentSection.getFirebaseableComments());
        firebaseable.put("timestamp", timestamp);
        firebaseable.put("restLocLat", locationPosted.getLatitude());
        firebaseable.put("restLocLong", locationPosted.getLongitude());
        firebaseable.put("postedLocLat", locationPosted.getLatitude());
        firebaseable.put("postedLocLong", locationPosted.getLongitude());
        return firebaseable;
    }

    public static FudDetail firebaseToFudDetail(HashMap<String, Object> hm){
        FudDetail fd = new FudDetail();
        String userID = (String) hm.get("whoPostedID");
        fd.locationPosted = new Location(LocationManager.NETWORK_PROVIDER);
        fd.locationOfRestaurant = new Location(LocationManager.NETWORK_PROVIDER);

        Log.d("Contents", hm.toString());

        fd.fudID = (String) hm.get("fudID");
        fd.imageURL = (String) hm.get("imageURL");
        fd.dishName = (String) hm.get("dishName");
        fd.restaurant = (String) hm.get("restaurant");
        fd.cost = (String) hm.get("cost");
        fd.description = (String) hm.get("description");
        fd.tags =  firebaseTagsToStrings(hm.get("tags"));
        fd.whoPosted = User.getStandInUser(userID, (String) hm.get("whoPostedName"));
        fd.vote = new Vote((long) hm.get("upvotes"), (long) hm.get("downvotes"), userID);
        fd.commentSection = CommentSection.firebaseToCommentSection( (HashMap<String, Object>) hm.get("comments"));
        fd.timestamp = new Date((long) hm.get("timestamp"));
        fd.locationOfRestaurant.setLatitude((Double) hm.get("restLocLat"));
        fd.locationOfRestaurant.setLongitude((Double) hm.get("restLocLong"));
        fd.locationPosted.setLatitude((Double) hm.get("postedLocLat"));
        fd.locationPosted.setLongitude((Double) hm.get("postedLocLong"));

        return fd;
    }

    public Fud firebaseFudDetailToFud(HashMap<String, Object> hm){
        return firebaseToFudDetail(hm).simplify();
    }

    private static String[] firebaseTagsToStrings(Object objs){
        String[] s;
        if(objs == null){
            return new String[]{""};
        }
        if(objs instanceof ArrayList){
            Object[] os = ((ArrayList<Object>) objs).toArray();
            s = new String[os.length];
            for(int i = 0; i < s.length; i++){
                s[i] = (String) os[i];
            }
        } else {
            s = new String[((Object[]) objs).length];
            for(int i = 0; i < s.length; i++){
                s[i] = (String) ((Object[]) objs)[i];
            }
        }

        return s;
    }

    @Override
    public boolean equals(Object o){
        if(o == null){
            return false;
        } else if(!(o instanceof  FudDetail)){
            return false;
        } else {
            return fudID.equals(((FudDetail) o).fudID);
        }
    }

    @Override
    public int compareTo(FudDetail another) {
        return (new FudDetailComparatorByTime()).compare(this, another);
    }

    private class FudDetailComparatorByTime implements Comparator<FudDetail> {

        @Override
        public int compare(FudDetail lhs, FudDetail rhs) {
            return lhs.timestamp.compareTo(rhs.timestamp);
        }
    }

}
