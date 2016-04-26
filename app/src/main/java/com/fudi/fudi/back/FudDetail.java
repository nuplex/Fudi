package com.fudi.fudi.back;

import android.location.Location;
import android.location.LocationManager;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Random;
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

    private final String fudID;

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
     * Pushes the FudDetail to the database, if it is new, it will store a new FudDetail on the
     * database, otherwise it just updates the entry for this database.
     */
    public void push(){
        //TODO: connect to the database and update any variables that have changed here
        //Note: Should figure out how to properly represent a Vote and Location
        //Suggestion: Vote, call vote.push();
        //Suggestion: Location: store doubles lat and long
    }

    /**
     * Pulls data from the database and updates the FudDetail locally
     */
    public void update(){
        //TODO: connect to the database and pull down data for this FudDetail based on it's id

    }

    /**
     * Add a comment to the CommentSection for the FudDetail
     * @param comment a GeneralComment or ReviewComment
     */
    public void addComment(Comment comment){
        commentSection.postComment(comment);
        commentSection.pushComment(comment);
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
        //TODO: implement the real verison of this that is mostly collison free/handles collisions
        long l = ((new Random()).nextLong() + 1000000000) % 10000000000L;
        return PREFIX + l;
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
