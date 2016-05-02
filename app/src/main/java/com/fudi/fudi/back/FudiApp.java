package com.fudi.fudi.back;

import android.app.Activity;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents the app in of itself, mostly is needed for backend connections and total app
 * management. This does NOT represent the on-sceen
 * Created by chijioke on 4/18/16.
 */
public class FudiApp {

    /**
     * The instance of the Fudi App. This is where all interaction to the backend are made.
     */
    private static FudiApp app = new FudiApp();

    private static final String SERVER_LOCATION = "https://fudiapp.firebaseio.com/";
    public static final String USERS = "users";
    public static final String FUDDETAILS = "fudDetails";
    public static final String COMMENTS = "comments";
    private static final int TIMEOUT = 10000;

    private static final int DEFAULT_FUD_PULL_NUM = 25;

    private User thisUser;
    private String thisUsersID;

    private FudDetail currentFudDetailToDisplay = null;
    private Fud currentOperatingFud = null;
    private FudDetail currentOperatingFudDetail = null;
    private CommentSection currentOperatingCommentSection = null;
    private TreeSet<FudDetail> currentlyDisplayedFudDetails = new TreeSet<FudDetail>();

    public boolean alreadyDidLogin = false;


    //Location variables
    private boolean locationRequested;
    private Firebase firebase;

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
        /*TODO: connect to database and get the user_id and phone number associated with this
        phones user and load in that user.
         */
        firebase = new Firebase(SERVER_LOCATION);
        //thisUser = TestDatabase.getInstance().getTestUser();
        locationRequested = false;
        locationListener = new FudiLocationListener();
        fixedLocation = new Location(LocationManager.GPS_PROVIDER);
    }

    public static FudiApp getInstance(){
        return app;
    }

    public static NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());


    /**
     * Checks whether or not the user has an internet connection; and that it can be connected to
     * @return true if can make a network connection, false otherwise
     */
    public static boolean hasNetworkConnection(){

        boolean has = false;
        try {
            URL urlToCheck = new URL("http://www.fudi.us");
            HttpURLConnection check = (HttpURLConnection) urlToCheck.openConnection();
            check.disconnect();
            has = true;
        } catch (IOException e){
            has = false;
        }
        return has;
    }

    /**
     * Gets a FudDetail from the database based on it's ID
     * @param fudId
     * @return the FudDetail for the associated ID, or null if not found
     */
    public FudDetail pullFudDetail(String fudId){
        final Firebase fudDetailRef = firebase.child(FUDDETAILS).child(fudId);
        final AtomicBoolean done = new AtomicBoolean(false);
        fudDetailRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.getValue().equals("test")) {
                    currentFudDetailToDisplay =
                            FudDetail.firebaseToFudDetail((HashMap<String, Object>) dataSnapshot.getValue());
                    done.set(true);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                done.set(true);
            }
        });

        busyWaitOrTimeout(done);

        return currentFudDetailToDisplay;
    }

    public CommentSection pullCommentSectionForFudDetail(String fudId) {
        final Firebase fudDetailCommentsRef = firebase.child(FUDDETAILS).child(fudId).child(COMMENTS);
        final AtomicBoolean done = new AtomicBoolean(false);
        fudDetailCommentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentOperatingCommentSection =
                        CommentSection.firebaseToCommentSection(
                                (HashMap<String, Object>) dataSnapshot.getValue());
                done.set(true);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                done.set(true);
            }
        });

        return currentOperatingCommentSection;
    }


    public Fud pullFud(String fudId){
        final Firebase fudDetailRef = firebase.child(FUDDETAILS).child(fudId);
        final AtomicBoolean done = new AtomicBoolean(false);
        fudDetailRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentOperatingFudDetail =
                        FudDetail.firebaseToFudDetail((HashMap<String, Object>) dataSnapshot.getValue());
                done.set(true);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                done.set(true);
            }
        });

        busyWaitOrTimeout(done);

        if(currentFudDetailToDisplay == null){
            return null;
        } else {
            return currentOperatingFudDetail.simplify();
        }
    }

    public TreeSet<FudDetail> pullFudsByTime(){
        (new AsyncTask<Void, Void,Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                final Firebase fudDetailsRef = firebase.child(FUDDETAILS);
                Query query = fudDetailsRef.orderByChild("timestamp");
                final AtomicBoolean done = new AtomicBoolean(false);

                query.addChildEventListener(new

                    ChildEventListener(){
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            if (dataSnapshot.exists()) {
                                if(dataSnapshot.getKey().equals("test")){return;}
                                FudDetail fd = FudDetail.firebaseToFudDetail(
                                        (HashMap<String, Object>)dataSnapshot.getValue());
                                if (fd != null) {
                                    currentlyDisplayedFudDetails.add(fd);
                                }
                            }
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                            if(dataSnapshot.getKey().equals("test")){return;}
                            for(FudDetail fd : currentlyDisplayedFudDetails){
                                if(fd.getFudID().equals(dataSnapshot.getKey())){
                                    fd = FudDetail.firebaseToFudDetail(
                                            (HashMap<String, Object>)dataSnapshot.getValue());
                                    break;
                                }
                            }
                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                        }
                    }

                );
                return  null;
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        return currentlyDisplayedFudDetails;
    }

    public void pushFudDetail(final FudDetail fudDetail){
        final String fudID = fudDetail.getFudID();
        final Firebase fudRef = firebase.child(FUDDETAILS);
        final AtomicBoolean done = new AtomicBoolean(false);
        fudRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(fudID)){
                    //update
                    fudRef.child(fudID).setValue(fudDetail.toFirebase());
                } else {
                    //make
                    fudRef.child(fudID).push();
                    fudRef.child(fudID).setValue(fudDetail.toFirebase());
                }
                done.set(true);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                done.set(true);
            }
        });

        busyWaitOrTimeout(done);
    }

    public void pushCommentForFudDetail(String fudID, final Comment comment){
        final Firebase fudDetailCommentsRef = firebase.child(FUDDETAILS).child(fudID).child(COMMENTS);
        final AtomicBoolean done = new AtomicBoolean(false);
        fudDetailCommentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long i = dataSnapshot.getChildrenCount();
                fudDetailCommentsRef.child(Long.toString(i)).push();
                fudDetailCommentsRef.child(Long.toString(i)).setValue(comment.toFirebase());
                done.set(true);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                done.set(true);
            }
        });

        busyWaitOrTimeout(done);
    }

    public User getThisUser() {
        if(thisUser == null) {
            final String userId = thisUsersID;
            final Firebase userRef = firebase.child(USERS);
            final AtomicBoolean done = new AtomicBoolean(false);
            //Query q = userRef.equalTo(use)
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(userId)) {
                        HashMap<String, Object> hm =
                                (HashMap<String, Object>) dataSnapshot.child(userId).getValue();
                        thisUser = User.firebaseToUser(hm);
                    } else {
                        userRef.child(userId).push().getKey();
                        User u = new User();
                        u.setUserID(userId);
                        userRef.child(userId).setValue(u);
                        thisUser = u;
                    }
                    done.set(true);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    done.set(true);
                }
            });

            busyWaitOrTimeout(done);

            if (thisUser == null) {
                return TestDatabase.getInstance().getTestUser();
            } else {
                return thisUser;
            }
        } else {
            return thisUser;
        }
    }

    public void loadInThisID(String userID){
        thisUsersID = userID;
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

    public FudDetail getCurrentFudDetailToDisplay() {
        return currentFudDetailToDisplay;
    }

    public TreeSet<FudDetail> getCurrentlyDisplayedFudDetails() {
        return currentlyDisplayedFudDetails;
    }

    /**
     * Busy waits the async task for something to finish.
     * @param done
     * @return whether exited naturally (true) or timed out/an error occured (false)
     */
    public boolean busyWaitOrTimeout(AtomicBoolean done){
        int waitTime = 100;
        int waited = 0;
        while(!done.get()){
            if(waited >= TIMEOUT){
                return false;
            }

            synchronized (this) {
                try {
                    this.wait(waitTime);
                } catch (InterruptedException e){
                    return false;
                }
            }
            waited += waitTime;

        }
        Log.d("WAITED", Integer.toString(waited));
        return true;
    }

    public static String generateID(int length){
        char[] lower = {'a','b','c','d','e','f','g','h','i','j'
                ,'k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
        char[] upper =  {'A','B','C','D','E','F','G','H','I','J',
                'K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
        int i = length;
        StringBuffer id = new StringBuffer("");
        SecureRandom r = new SecureRandom();
        while(i > 0) {
            int choice = r.nextInt(3);
            switch (choice) {
                case 0:
                    id.append(lower[(new SecureRandom()).nextInt(lower.length)]);
                    break;
                case 1:
                    id.append(upper[(new SecureRandom()).nextInt(upper.length)]);
                    break;
                case 2:
                    id.append((new SecureRandom().nextInt(10)));
                    break;
                default:
                    break;
            }
            i--;
        }
        return id.toString();
    }

    public Fud getCurrentOperatingFud() {
        return currentOperatingFud;
    }

    public FudDetail getCurrentOperatingFudDetail() {
        return currentOperatingFudDetail;
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public Firebase getFirebase() {
        return firebase;
    }

    /**
     * Updates the vote for any Voteable object
     */
    public void updateVote(Voteable v){
        final AtomicBoolean done = new AtomicBoolean(false);

        //TODO: First, push the changed vote data for this user

        if(v instanceof FudDetail){
            FudDetail fudDetail = (FudDetail) v;

            //Get Location of the FudDetail we are updating.
            String fudID = fudDetail.getFudID();
            final Firebase fudRef = firebase.child(FUDDETAILS).child(fudID);
            final Map<String, Object> votes = new TreeMap<String, Object>();
            votes.put("downvotes", fudDetail.getVote().getDownvotes());
            votes.put("upvotes", fudDetail.getVote().getUpvotes());

            fudRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    fudRef.setValue(votes);
                    done.set(true);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    done.set(true);
                }
            });

            busyWaitOrTimeout(done);
        } else if(v instanceof Fud) {
            Fud fud = (Fud) v;

            //Get Location of the FudDetail we are updating.
            String fudID = fud.getFudID();
            final Firebase fudRef = firebase.child(FUDDETAILS).child(fudID);
            final Map<String, Object> votes = new TreeMap<String, Object>();
            votes.put("downvotes", fud.getVote().getDownvotes());
            votes.put("upvotes", fud.getVote().getUpvotes());

            fudRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    fudRef.setValue(votes);
                    done.set(true);
                }
                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    done.set(true);
                }
            });

            busyWaitOrTimeout(done);
        } else if(v instanceof Comment){
            Comment comment = (Comment) v;
            CommentSection cSection = (CommentSection) comment.getParent();
            FudDetail fDetail = (FudDetail) cSection.getParentFud();
            //Need to discriminate between a GeneralComment and ReviewComment
            //TODO: Don't worry about this yet, or, you can. Same as above just storing the vote data.

            String fudID = fDetail.getFudID();
            final Firebase fudRef = firebase.child(FUDDETAILS).child(fudID).child(COMMENTS)
                    .child(comment.getText());
            final Map<String, Object> votes = new TreeMap<String, Object>();
            votes.put("downvotes", fDetail.getVote().getDownvotes());
            votes.put("upvotes", fDetail.getVote().getUpvotes());
            fudRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    fudRef.setValue(votes);
                    done.set(true);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    done.set(true);
                }
            });

            busyWaitOrTimeout(done);
        }

        return;
    }

    /**
     * Updates the user's location.
     */
    public void updateLocation(){

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
