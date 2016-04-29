package com.fudi.fudi.back;

import android.content.Intent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
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
    private String userID;
    private String firebaseID;

    private String username;

    /**
     * The user's phone number. ALL users must have a phone number
     */
    private String phoneNumber;

    /**
     * If a user opts to fully registered. A registered user has a username.
     */
    private boolean firstTime;
    private boolean registered;
    private boolean verified;

    /**
     * A user's "fu" is equivalent to reddit Karam or YikYak yakarma.
     * It raises based on the user's own posts and comments and how much they get upvoted.
     */
    private long fu;

    private Date dateJoined;
    public static final String UNREGISTERED = "_unregistered";
    private static final String PREFIX = "U";

    public User(){
        this.phoneNumber = "";
        username = UNREGISTERED;
        registered = false;
        verified = false;
        fu = 0;
        dateJoined = Calendar.getInstance().getTime();
        firstTime = true;
    }

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
        firstTime = true;
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
        firstTime = true;
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

    public static User firebaseToUser(HashMap<String, Object> hm){
        String usnm =  (String) hm.get("username");
        Boolean fstTime = (Boolean) hm.get("firstTime");
        String pNum = (String) hm.get("phoneNumber");
        Boolean vrfd = (Boolean) hm.get("verified");
        Long fu = (Long) hm.get("fu");
        Date dateJoined = new Date((Long) hm.get("dateJoined"));
        User u = new User();
        u.username = usnm;
        u.firstTime = fstTime;
        u.phoneNumber = pNum;
        u.verified = vrfd;
        u.fu = fu;
        u.dateJoined = dateJoined;
        u.userID = (String) hm.get("userID");
        return u;
    }

    /**
     * Checks the database to check if the user is registered.
     * @return true if registered, false otherwise
     */
    public boolean isRegistered(){
        return registered;
    }

    /**
     * Checks the database to check if the user is verified.
     * @return true if verified, false otherwise
     */
    public boolean isVerified() {
        return verified;
    }

    public static String generateID(){
        return PREFIX+FudiApp.generateID(15);
    }

    public boolean isFirstTime() {
        return firstTime;
    }

    public String getUsername(){
        return username;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public long getFu() {
        return fu;
    }

    public String getUserID() {
        return userID;
    }

    public Date getDateJoined() {
        return dateJoined;
    }

    public String getFirebaseID() {
        return firebaseID;
    }

    //Returns a user with just a userID and username;
    public static User getStandInUser(String userID, String username){
        User standIn = new User();
        standIn.setUsername(username);
        standIn.setUserID(userID);
        return standIn;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setFirstTime(boolean firstTime) {
        this.firstTime = firstTime;
    }

    public void setFirebaseID(String firebaseID) {
        this.firebaseID = firebaseID;
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
