package com.fudi.fudi.back;

import android.content.Intent;

import java.util.Calendar;
import java.util.Date;
import java.util.TreeSet;

/**
 * Created by chijioke on 4/14/16.
 *
 * Represents a user on Fudi.
 */
public class User {

    /**
     * The User Id is unique for every user and should be generated in such a way that all user IDs
     * are unique. Creating a hash from the user's phone number and some other data may be the
     * the easiest way. Note this can be a long or a String, dependent on how the ID is generated
     */
    private final String userID;

    private String username;

    /**
     * The user's phone number. ALL users must have a phone number
     */
    private String phoneNumber;

    /**
     * If a user opts to fully registered. A registered user has a username.
     */
    private boolean registered;
    private boolean verified;

    /**
     * A user's "fu" is equivalent to reddit Karam or YikYak yakarma.
     * It raises based on the user's own posts and comments and how much they get upvoted.
     */
    private int fu;

    private Date dateJoined;
    private Date dateRegistered;
    private Date dateVerified;
    private boolean isInDatabase;
    public static final String UNREGISTERED = "_unregistered";
    private static final String PREFIX = "U";

    public User(){ userID = generateID();}

    /**
     * This constructor is for users who choose not to immediately register. All users must have
     * a phone number associated with them.
     * @param phoneNumber
     */
    public User(String phoneNumber){
        this.phoneNumber = phoneNumber;
        userID = generateID();
        username = UNREGISTERED;
        registered = false;
        verified = false;
        fu = 0;
        dateJoined = Calendar.getInstance().getTime();
        isInDatabase = true;
    }

    /**
     * This constructor is for users who registered. Note the registration process can also verify
     * a user.
     * @param phoneNumber
     * @param username
     */
    public User(String phoneNumber, String username){
        this.phoneNumber = phoneNumber;
        userID = generateID();
        this.username = username;
        registered = true;
        verified = false;
        fu = 0;
        dateJoined = Calendar.getInstance().getTime();
        isInDatabase = true;
    }

    /**
     * Sends all changed user data to the database. If the user was just created, must add the user
     * to the database
     */
    public void push(){
        //TODO: push all non-constant variables to the database
        /*if you want to be effecient, push only variables that have changed*/


        //TODO: new user case
        /*userId is final and cannot be changed, if this can be represented in the database, ensure
        it is.*/
    }

    /**
     * Pulls user data from database, updating the local user
     */
    public void pull(){
        //TODO: pulls all variables from the database or this user
    }

    /**
     * Pulls all comments for this user from the database.
     * @return a TreeSet of Comment
     */
    public TreeSet<Comment> pullComments(){
        //TODO: pull all user's comments from the database; you will need to reconstruct the comment
        return null;
    }

    /**
     * Pulls all general comments for this user from the database.
     * @return a TreeSet of GeneralComment
     */
    public TreeSet<GeneralComment> pullGeneralComments(){
        //TODO: call pullComments() and return a set only of GeneralComments
        return null;
    }

    /**
     * Pulls all review comments for this user from the database.
     * @return a TreeSet of ReviewComment
     */
    public TreeSet<ReviewComment> pullReviews(){
        //TODO: call pullComments() and return a set of only ReviewComments;
        return null;
    }


    /**
     * Pulls all fuds for this user from the database.
     * @return a TreeSet of Fud
     */
    public TreeSet<Fud> pullFuds(){
        //TODO: pull all user's fuds from the database; you will need to reconstruct the fud
        return null;
    }

    /**
     * Updates fu locally and on the database for this User.
     * @param voteType
     */
    public void updateFu(Vote.Type voteType){
        if(voteType == Vote.Type.UPFU){
            fu++;
        } else if (voteType == Vote.Type.DOWNFU){
            fu--;
        } else {return;}

        //TODO update user's fu score in the database
    }

    /**
     * Checks the database to check if the user is registered.
     * @return true if registered, false otherwise
     */
    public boolean isRegistered(){
        return isRegistered();
    }

    /**
     * Checks the database to check if the user is verified.
     * @return true if verified, false otherwise
     */
    public boolean isVerified() {
        return isVerified();
    }

    public Date getDateRegistered() {
        return dateRegistered;
    }

    public Date getDateVerified() {
        return dateVerified;
    }

    private String generateID(){
        /*TODO: Create a method for generating IDs, suggestion: use phoneNumber + someOtherStat.
            A good method would ensure no collisions happen between IDs, ever. A good ID is one
            that is generated like a hash.
            Should of course handle collisions.
            TODO: IDs should be PREFIX + 10 characters, numerical and uppercase A-Z.
         */
        return PREFIX+"";
    }

    public String getUsername(){
        return username;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public int getFu() {
        return fu;
    }

    public String getUserID() {
        return userID;
    }

    public Date getDateJoined() {
        return dateJoined;
    }

    public boolean isInDatabase() {
        return isInDatabase;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setIsInDatabase(boolean isInDatabase) {
        this.isInDatabase = isInDatabase;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setFu(int fu) {
        this.fu = fu;
    }



    @Override
    public boolean equals(Object o){
        if(o == null){
            return false;
        } else if(!(o instanceof User)){
            return false;
        } else {
            return userID.equals(((User) o).userID);
        }
    }


}
