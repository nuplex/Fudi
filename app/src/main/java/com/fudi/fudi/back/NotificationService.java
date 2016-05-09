package com.fudi.fudi.back;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.HashMap;

public class NotificationService extends Service {


    private static final String SERVER_LOCATION = "https://fudiapp.firebaseio.com/";

    private String userID;
    private NotificationThread nt;

    public NotificationService() {
        nt = new NotificationThread();
    }

    @Override
    public void onCreate(){
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID){
        Log.i("Service", "Service Started");

        if(intent != null){
            userID = intent.getStringExtra("userID");
            Log.i("Service: UserID", userID);
            nt.start();
        } else {

        }
        return START_NOT_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    private final class NotificationThread extends Thread{


        private Firebase firebase;
        private HashMap<String, Object> values;

        @Override
        public void run() {

            Log.i("NotificationThread", "Thread Started");

            firebase = new Firebase(SERVER_LOCATION);
            values = new HashMap<String, Object>();

            Firebase userNotifyRef = firebase.child(FudiApp.USERS).child("U33s0N67eXR49e2o").child(FudiApp.NOTIFICATIONS);

            userNotifyRef.addChildEventListener(new ChildEventListener() {
                // Retrieve new posts as they are added to the database
                @Override
                public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                    Log.i("NotificationService:", "onChildAdded Triggered");
                    values = (HashMap<String, Object>) snapshot.getValue();
                    Log.i("NotificationService:", (String) values.toString());
                    FudiNotification fn = FudiNotification.fromFirebaseToFudiNotification(values);
                }

                @Override
                public void onChildRemoved(DataSnapshot snapshot) {
                    Log.i("NotificationService:", "Child Removed");

                }

                @Override
                public void onChildChanged(DataSnapshot snapshot, String string) {

                }

                @Override
                public void onChildMoved(DataSnapshot snapshot, String string) {

                }

                @Override
                public void onCancelled(FirebaseError error) {

                }

            });

        }
    }
}
