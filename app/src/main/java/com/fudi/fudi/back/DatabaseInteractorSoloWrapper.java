package com.fudi.fudi.back;

import android.content.Context;
import android.os.AsyncTask;

/**
 * This wraps around a DatabaseInteractor object and prevents it
 * from being Asynchronous. This is to force certain operations to be actively processed by the
 * application and allows easier failure handling (i.e. show the user whatever was being
 * posted had failed and not show it on the next screen)
 *
 * If you are using this class, you MUST call execute() from here, NOT on the passed in
 * DatabaseInteractor.
 *
 * <b>Execute must be called from the main UI Thread.</b>
 * Created by chijioke on 4/21/16.
 */
public class DatabaseInteractorSoloWrapper{

    private DatabaseInteractor di;
    private Context context;

    private String MSG = "Processing...";


    public DatabaseInteractorSoloWrapper(Context context, DatabaseInteractor di){
        this.context = context;
        this.di = di;
    }

    /**
     * Sets the message that shows up on the screen. If this is not called, or the parameter is null,
     * the defualt message "Processing..." will be used.
     * @param msg The message to display while the task is processing;
     */
    public void setProcessingMessage(String msg){
        if(msg != null) {
            MSG = msg;
        }
    }

    /**
     * Executes the DatabaseInteractor that was passed in and return the result of the execution for
     * later handling.
     * @return the result of the task
     */
    public boolean execute(){
        if(di == null){
            throw new NullPointerException();
        }
        di.execute();
        //TODO: put a loading/processing popup on the screen with a message
        while(di.getStatus() != AsyncTask.Status.FINISHED ){}
        //TOO
        return di.getResult();
    }
}
