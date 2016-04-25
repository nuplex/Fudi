package com.fudi.fudi.back;

import android.os.AsyncTask;
import java.util.TreeMap;
import java.util.concurrent.Executor;

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


    public DatabaseInteractor(MethodCalled method, Request request){
        this.method = method;
        this.request = request;
        describers = new TreeMap<String, Object>();

        //TODO: set any variables you need
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
}
