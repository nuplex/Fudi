package com.fudi.fudi.back;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.fudi.fudi.R;
import com.fudi.fudi.front.FudDetailActivity;
import com.fudi.fudi.front.MainActivity;
import com.fudi.fudi.front.NotificationView;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

public class NotificationService extends Service {


    public static FudDetail notifyFud;
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
            notifyFud = null;
            Log.i("Service: UserID", userID);

            try {
                nt.start();
            } catch(IllegalThreadStateException e){

            }
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
        private FudDetail fd;
        private CountDownLatch cd;
        private HashMap<String, Object> values;

        @Override
        public void run() {

            Log.i("NotificationThread", "Thread Started");

            firebase = new Firebase(SERVER_LOCATION);
            values = new HashMap<String, Object>();

            Firebase userNotifyRef = firebase.child(FudiApp.USERS).child(userID).child(FudiApp.NOTIFICATIONS);

            userNotifyRef.addChildEventListener(new ChildEventListener() {
                // Retrieve new posts as they are added to the database
                @Override
                public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {

                    if(!(snapshot.getValue() instanceof  HashMap)){
                        return;
                    }

                    values = (HashMap<String, Object>) snapshot.getValue();
                    Log.i("NotificationService:", values.toString());
                    //FudiNotification fn = FudiNotification.fromFirebaseToFudiNotification(values);

                    // Grab the FudDetail from online.
                    final Firebase fudDetailRef = firebase.child(FudiApp.FUDDETAILS).child((String) values.get("fudID"));
                    fudDetailRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.i("NotificationService:", "Grabbing Fud");
                            notifyFud = FudDetail.firebaseToFudDetail((HashMap<String, Object>) dataSnapshot.getValue());

                            //Build the notification.
                            NotificationCompat.Builder mBuilder =
                                    new NotificationCompat.Builder(getApplicationContext())
                                            .setSmallIcon(R.drawable.fudi_icon)
                                            .setContentTitle("Fudi")
                                            .setContentText("Someone commented on your post!");

                            Intent resultIntent = new Intent(getApplicationContext(), FudDetailActivity.class);
                            resultIntent.putExtra(Fud.EXTRA_TAG_ID,(String)values.get("fudID"));
                            resultIntent.putExtra(NotificationView.FROM_NOTIFICATION,true);
                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
                            stackBuilder.addParentStack(MainActivity.class);
                            stackBuilder.addParentStack(FudDetailActivity.class);
                            stackBuilder.addNextIntent(resultIntent);
                            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                            mBuilder.setContentIntent(resultPendingIntent);


                            NotificationManager mNotifyMgr =
                                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                            mNotifyMgr.notify(001,mBuilder.build());

                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                        }
                    });

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
