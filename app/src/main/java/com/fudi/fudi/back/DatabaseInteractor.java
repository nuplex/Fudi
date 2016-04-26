package com.fudi.fudi.back;

import android.os.AsyncTask;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class is used to pull or push data from the Database. It uses "describers" and the MethodCalled
 * enum to determine how it handles various pull requests. Describers can be thought of as the
 * method parameters and return object references.
 *
 * Database Info:
 * //TODO: Enter info here for connecting
 * Created by chijioke on 4/20/16.
 */
public class DatabaseInteractor extends AsyncTask<Void, Void, Boolean>{

    /**
     * Designates the method that was called since many methods pull and push data from the database.
     *
     * Format: CLASSNAME_METHODNAME[_PARAM], e.g. FUDDETAIL_UPDATE
     */
    private interface MethodCalled{}
    public enum PullMethodCalled implements MethodCalled{
        FUDIAPP_PULL_FUDI_DETAILS, FUDIAPP_GET_FUDS_AMOUNT, FUDIAPP_GET_FUDS_AMOUNT_WHERE,
        FUDIAPP_PULL_COMMENTSECTION, FUDIAPP_PULL_USER_DATA,

        FUDDETAIL_UPDATE,

        COMMENTSECTION_PULL_COMMENTS,

        USER_PULL, USER_PULL_COMMENTS, USER_PULL_FUDS, USER_IS_REGISTERED, USER_IS_VERIFIED,

        GEOAREA_GET_GEO_AREA
    }

    public enum PushMethodCalled implements MethodCalled{
        FUDDETAIL_PUSH,

        COMMENTSECTION_PUSH, COMMENTSECTION_PUSH_COMMENT, COMMENTSECTION_PUSH_COMMENTS,
        COMMENTSECTION_DELETE,

        USER_PUSH, USER_PUSH_COMMENT, USER_PUSH_FUD, USER_UPDATE_FU
    }

    private enum Request{PUSH,PULL}

    private MethodCalled method;
    private Request request;
    private TreeMap<String, Object> describers;
    private boolean result;

    //TODO: add any variables you need

    // Reference to the root of the database.
    private Firebase ref;

    // This will be used to ensure that we do complete the
    // AsyncTask until the write is finished.
    private final AtomicBoolean done = new AtomicBoolean(false);

    public DatabaseInteractor(MethodCalled method, Request request){
        this.method = method;
        this.request = request;
        describers = new TreeMap<String, Object>();

        //TODO: set any variables you need
        ref = new Firebase("https://fudi.firebaseio.com");
        result = false;

    }

    public DatabaseInteractor(DatabaseInteractor di){
        this.method = di.method;
        this.request = di.request;
        describers = new TreeMap<String, Object>(describers);
    }

    /**
     * Adds a describer to the task to identify objects that need to be pushed or pulled.
     * Think of this as passing in method parameters that will be used, as well
     * as the return object.
     *
     * For example, we have in FudiApp TreeSet<Fud> getFuds(int amount, GeoArea where)
     * If we want to properly pull call this class we will have to do this:
     *
     * DatabaseInteractor di = new DatabaseInteractor(PullMethodCall.FUDIAPP_GET_FUDS_AMOUNT_WHERE);
     * di.putDescriber("amount", amount);
     * di.putDescriber("where", where);
     * di.execute();
     *
     * If you are using a DatabaseInteractorSoloWrapper, do the above but call execute from the
     * wrapper.
     *
     * @param tag The tag for this describer
     * @param describer the object which will hold the pulled results
     */
    public void putDescriber(String tag, Object describer){
        describers.put(tag, describer);
    }

    /**
     * Gets the Object with the associated tag.
     * @param tag The tag of the describer
     * @return The associated Object, or null if object is found for the tag.
     */
    public Object getDescriber(String tag){
        return describers.get(tag);
    }

    @Override
    protected Boolean doInBackground(Void... v) {
        //TODO: Do the pull logic for the requested method
        /*
        Get the passed in describers corresponding to the paramters and return object
        If it helps, you can write private methods representing the method from those classes, so that
        this method isn't 1000 lines long.
         */

        switch(request){
            case PULL:
                return pull();
            case PUSH:
                return push();
            default: return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean bool){
        result = bool;
        //TODO: do something if failed (false)
        /* Probably need to pass in a context as well, so that on failure
            whatever operation needed this is canceled, and a message is shown to the user that there
            was an error.

            TODO:I've created a DatabaseInteractorSoloWrapper which forces this task
            to be non-Asynchronous. This will allow any operations that should NOT be asynchronous,
            that is if they fail they absolutely should not be updated located.

            Something still needs to be done here however. The DatabaseInteractorSoloWrapper can only
            make the application wait for this operation to be done.

            Application cannot show something locally that has not been updated in the database,
            though if it did it would simply just erase that on a refresh.
         */
    }

    private boolean push(){
        PullMethodCalled pmc = (PullMethodCalled) method;
        switch(pmc){
            case FUDDETAIL_UPDATE:
                //TODO: implement the other cases
            default: return false;
        }
    }

    private boolean pull(){
        PushMethodCalled pmc = (PushMethodCalled) method;
        switch(pmc){
            case FUDDETAIL_PUSH:
                //TODO
                return true|false;
            case COMMENTSECTION_PUSH:
                //TODO: implement the other cases'
                return true|false;
            default: return false;
        }
    }

    private boolean fudDetailPush(){
        FudDetail fudDetail = (FudDetail) getDescriber("fudDetail");
        //TODO: do the rest;


        return true|false; //true - worked, false - did not
    }

    /**
     * Gets the result of the process. This should only be called in when the AsyncTask
     * is known to be finished.
     * @return true if successful, false otherwise
     */
    protected boolean getResult(){
        return result;
    }

    /**
     * Push Methods : Methods called in the push() function to place data on the Database.
     * All push calls will overwrite any data already on the database. (i.e., they update
     * existing information instead of duplicating it).
     */


    // PushMethodCalled.USER_PUSH
    private boolean pushUser(){

        //Make sure done and isSuccess are set to false.
        done.set(false);
        result = false;

        // Grab the user from the describes map and grab the reference
        // to the part of the database where we want to save the data.
        User user = (User) describers.get("user");
        Firebase userRef = ref.child("users").child(user.getUserID());

        // Cycle through the information the User has
        // and place any information in the Map that will be
        // placed on the database.
        Map<String, String> userValues = new HashMap<String, String>();

        if(user.getPhoneNumber() != null){
            userValues.put("phoneNumber", user.getPhoneNumber());
        }

        if(user.getUsername() != user.UNREGISTERED){
            userValues.put("username", user.getUsername());
        }

        if(user.getUserID() != null){
            userValues.put("userID", user.getUserID());
        }

        if(user.getDateJoined() != null){
            userValues.put("dateJoined", user.getDateJoined().toString());
        }

        if(userValues.size() != 0) {

            userRef.setValue(userValues, new Firebase.CompletionListener() {

                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    if (firebaseError == null) {
                        result = true;
                    } else {
                        result = false;
                    }
                    done.set(true);
                }
            });

            // Busy wait in the Async Task until the
            // write call is done.
            while (!done.get()) ;

        }

        // Reset done to false so that it can be used again.
        done.set(false);
        return result;
    }


    //PushMethodCalled.COMMENTSECTION_PUSH_COMMENT;
    private boolean pushCSComment(){

        //Make sure done and isSuccess set to false
        done.set(false);
        result = false;

        // Grab the comment and its parent.
        Comment comment = (Comment) describers.get("comment");
        CommentSection parent = comment.getParent();

        // All comments will be pushed to the same place.
        // They will be uniquely identified on the server.
        // Firebase API will handle the unique identification.
        Firebase commentsDataRef = ref.child("comments");

        Map<String, Object> commentInfo = new HashMap<String, Object>();

        if(parent != null){
            commentInfo.put("commentSection", parent.getCommentSectionID());
        }

        if(comment != null){
            commentInfo.put("text", comment.getText());
            commentInfo.put("poster", comment.getWhoPosted());
            commentInfo.put("upvotes", comment.getVote().getUpvotes());
            commentInfo.put("downvotes", comment.getVote().getDownvotes());
            commentInfo.put("date", comment.getTimestamp().toString());
        }


        // Get a Unique ID for this comment in the comments dataset.
        Firebase commentRef = commentsDataRef.push();

        commentRef.setValue(commentInfo, new Firebase.CompletionListener(){

            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase){
                if(firebaseError == null){
                    result = true;
                } else {
                    result = false;
                }
                done.set(true);
            }
        });

        while(!done.get());

        done.set(false);
        return result;
    }

//    //PushMethodCalled.COMMENTSECTION_PUSH
//    private boolean pushCommentSection(){
//
//        //Set booleans to false
//        done.set(false);
//        result = false;
//
//        // Grab CommentSection we will be pushing
//        CommentSection commentSection = (CommentSection) describers.get("commentSection");
//
//
//    }



    /**
     * Pull Methods : Methods called in the pull() function to retreive data from the Database.
     */
}
