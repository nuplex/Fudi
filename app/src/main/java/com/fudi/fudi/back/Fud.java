package com.fudi.fudi.back;

import android.content.Intent;
import android.location.Location;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

/**
 * Represents a 'Fud' or a post on the Main Screen.
 *
 * Note: A Fud post is a simplified version of a FudDetail
 *
 * The Fud post is what displays a post of a particular dish. There is a photo of the dish displayed.
 * (Photos are mandatory), along with this is some sort of upvote/downvote button, that may be
 * stylized, or just standard arrows (e.g. ^ V). The number of net votes will be displayed
 * (upvote - downvote) as well as the number of comments/reviews. The restaurant the dish is at will
 * also be displayed, as well as it’s cost. Optionally a “real rating” may be displayed, which
 * aggregates internal review comments and their ratings of the dish.
 * Optionally, to implement, a fud post may be saved.
 *
 * Created by chijioke on 4/14/16.
 */
public class Fud implements Comparable<Fud>, Voteable{

    private String imageURL;
    private Vote vote;
    private String dishName;
    private String fudID;
    private String userID;
    private String restaurant;
    private String cost;
    private int comments;
    private Date timestamp;

    public static String EXTRA_TAG_ID = "fudID";


    protected Fud(String fudID, String imageURL, String dishName, String restaurant, String cost,
                  Vote vote, int comments, Date timestamp, String userID){
        this.fudID = fudID;
        this.dishName = dishName;
        this.imageURL = imageURL;
        this.restaurant = restaurant;
        this.cost = cost;
        this.vote = vote;
        this.comments = comments;
        this.timestamp = timestamp;
        this.userID = userID;
    }

    /**
     * Passes the fudId of this fudPost
     * @return the Intent holding the ID
     */
    public Intent pass(){
        Intent i = new Intent();
        i.putExtra(EXTRA_TAG_ID,fudID);
        return i;
    }

    public String getImageURL() {
        return imageURL;
    }

    public Vote getVote() {
        return vote;
    }

    public String getDishName() {
        return dishName;
    }

    public int getComments() {
        return comments;
    }

    public String getRestaurant() {
        return restaurant;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getUserID() {
        return userID;
    }

    public String getTimeSincePostedString(){
        Date now = Calendar.getInstance().getTime();
        long diff = now.getTime() - timestamp.getTime();
        diff =  Math.round(diff/1000); //want in seconds
        if(diff < 60){
            return diff + "s ago";
        } else if (diff < 3600){
            diff = Math.round(diff/60);
            return diff + " m ago";
        } else if (diff <  86400){
            diff = Math.round(diff/3600);
            return diff + " hr ago";
        } else if (diff < 604800){ //604800 is 7 days
            diff = Math.round(diff/86400);
            return diff + " days ago";
        } else {
            return getFormattedTime();
        }
    }

    public String getFormattedTime(){
        return (new SimpleDateFormat("MMM d, yyyy - h:mm a").format(timestamp));
    }

    public String getFudID() {
        return fudID;
    }

    public String getCost() {
        return cost;
    }

    @Override
    public boolean equals(Object o){
        if(o == null){
            return false;
        } else if(!(o instanceof  Fud)){
            return false;
        } else {
            return fudID.equals(((Fud) o).fudID);
        }
    }

    @Override
    public int compareTo(Fud another) {
        return (new FudComparatorByTime()).compare(this, another);
    }

    private class FudComparatorByTime implements Comparator<Fud>{

        @Override
        public int compare(Fud lhs, Fud rhs) {
            return rhs.timestamp.compareTo(lhs.timestamp);
        }
    }

}
