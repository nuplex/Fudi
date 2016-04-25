package com.fudi.fudi.back;

import android.content.Intent;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TreeSet;

/**
 * Represents the app in of itself, mostly is needed for backend connections and total app
 * management. This does NOT represent the on-sceen
 * Created by chijioke on 4/18/16.
 */
public class FudiApp {

    /**
     * The instance of the Fudi App. This is where all interaction to the backend are made.
     * TODO: Almost everything should be done Asynchronously (in an AsyncTask)
     */
    private static FudiApp app = new FudiApp();

    private User thisUser;


    //Location variables
    private boolean locationRequested;

    /**
     * A Location set by the user in case all location requests fail.
     */
    private Location fixedLocation;

    /**
     * Some location that the user currently wants to peek at; this overrides any location
     * searching. Will be null if not peeking.
     */
    public Location peekLocation;
    private FudiLocationListener locationListener;

    private FudiApp(){
        thisUser = TestDatabase.getInstance().getTestUser();
        /*TODO: connect to database and get the user_id and phone number associated with this
        phones user and load in that user.
         */
        locationRequested = false;
        locationListener = new FudiLocationListener();
        fixedLocation = new Location(LocationManager.GPS_PROVIDER);
    }

    public static FudiApp getInstance(){
        return app;
    }

    public static NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());

    /**
     * Gets a FudDetail from the database based on it's ID
     * @param fudId
     * @return the FudDetail for the associated ID, or null if not found
     */
    public FudDetail pullFudDetail(String fudId){
        //TODO: get the FudDetail from the databse based on it's ID
        return null;
    }

    /**
     * Gets a FudDetail from the database based on it's ID
     * @param intent the Intent created by Fud.pass()
     * @return the FudDetail for the associated ID, or null if not found
     */
    public FudDetail pullFudDetail(Intent intent){
        return pullFudDetail(intent.getStringExtra("fudID"));
    }

    /**
     * Checks whether or not the user has an internet connection; and that it can be connected to
     * @return true if can make a network connection, false otherwise
     */
    public boolean hasNetworkConnection(){
        //TODO: check if the user has either Wifi or Data and that it can be connected to
        return false;
    }

    /*TODO To do all the Push and Pull methods in the app, you will need to use a
    DatabaseInteractor. Look at that class to determine how to properly call it.
    * */

    /**
     * Constructs fuds by retrieving FudDetails from the database based on their ID's
     * @param fudIDs
     * @return the Fuds for the passed in fudIds
     */
    public TreeSet<Fud> getFuds(ArrayList<String> fudIDs){
        //TODO: get the FudDetails from the database and turn them into Fuds
        return null;
    }

    /**
     * Constructs fuds by retrieving FudDetails from the database based on recency and the
     * user's GeoArea
     * @param amount
     * @return the Fuds that are the most recent in the user's GeoArea
     */
    public TreeSet<Fud> getFuds(int amount){
        /*TODO: get amount FudDetails from the database. These would be the most recently
        posted FudDetails for the user's current GeoArea*/
        return null;
    }

    /**
     * Constructs fuds by retrieving FudDetails from the database based on recency and
     * @param amount
     * @param where
     * @return
     */
    public TreeSet<Fud> getFuds(int amount, GeoArea where){
        /*TODO: get amount FudDetails from the database. These would be the most recently
        posted FudDetails for the specified locationa*/
        return null;
    }

    public CommentSection pullCommentSection(String csId){
        //TODO: get the CommentSection from the databse based on it's ID
        return null;
    }

    public User pullUserData(String userId){
        //TODO: get a User's data from the database based on their ID

        return null;
    }

    public User getThisUser() {
        return thisUser;
    }

    public static String getTimeSincePostedString(Date time){
        Date now = Calendar.getInstance().getTime();
        long diff = now.getTime() - time.getTime();
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
            return getFormattedTime(time);
        }
    }

    public static String getFormattedTime(Date time){
        return (new SimpleDateFormat("MMM d, yyyy - h:mm a").format(time));
    }

    /**
     * Updates the user's location.
     */
    public void updateLocation(){
        locationRequested = true;
    }

    /**
     * The LocationListener for the app. Runs whenever a location request is made.
     */
    public class FudiLocationListener implements android.location.LocationListener{

        private boolean GPSIsOut;
        private boolean NetworkIsOut;
        private boolean PassiveIsOut;

        private Location currentNetworkLocation;
        private Location currentGPSLocation;
        private Location currentPassiveLocation;

        public FudiLocationListener(){
            currentNetworkLocation = new Location(LocationManager.NETWORK_PROVIDER);
            currentGPSLocation = new Location(LocationManager.GPS_PROVIDER);
            currentPassiveLocation = new Location(LocationManager.PASSIVE_PROVIDER);
        }

        /**
         * Returns the location from preference to GPS -> Passive -> Network
         * @return the Location, or null if no location is available.
         */
        public Location getLocation(){
            if(!GPSIsOut){
                return currentGPSLocation;
            } else if (!PassiveIsOut){
                return currentPassiveLocation;
            } else if (!NetworkIsOut){
                return currentNetworkLocation;
            } else {
                return null;
            }
        }

        public boolean isLocationable(){
            return !NetworkIsOut;
        }

        @Override
        public void onLocationChanged(Location location) {
            if(locationRequested) {
               String provider = location.getProvider();
                switch(provider){
                    case LocationManager.GPS_PROVIDER:
                        currentGPSLocation = location;
                        break;
                    case LocationManager.NETWORK_PROVIDER:
                        currentNetworkLocation = location;
                        break;
                    case LocationManager.PASSIVE_PROVIDER:
                        currentPassiveLocation = location;
                        break;
                }
            }
            locationRequested = false;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //TODO: change the isOut variables accordingly
        }

        @Override
        public void onProviderEnabled(String provider) {
            //TODO: change the isOut variables accordingly
        }

        @Override
        public void onProviderDisabled(String provider) {
            //TODO: implement logic for if all are disabled, or if NETWORK is disabled.
            //Network should never be out.
        }
    }

}
