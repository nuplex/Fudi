package com.fudi.fudi.back;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
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
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

//import com.google.android.gms.location.LocationServices;

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
    public static final String USERNAMES = "usernames";
    public static final String PHONENUMBERS = "phoneNumbers";
    public static final String FUDDETAILS = "fudDetails";
    public static final String COMMENTS = "comments";
    public static final String NOTIFICATIONS = "notifications";
    private static final int TIMEOUT = 10000;

    private static final int DEFAULT_FUD_PULL_NUM = 25;

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 26;


    private User thisUser;
    private String thisUsersID;
    private String currentVerifyCode;

    private FudDetail currentFudDetailToDisplay = null;
    private Fud currentOperatingFud = null;
    private FudDetail currentOperatingFudDetail = null;
    private CommentSection currentOperatingCommentSection = null;
    private TreeSet<FudDetail> currentlyDisplayedFudDetails = new TreeSet<FudDetail>();
    private TreeSet<FudiNotification> currentOperatingNotifications = new TreeSet<FudiNotification>();
    private boolean childEventActive = false;

    private AtomicBoolean found = new AtomicBoolean(true);


    public boolean alreadyDidLogin = false;


    //Location variables
    private boolean locationRequested;
    private Firebase firebase;
    private LocationManager locationManager;
    private GeoArea currentArea;

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
                if (!dataSnapshot.getValue().equals("test")) {
                    currentFudDetailToDisplay =
                            FudDetail.firebaseToFudDetail((HashMap<String, Object>) dataSnapshot.getValue());
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        return currentFudDetailToDisplay;
    }

    public void updateVote(Voteable v){

        TreeMap<String, Object> votes = new TreeMap<String, Object>();

        if( v instanceof Fud) {
            Fud f = (Fud) v;
            final Firebase fudDetailRef = firebase.child(FUDDETAILS).child(f.getFudID());

            votes.put("netvotes", f.getVote().getNet());
            votes.put("upvotes", f.getVote().getUpvotes());
            votes.put("downvotes", f.getVote().getDownvotes());

            fudDetailRef.updateChildren(votes);


        } else if (v instanceof FudDetail){
            FudDetail fd = (FudDetail) v;
            final Firebase fudDetailRef = firebase.child(FUDDETAILS).child(fd.getFudID());

            votes.put("netvotes", fd.getVote().getNet());
            votes.put("upvotes", fd.getVote().getUpvotes());
            votes.put("downvotes", fd.getVote().getDownvotes());

            fudDetailRef.updateChildren(votes);

        } else if (v instanceof Comment){
            Comment c = (Comment) v;
            long commentNumber = c.getCommentNumber();

            final Firebase fudDetailRef = firebase.child(FUDDETAILS).child(
                    c.getParent().getParentFud().getFudID()).child("comments").child(String.valueOf(c.getCommentNumber()));

            votes.put("netvotes", c.getVote().getNet());
            votes.put("upvotes", c.getVote().getUpvotes());
            votes.put("downvotes", c.getVote().getDownvotes());

            fudDetailRef.updateChildren(votes);

        }

    }


//   public CommentSection pullCommentSectionForFudDetail(String fudId) {
//        final Firebase fudDetailCommentsRef = firebase.child(FUDDETAILS).child(fudId).child(COMMENTS);
//        final AtomicBoolean done = new AtomicBoolean(false);
//        fudDetailCommentsRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                currentOperatingCommentSection =
//                        CommentSection.firebaseToCommentSection(
//                                (HashMap<String, Object>) dataSnapshot.getValue());
//                done.set(true);
//            }
//
//            @Override
//            public void onCancelled(FirebaseError firebaseError) {
//                done.set(true);
//            }
//        });
//
//        return currentOperatingCommentSection;
//    }


    @Deprecated
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

        final CountDownLatch done = new CountDownLatch(1);
        (new AsyncTask<Void, Void,Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                final Firebase fudDetailsRef = firebase.child(FUDDETAILS);
                Query query = fudDetailsRef.orderByChild("timestamp");

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
                            done.countDown();
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                            if(dataSnapshot.getKey().equals("test")){return;}
                            FudDetail found = null;
                            FudDetail toAdd = null;
                            for(FudDetail fd : currentlyDisplayedFudDetails){
                                if(fd.getFudID().equals(dataSnapshot.getKey())){
                                    found = fd;
                                    toAdd = FudDetail.firebaseToFudDetail(
                                            (HashMap<String, Object>)dataSnapshot.getValue());
                                    break;
                                }
                            }
                            if(toAdd != null){
                                if(found != null){
                                    currentlyDisplayedFudDetails.remove(found);
                                }
                                currentlyDisplayedFudDetails.add(toAdd);
                            }
                            done.countDown();
                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getKey().equals("test")){return;}
                            FudDetail found = null;
                            for(FudDetail fd : currentlyDisplayedFudDetails){
                                if(fd.getFudID().equals(dataSnapshot.getKey())){
                                    found = fd;
                                    break;
                                }
                            }
                            if(found != null){
                                currentlyDisplayedFudDetails.remove(found);
                            }
                            done.countDown();

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                            done.countDown();
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                            done.countDown();
                        }
                    }

                );
                return  null;
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        try {
            done.await();
        }catch (InterruptedException e){

        }

        return currentlyDisplayedFudDetails;
    }

    public void pushFudDetail(final FudDetail fudDetail){
        final String fudID = fudDetail.getFudID();
        final Firebase fudRef = firebase.child(FUDDETAILS);
        final AtomicBoolean done = new AtomicBoolean(false);
        fudRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(fudID)) {
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
                comment.setCommentNumber(i);
                fudDetailCommentsRef.child(Long.toString(i)).push();
                fudDetailCommentsRef.child(Long.toString(i)).setValue(comment.toFirebase());
                done.set(true);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                done.set(true);
            }
        });

        (new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                pushNotifications(comment);
                return null;
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        busyWaitOrTimeout(done);
    }

    public void pushNotifications(Comment comment){

        CommentSection cs = comment.getParent();
        Iterator<Comment> comments = cs.getComments().iterator();
        FudDetail fd = comment.getParent().getParentFud();

        FudiNotification fudNotify = new FudiNotification(fd.getDishName(), fd.getFudID(),
                comment.getTimestamp(), false, FudiNotification.NotificationType.FUD_POST);

        FudiNotification commentNotify = new FudiNotification(fd.getDishName(), fd.getFudID(),
                comment.getTimestamp(), false, FudiNotification.NotificationType.COMMENTED_ON);

        HashMap<String, Object> notification = fudNotify.toFirebase();
        HashMap<String, Boolean> alreadyNotified = new HashMap<String, Boolean>();

        Firebase notificationRef = firebase.child(USERS);

        if(alreadyNotified.get(fd.getWhoPosted().getUsername()) == null) {
            Firebase thisNotifyRef = notificationRef.child(fd.getWhoPosted().getUserID()).child(NOTIFICATIONS);
            thisNotifyRef.child(fudNotify.getNotificationID()).push();
            thisNotifyRef.child(fudNotify.getNotificationID()).setValue(notification);
            Log.i("Pushing a Notification:", thisNotifyRef.toString());
            alreadyNotified.put(fd.getWhoPosted().getUsername(), true);
        }

        notification = commentNotify.toFirebase();

        while(comments.hasNext()){
            Comment c = comments.next();
            String username = c.getWhoPosted().getUsername();

            if (alreadyNotified.get(username) == null) {
                alreadyNotified.put(username, true);
                Firebase thisNotifyRef = notificationRef.child(c.getWhoPosted().getUserID()).child(NOTIFICATIONS);
                thisNotifyRef.child(commentNotify.getNotificationID()).push();
                thisNotifyRef.child(commentNotify.getNotificationID()).setValue(notification);
                Log.i("Pushing a Notification:", thisNotifyRef.toString());
            }
        }

        return;
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

            //busyWaitOrTimeout(done);

            if (thisUser == null) {
                return TestDatabase.getInstance().getTestUser();
            } else {
                return thisUser;
            }
        } else {
            return thisUser;
        }
    }

    public void getUsersFuds(String userID){

    }

    public void getCommentedOnFudsForUser(String userID){

    }

    public TreeSet<FudiNotification> getNotifications(){
        Firebase notifRef = firebase.child(USERS).child(NOTIFICATIONS);
        Query notifs = notifRef.orderByKey();
        notifs.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (!dataSnapshot.getKey().equals("test")) {
                    FudiNotification fn = FudiNotification.fromFirebaseToFudiNotification(
                            (HashMap<String, Object>) dataSnapshot.getValue());
                    if (fn != null) {
                        currentOperatingNotifications.add(fn);
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
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
        });


        return currentOperatingNotifications;
    }

    public void loadInThisID(String userID){
        thisUsersID = userID;
    }


    public String generateAndSendCode(String phoneNumber){
        final String code = generateCode();
        final Firebase userRef = firebase.child(USERS).child(thisUser.getUserID());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("verifyCode")) {
                    userRef.child("verifyCode").setValue(code);
                    currentVerifyCode = code;
                } else {
                    userRef.child("verifyCode").push();
                    userRef.child("verifyCode").setValue(code);
                    currentVerifyCode = code;
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, "Fudi Registration Code: " + code, null, null);

        return code;
    }

    public boolean verifyCode(final String code){
        Firebase userCode = firebase.child(USERS).child(thisUser.getUserID()).child("verifyCode");
        final AtomicBoolean done = new AtomicBoolean(false);
        userCode.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //if(dataSnapshot.getValue().equals(code)){
                currentVerifyCode = (String) dataSnapshot.getValue();
                //}
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
        if(currentVerifyCode == null){
            return false;
        } else {
            if(currentVerifyCode.equals(code)){
                return true;
            }
            return  false;
        }
    }

    /**
     *
     * @return True if username is available, false otherwise
     */
    public boolean checkUsernameAvailability(final String username){
        Firebase usernameRef = firebase.child(USERNAMES);
        usernameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(username)){
                    found.set(true);
                } else {
                    found.set(false);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        return !found.get();
    }

    public boolean checkPhoneNumberInUse(final String phoneNumber){
        final Firebase phoneNumberRef = firebase.child(PHONENUMBERS);
        final AtomicBoolean found = new AtomicBoolean(true);
        phoneNumberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(phoneNumber)) {
                    found.set(true);
                } else {
                    found.set(false);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                found.set(false);
            }
        });

        return !found.get();
    }

    public boolean registerThisUser(final String username, final String phoneNumber){
            final Firebase usernameRef = firebase.child(USERNAMES);
            final AtomicBoolean success = new AtomicBoolean(false);
            usernameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    thisUsersID = thisUser.getUserID();
                    usernameRef.child(username).push();
                    usernameRef.child(username).child("username").push();
                    usernameRef.child(username).child("username").setValue(username);
                    usernameRef.child(username).child("userID").push();
                    usernameRef.child(username).child("userID").setValue(thisUser.getUsername());
                    Firebase userRef = firebase.child(USERS).child(thisUsersID);
                    userRef.child("username").setValue(username);
                    userRef.child("registered").setValue(true);
                    userRef.child("verified").setValue(true);
                    userRef.child("phoneNumber").setValue(phoneNumber);
                    firebase.child(PHONENUMBERS).child(phoneNumber).child("userID").push();
                    firebase.child(PHONENUMBERS).child(phoneNumber).child("userID").setValue(thisUsersID);
                    userRef.child("firstTime").setValue(false);
                    success.set(true);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    success.set(false);

                }
            });
            return success.get();
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
            if(diff == 1) {
                return diff + " day ago";
            } else {
                return diff + " days ago";
            }
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
    public boolean busyWaitOrTimeout(AtomicBoolean done, long ms){
        int waitTime = 100;
        int waited = 0;
        while(!done.get()){
            if(waited >= ms){
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

    private static String generateCode(){
        SecureRandom r = new SecureRandom();
        String code = Integer.toString(r.nextInt(900000) + 100000);
        return code;
    }

    public Fud getCurrentOperatingFud() {
        return currentOperatingFud;
    }

    public FudDetail getCurrentOperatingFudDetail() {
        return currentOperatingFudDetail;
    }

    public TreeSet<FudiNotification> getCurrentOperatingNotifications() {
        return currentOperatingNotifications;
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        try {
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    public Firebase getFirebase() {
        return firebase;
    }

    /**
     * Updates the user's location.
     */
    public void updateLocation() {
        currentArea = new GeoArea("Current", locationListener.getLocation().getLatitude(),
                locationListener.getLocation().getLongitude(),10000);
    }

    public LocationManager FudiLocationManager(Context context) {
         return locationManager = (LocationManager)
                 context.getSystemService(Context.LOCATION_SERVICE);
    }

    public void setLocationRequested(boolean requested) {
        this.locationRequested = requested;
    }

    public Location getCurrentLocation() {
        return locationListener.getLocation();
    }

    public GeoArea getCurrentArea() {
        return currentArea;
    }

    public void setCurrentArea(GeoArea geoArea) {
        currentArea = new GeoArea(geoArea.getName(), geoArea.getLatitude(), geoArea.getLongitude(),
                geoArea.getRadius());
    }

    public FudiLocationListener getLocationListener() {
        return locationListener;
    }

    public void checkLocationPermission (Activity activity) {
        if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String []{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }
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
            //Log.i("LOCATION REQUESTED", "LOCATION REQUESTED");
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
            switch(provider){
                case LocationManager.GPS_PROVIDER:
                    switch (status) {
                        case LocationProvider.OUT_OF_SERVICE:
                            GPSIsOut = true;
                            break;
                        case LocationProvider.TEMPORARILY_UNAVAILABLE:
                            GPSIsOut = true;
                            break;
                        case LocationProvider.AVAILABLE:
                            GPSIsOut = false;
                            break;
                    }
                case LocationManager.NETWORK_PROVIDER:
                    switch (status) {
                        case LocationProvider.OUT_OF_SERVICE:
                            NetworkIsOut = true;
                            break;
                        case LocationProvider.TEMPORARILY_UNAVAILABLE:
                            NetworkIsOut = true;
                            break;
                        case LocationProvider.AVAILABLE:
                            NetworkIsOut = false;
                            break;
                    }
                case LocationManager.PASSIVE_PROVIDER:
                    switch (status) {
                        case LocationProvider.OUT_OF_SERVICE:
                            PassiveIsOut = true;
                            break;
                        case LocationProvider.TEMPORARILY_UNAVAILABLE:
                            PassiveIsOut = true;
                            break;
                        case LocationProvider.AVAILABLE:
                            PassiveIsOut = false;
                            break;
                    }
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            //TODO: change the isOut variables accordingly
            switch (provider) {
                case LocationManager.GPS_PROVIDER:
                    GPSIsOut = false;
                    break;
                case LocationManager.NETWORK_PROVIDER:
                    NetworkIsOut = false;
                    break;
                case LocationManager.PASSIVE_PROVIDER:
                    PassiveIsOut = false;
                    break;
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            //TODO: implement logic for if all are disabled, or if NETWORK is disabled.
            //Network should never be out.
            switch (provider) {
                case LocationManager.GPS_PROVIDER:
                    GPSIsOut = true;
                    break;
                case LocationManager.NETWORK_PROVIDER:
                    Log.i("NETWORK IS OUT", "WHY IS YOUR NETWORK OUT?");
                    NetworkIsOut = true;
                    break;
                case LocationManager.PASSIVE_PROVIDER:
                    PassiveIsOut = true;
                    break;
            }
        }
    }

}
