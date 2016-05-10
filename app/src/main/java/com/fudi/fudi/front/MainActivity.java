package com.fudi.fudi.front;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.fudi.fudi.R;
import com.fudi.fudi.back.Fud;
import com.fudi.fudi.back.FudDetail;
import com.fudi.fudi.back.FudiApp;
import com.fudi.fudi.back.GeoArea;
import com.fudi.fudi.back.ImageHandler;
import com.fudi.fudi.back.NotificationService;
import com.fudi.fudi.back.TestDatabase;
import com.fudi.fudi.back.User;

import java.util.TreeSet;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "Fudi-App";

    private LinearLayout fudList;
    private TreeSet<FudView> fudViews;
    private ScrollView scroll;
    private FrameLayout mainframe;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageManager imgManager;
    private LocationManager locationManager;

    private boolean hotPressed;
    private boolean newPressed;

    protected static final int FUD_CREATION_SUCCESS = 1;
    protected static final int FUD_CREATION_FAILURE = 2;
    protected static final int LOGIN_SUCCESS = 3;
    protected static final int LOCATION_REQUEST_CODE = 4;
    protected static final int LOCATION_UPDATE_TIME = 60 * 1000 * 2;
    protected static final int LOCATION_UPDATE_DISTANCE = 100;

    private boolean firstTimeStopped;
    BroadcastReceiver rec;

    public MainActivity() {

    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Firebase.setAndroidContext(getApplicationContext());

        fudList = (LinearLayout) findViewById(R.id.main_fud_list);
        fudViews = new TreeSet<FudView>();
        mainframe = (FrameLayout) findViewById(R.id.main_frame_layout);
        imgManager = new ImageManager(getApplicationContext());
        hotPressed = false;
        newPressed = true;


        //Starts getting location requests

        FudiApp.getInstance().checkLocationPermission(MainActivity.this);

        locationManager = FudiApp.getInstance().FudiLocationManager(getApplicationContext());

        FudiApp.getInstance().setLocationRequested(true);



        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_UPDATE_TIME,
                LOCATION_UPDATE_DISTANCE, FudiApp.getInstance().getLocationListener());


        //set button onClickListener
        ImageButton newFudButton = (ImageButton) findViewById(R.id.main_new_fud_button);
        newFudButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FudCreationActivity.class);
                startActivityForResult(intent, FUD_CREATION_SUCCESS);
            }
        });

        scroll = (ScrollView) findViewById(R.id.main_scrollview);
        scroll.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
                    for (FudView fv : fudViews) {
                        Rect viewR = new Rect();
                        fv.getDrawingRect(viewR);
                        scroll.getDrawingRect(viewR);
                        int functionalTop = viewR.top - ImageHandler.pfdp(232 + 20 + 45, MainActivity.this);
                        int functionalBottom = viewR.bottom;
                        int thisTop = fv.getTopInScroll();
                        if (thisTop < functionalBottom && thisTop > functionalTop) {
                            if (!fv.imageIsLoaded()) {
                                fv.loadImage();
                            }
                        } else {
                            if (fv.imageIsLoaded()) {
                                fv.unloadImage();
                            }
                        }
                    }
                }
                return false;
            }
        });


        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        final TextView newButton = (TextView) findViewById(R.id.main_new_button);
        final TextView hotButton = (TextView) findViewById(R.id.main_hot_button);

        if (!networkCheck()) {
            return;
        }

        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!newPressed) {
                    newButton.setBackgroundColor(getResources().getColor(R.color.secondary_color_darker2));
                    hotButton.setBackgroundColor(getResources().getColor(R.color.secondary_color_darker));
                    newPressed = true;
                    hotPressed = false;
                    refresh();
                }
            }
        });

        hotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!hotPressed){
                    newButton.setBackgroundColor(getResources().getColor(R.color.secondary_color_darker));
                    hotButton.setBackgroundColor(getResources().getColor(R.color.secondary_color_darker2));
                    newPressed = false;
                    hotPressed = true;
                    refresh();
                }
            }
        });

        SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), MODE_PRIVATE);
        if (!sharedPref.contains("userID")) {
            String userID = User.generateID();
            sharedPref.edit().putString("userID", userID).commit();
        }

        if (!sharedPref.contains("firstTime")) {
            sharedPref.edit().putString("firstTime", "true").commit();
        }

        if (!sharedPref.contains("registered")) {
            sharedPref.edit().putString("registered", "false").commit();
        }

        String firstTime = sharedPref.getString("firstTime", "false");
        FudiApp.getInstance().loadInThisID(sharedPref.getString("userID", "notfound"));

        boolean gotUser = false;
        if (firstTime.equals("true")) {
            if (!FudiApp.getInstance().alreadyDidLogin) {
                FudiApp.getInstance().getThisUser();
                gotUser = true;
                if (!FudiApp.getInstance().getThisUser().isRegistered()) {
                    FudiApp.getInstance().alreadyDidLogin = true;
                    Intent reg = new Intent(this, LoginActivity.class);
                    startActivity(reg);
                }
            }
        }


        if (!gotUser) {
            (new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    FudiApp.getInstance().getThisUser();
                    return null;
                }
            }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        //liivt = new LoadImageInViewTask(fudViews, scroll);

        //liivt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


        firstTimeStopped = true;
        IntentFilter intentFilter = new IntentFilter("com.fudi.fudi.LISTUPDATED");
        rec = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                unregisterReceiver(rec);
                Log.i("Broadcast", "Received Broadcast");
                pull();
            }
        };

        registerReceiver(rec, intentFilter);

                (new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        FudiApp.getInstance().pullFudsByTime();

                        Intent intent = new Intent("com.fudi.fudi.LISTUPDATED");
                        sendBroadcast(intent);

                        Log.i("Broadcast", "Sent Broadcast");


                        return null;

                    }
                }).execute();


    }

    @Override
    public void onStop(){
        super.onStop();

        if(firstTimeStopped) {
            //Start the Notification Service.
            firstTimeStopped = false;
            Intent intent = new Intent(this, NotificationService.class);
            intent.putExtra("userID", FudiApp.getInstance().getThisUser().getUserID());
            startService(intent);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        pull();
        // refresh();
        //liivt = new LoadImageInViewTask(fudViews, scroll);
        //liivt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).execute();
        //System.gc();
    }

    @Override
    protected void onPause() {
        //pull();
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates(FudiApp.getInstance().getLocationListener());

        //liivt.cancel(true);
        //liivt = null;
        //removeAll();
        //System.gc();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //liivt.cancel(true);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*TODO; show proper popups, do FudiApp.pullFud() and/or FudiApp.getFuds() instead when
        * the database is up and working.
        */
        pull();
        if (requestCode == FUD_CREATION_SUCCESS && resultCode == RESULT_OK) {

            // refresh();
            /**
             * TODO: Uncomment when database works and remove above line
             fudList.add(FudiApp.getInstance().pullFudDetail(data.getStringExtra(Fud.EXTRA_TAG_ID)).simplify());
             */
        } else if (resultCode == FUD_CREATION_FAILURE && requestCode == FUD_CREATION_SUCCESS) {
            Toast.makeText(this, R.string.fud_creation_error, Toast.LENGTH_LONG).show();
        } else if (resultCode == RESULT_CANCELED) {

            //Results from the location screen
        } else if (requestCode == LOCATION_REQUEST_CODE && resultCode ==
                LocationPickActivity.HERE_SELECTED) {
            FudiApp.getInstance().setLocationRequested(true);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Location current = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            //Toast.makeText(getApplicationContext(), current.getLatitude() + " " + current.getLongitude(),
                    //Toast.LENGTH_LONG).show();
            FudiApp.getInstance().updateLocation();
        } else if (requestCode == LOCATION_REQUEST_CODE && resultCode ==
                LocationPickActivity.CHOOSE_SELECTED) {
            GeoArea changed = data.getParcelableExtra(LocationPickActivity.SELECTED_GEO_AREA);
            FudiApp.getInstance().setCurrentArea(changed);
            //Toast.makeText(getApplicationContext(), changed.toString(), Toast.LENGTH_LONG).show();
        } else if (requestCode == LOCATION_REQUEST_CODE && resultCode ==
                LocationPickActivity.GLOBAL_SELECTED) {
            FudiApp.getInstance().setCurrentArea(GeoArea.GLOBAL);
           // Toast.makeText(getApplicationContext(), GeoArea.GLOBAL.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id) {
            case R.id.action_location_here:
                Intent h = new Intent(MainActivity.this, LocationPickActivity.class);
                startActivityForResult(h, LOCATION_REQUEST_CODE);
                return true;
            case R.id.fudi_action_user_account:
                Intent i = new Intent(MainActivity.this, MeActivity.class);
                startActivity(i);
                return true;
            case R.id.action_settings:
                Intent j = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(j);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean networkCheck(){
        if(!FudiApp.hasNetworkConnection()){
            fudList.removeAllViews();
            LinearLayout noNetwork = (LinearLayout)
                    getLayoutInflater().inflate(R.layout.no_network, null);
            noNetwork.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(networkCheck()){
                        pull();
                    }
                }
            });
            fudList.addView(noNetwork);
            return false;
        } else {
            return true;
        }
    }

    public void pull(){
        Log.i("Pull", "Pull called.");
        if(hotPressed){
            organizeByUpvotes();
        } else {

        removeAll();

            TreeSet<FudDetail> fudDetails = FudiApp.getInstance().getCurrentlyDisplayedFudDetails();
            if (fudDetails == null) {
                fudDetails = new TreeSet<FudDetail>();
                Log.e("ERROR", "fuds was null in main, loading empty");
            }

            addFudDetailsToList(fudDetails);
            display();
        }
    }

    public void display(){
        int toDisplay = 3;
        int pos = 0;
        for(FudView fv : fudViews){
            fudList.addView(fv.getView());
            Space between = new Space(MainActivity.this);
            fudList.addView(between);
            between.getLayoutParams().height = ImageHandler.pfdp(20,MainActivity.this);
            fv.setTopInScroll(pos);
            if(toDisplay >= 0){
                fv.loadImage();
                toDisplay--;
            }
            pos = pos + ImageHandler.pfdp(232 + 20, this);
        }
    }

    public void displayWithAtTop(Fud fud){
        FudView top = new FudView(MainActivity.this, fud);
        fudList.addView(top);
        top.loadImage();
        Space between1 = new Space(MainActivity.this);
        between1.getLayoutParams().height = ImageHandler.pfdp(20,MainActivity.this);

        int pos = 0;
        int toDisplay = 3;
        for(FudView fv : fudViews){
            fudList.addView(fv.getView());
            Space between = new Space(MainActivity.this);
            between.getLayoutParams().height = ImageHandler.pfdp(20,MainActivity.this);

            fv.setTopInScroll(pos);
            if(toDisplay > 0){
                fv.loadImage();
                toDisplay--;
            }
            pos = pos + ImageHandler.pfdp(232 + 20, this);
        }

    }

    private void addFudsToList(TreeSet<Fud> fuds){
        for(Fud f : fuds){
            FudView fv = new FudView(MainActivity.this, f);
            addFudView(fv);
        }
    }

    private void addFudDetailsToList(TreeSet<FudDetail> fuds){
        for(FudDetail fd : fuds){
            FudView fv = new FudView(MainActivity.this, fd.simplify());
            addFudView(fv);
        }
    }
    /**
     * A logical renaming of pull();
     */
    public void refresh(){
        pull();
    }

    /**
     * Pull's new views from the database, clears the old ones, and display the new ones with
     * the selected {@param fud} up top.
     * @param fud The fud to place up top.
     */
    public void refreshWithAtTop(Fud fud){
        removeAll();
        addFudsToList(TestDatabase.getInstance().getFuds());
        displayWithAtTop(fud);
    }

    public void addFudView(FudView fudView){
        fudViews.add(fudView);
    }

    public void addFudViews(TreeSet<FudView> fudViews){
        for(FudView fv : fudViews) {
            fudViews.add(fv);
        }
    }

    public void remove(FudView fudView){
        fudViews.remove(fudView);
        fudList.removeView(fudView);
    }

    public void removeAll() {
        fudViews.clear();
        fudList.removeAllViews();
    }


   public void organizeByUpvotes(){
       TreeSet<FudView> fvs = new TreeSet<FudView>(new FudView.FudViewVoteComparator());
       fvs.addAll(fudViews);

       fudList.removeAllViews();
       for(FudView fv : fudViews){
           fv.unloadImage();
       }

       int toDisplay = 3;
       int pos = 0;
       for(FudView fv : fvs){
           fudList.addView(fv.getView());
           Space between = new Space(MainActivity.this);
           fudList.addView(between);
           between.getLayoutParams().height = ImageHandler.pfdp(20,MainActivity.this);
           fv.setTopInScroll(pos);
           if(toDisplay >= 0){
               fv.loadImage();
               toDisplay--;
           }
           pos = pos + ImageHandler.pfdp(232 + 20, this);
       }
   }

    @Override
    public void onRefresh() {
        // TODO - Joan
        // query db and fetch new Fuds
        swipeRefreshLayout.setRefreshing(true); // sets the refresh animation
        pull();
        swipeRefreshLayout.setRefreshing(false);
    }


}
